package org.camunda.community.bpmndt.platform8.api;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceMemo.ElementMemo;
import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceMemo.JobMemo;
import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceMemo.ProcessInstanceMemo;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.filters.RecordStream;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.intent.JobIntent;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;

/**
 * Link between a test case and its execution, utilizing a process instance that was instantiated by a {@link TestCaseExecutor} and handlers (e.g.
 * {@code UserTaskHandler}) that are part of a test case.
 */
public class TestCaseInstance implements AutoCloseable {

  final ZeebeTestEngine engine;
  final ZeebeClient client;

  private final ExecutorService executorService;

  private Future<?> consumeRecordStreamTask;

  private volatile SelectTask<?> selectTask;
  private volatile SelectAndTestTask selectAndTestTask;

  TestCaseInstance(ZeebeTestEngine engine, ZeebeClient client) {
    this.engine = engine;
    this.client = client;

    executorService = Executors.newSingleThreadScheduledExecutor();

    consumeRecordStreamTask = executorService.submit(this::consumeRecordStream);
  }

  public void close() {
    if (consumeRecordStreamTask != null) {
      consumeRecordStreamTask.cancel(true);
      consumeRecordStreamTask = null;
    }

    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
        throw new IllegalStateException("failed to stop record stream consumption");
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  public void apply(ProcessInstanceEvent processInstanceEvent, JobHandler handler) {
    handler.apply(this, processInstanceEvent);
  }

  public void apply(ProcessInstanceEvent processInstanceEvent, UserTaskHandler handler) {
    handler.apply(this, processInstanceEvent);
  }

  public void hasPassed(ProcessInstanceEvent processInstanceEvent, String bpmnElementId) {
    boolean hasPassed = selectAndTest(memo -> {
      ProcessInstanceMemo processInstance = memo.processInstances.get(processInstanceEvent.getProcessInstanceKey());
      if (processInstance == null) {
        return false;
      }

      ElementMemo element = processInstance.elements.get(bpmnElementId);
      return element != null && element.state == ProcessInstanceIntent.ELEMENT_COMPLETED;
    });

    if (!hasPassed) {
      String message = "expected process instance %d to has passed BPMN element %s, but was not";
      throw new AssertionError(String.format(message, processInstanceEvent.getProcessInstanceKey(), bpmnElementId));
    }
  }

  public void isCompleted(ProcessInstanceEvent processInstanceEvent) {
    boolean isCompleted = selectAndTest(memo -> {
      ProcessInstanceMemo processInstance = memo.processInstances.get(processInstanceEvent.getProcessInstanceKey());
      if (processInstance == null) {
        return false;
      }

      return processInstance.state == ProcessInstanceIntent.ELEMENT_COMPLETED;
    });

    if (!isCompleted) {
      String message = "expected process instance %d to be completed, but was not";
      throw new AssertionError(String.format(message, processInstanceEvent.getProcessInstanceKey()));
    }
  }

  public void isWaitingAt(ProcessInstanceEvent processInstanceEvent, String bpmnElementId) {
    boolean isWaitingAt = selectAndTest(memo -> {
      ProcessInstanceMemo processInstance = memo.processInstances.get(processInstanceEvent.getProcessInstanceKey());
      if (processInstance == null) {
        return false;
      }

      JobMemo job = processInstance.jobs.get(bpmnElementId);
      if (job == null) {
        return false;
      }

      return job.state == JobIntent.CREATED;
    });

    if (!isWaitingAt) {
      String message = "expected process instance %d to be waiting at %s, but was not";
      throw new AssertionError(String.format(message, processInstanceEvent.getProcessInstanceKey(), bpmnElementId));
    }
  }

  JobMemo getJob(ProcessInstanceEvent processInstanceEvent, String bpmnElementId) {
    return select(memo -> {
      ProcessInstanceMemo processInstance = memo.processInstances.get(processInstanceEvent.getProcessInstanceKey());
      if (processInstance == null) {
        String message = "process instance %d could not be found";
        throw new IllegalStateException(String.format(message, processInstanceEvent.getProcessInstanceKey()));
      }

      JobMemo jobMemo = processInstance.jobs.get(bpmnElementId);
      if (jobMemo == null) {
        String message = "job %s could not be found";
        throw new IllegalStateException(String.format(message, bpmnElementId));
      }

      return jobMemo;
    });
  }

  private void consumeRecordStream() {
    RecordStream recordStream = RecordStream.of(engine.getRecordStreamSource());

    TestCaseInstanceMemo memo = new TestCaseInstanceMemo();

    int recordCount = 0;
    while (true) {
      int i = 0;
      for (Record<?> record : recordStream.records()) {
        i++;

        if (i <= recordCount) {
          // skip record, since it has been processed already
          continue;
        }

        recordCount++;

        memo.apply(record);
      }

      if (selectTask != null) {
        SelectTask<?> task = selectTask;

        selectTask = null;

        synchronized (task) {
          task.select(memo);
          task.notify();
        }
      }

      if (selectAndTestTask != null) {
        SelectAndTestTask task = selectAndTestTask;

        if (task.selectAndTest(memo)) {
          selectAndTestTask = null;

          synchronized (task) {
            task.notify();
          }
        }
      }

      try {
        TimeUnit.MILLISECONDS.sleep(100);
      } catch (InterruptedException e) {
        return;
      }
    }
  }

  private <T> T select(Function<TestCaseInstanceMemo, T> selector) {
    SelectTask<T> task = new SelectTask<>(selector);

    selectTask = task;
    try {
      synchronized (selectTask) {
        task.wait(5000);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    return task.result;
  }

  private boolean selectAndTest(Predicate<TestCaseInstanceMemo> predicate) {
    selectAndTestTask = new SelectAndTestTask(predicate);
    try {
      synchronized (selectAndTestTask) {
        selectAndTestTask.wait(5000);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    return selectAndTestTask == null;
  }

  private static class SelectTask<T> {

    final Function<TestCaseInstanceMemo, T> selector;

    T result;

    SelectTask(Function<TestCaseInstanceMemo, T> selector) {
      this.selector = selector;
    }

    void select(TestCaseInstanceMemo memo) {
      result = selector.apply(memo);
    }
  }

  private static class SelectAndTestTask {

    final Predicate<TestCaseInstanceMemo> predicate;

    SelectAndTestTask(Predicate<TestCaseInstanceMemo> predicate) {
      this.predicate = predicate;
    }

    boolean selectAndTest(TestCaseInstanceMemo memo) {
      return predicate.test(memo);
    }
  }
}
