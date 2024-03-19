package org.camunda.community.bpmndt.platform8.api;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceMemo.ElementMemo;
import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceMemo.JobMemo;
import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceMemo.MessageSubscriptionMemo;
import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceMemo.ProcessInstanceMemo;
import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceMemo.SignalSubscriptionMemo;
import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceMemo.TimerMemo;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.filters.RecordStream;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;

/**
 * Link between a test case and its execution, utilizing a process instance that was instantiated by a {@link TestCaseExecutor} and handlers (e.g.
 * {@code UserTaskHandler}) that are part of a test case.
 */
public class TestCaseInstance implements AutoCloseable {

  final ZeebeTestEngine engine;
  final ZeebeClient client;

  private final long taskTimeout;

  private final ExecutorService executorService;

  private Future<?> consumeRecordStreamTask;

  private volatile SelectTask<?> selectTask;
  private volatile RuntimeException selectTaskException;
  private volatile SelectAndTestTask selectAndTestTask;

  TestCaseInstance(ZeebeTestEngine engine, ZeebeClient client, long taskTimeout) {
    this.engine = engine;
    this.client = client;
    this.taskTimeout = taskTimeout;

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

  public void apply(ProcessInstanceEvent processInstanceEvent, MessageEventHandler handler) {
    handler.apply(this, processInstanceEvent);
  }

  public void apply(ProcessInstanceEvent processInstanceEvent, SignalEventHandler handler) {
    handler.apply(this, processInstanceEvent);
  }

  public void apply(ProcessInstanceEvent processInstanceEvent, TimerEventHandler handler) {
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

      ElementMemo element = processInstance.getElement(bpmnElementId);
      return element != null && element.state == ProcessInstanceIntent.ELEMENT_COMPLETED;
    });

    if (!hasPassed) {
      String message = "expected process instance %d to have passed BPMN element %s, but has not";
      throw new AssertionError(String.format(message, processInstanceEvent.getProcessInstanceKey(), bpmnElementId));
    }
  }

  public void hasTerminated(ProcessInstanceEvent processInstanceEvent, String bpmnElementId) {
    boolean hasTerminated = selectAndTest(memo -> {
      ProcessInstanceMemo processInstance = memo.processInstances.get(processInstanceEvent.getProcessInstanceKey());
      if (processInstance == null) {
        return false;
      }

      ElementMemo element = processInstance.getElement(bpmnElementId);
      return element != null && element.state == ProcessInstanceIntent.ELEMENT_TERMINATED;
    });

    if (!hasTerminated) {
      String message = "expected process instance %d to have terminated BPMN element %s, but has not";
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

      ElementMemo element = processInstance.getElement(bpmnElementId);
      if (element == null) {
        return false;
      }
      if (element.state == ProcessInstanceIntent.ELEMENT_ACTIVATED) {
        return true;
      }

      // special check for elements with a task definition
      // needed because a job worker could already have completed or terminated the related job
      JobMemo job = processInstance.getJob(bpmnElementId);
      if (job == null) {
        return false;
      }

      return element.state == ProcessInstanceIntent.ELEMENT_COMPLETED || element.state == ProcessInstanceIntent.ELEMENT_TERMINATED;
    });

    if (!isWaitingAt) {
      String message = "expected process instance %d to be waiting at BPMN element %s, but was not";
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

      JobMemo job = processInstance.getJob(bpmnElementId);
      if (job == null) {
        String message = "job %s could not be found";
        throw new IllegalStateException(String.format(message, bpmnElementId));
      }

      return job;
    });
  }

  MessageSubscriptionMemo getMessageSubscription(ProcessInstanceEvent processInstanceEvent, String bpmnElementId) {
    return select(memo -> {
      ProcessInstanceMemo processInstance = memo.processInstances.get(processInstanceEvent.getProcessInstanceKey());
      if (processInstance == null) {
        String message = "process instance %d could not be found";
        throw new IllegalStateException(String.format(message, processInstanceEvent.getProcessInstanceKey()));
      }

      MessageSubscriptionMemo messageSubscription = processInstance.getMessageSubscription(bpmnElementId);
      if (messageSubscription == null) {
        String message = "message subscription %s could not be found";
        throw new IllegalStateException(String.format(message, bpmnElementId));
      }

      return messageSubscription;
    });
  }

  SignalSubscriptionMemo getSignalSubscription(ProcessInstanceEvent processInstanceEvent, String bpmnElementId) {
    return select(memo -> {
      ProcessInstanceMemo processInstance = memo.processInstances.get(processInstanceEvent.getProcessInstanceKey());
      if (processInstance == null) {
        String message = "process instance %d could not be found";
        throw new IllegalStateException(String.format(message, processInstanceEvent.getProcessInstanceKey()));
      }

      ElementMemo element = processInstance.getElement(bpmnElementId);
      if (element == null || element.state != ProcessInstanceIntent.ELEMENT_ACTIVATED) {
        String message = "element %s of process instance %d has not been activated";
        throw new IllegalStateException(String.format(message, bpmnElementId, processInstanceEvent.getProcessInstanceKey()));
      }

      return memo.signalSubscriptions.stream()
          .filter(signalSubscription -> signalSubscription.catchEventInstanceKey == element.key)
          .findFirst()
          .orElseThrow(() -> {
            String message = "element %s of process instance %d has no signal subscription";
            throw new IllegalStateException(String.format(message, bpmnElementId, processInstanceEvent.getProcessInstanceKey()));
          });
    });
  }

  TimerMemo getTimer(ProcessInstanceEvent processInstanceEvent, String bpmnElementId) {
    return select(memo -> {
      ProcessInstanceMemo processInstance = memo.processInstances.get(processInstanceEvent.getProcessInstanceKey());
      if (processInstance == null) {
        String message = "process instance %d could not be found";
        throw new IllegalStateException(String.format(message, processInstanceEvent.getProcessInstanceKey()));
      }

      TimerMemo timer = processInstance.getTimer(bpmnElementId);
      if (timer == null) {
        String message = "timer %s could not be found";
        throw new IllegalStateException(String.format(message, bpmnElementId));
      }

      return timer;
    });
  }

  /**
   * Consumes all records that has not been consumed already. Additionally select as well as select and test tasks that are waiting to be executed are handled
   * using the memorization.
   */
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
          try {
            task.select(memo);
          } catch (RuntimeException e) {
            selectTaskException = e;
          }

          if (task.result != null) {
            task.notify();
          }
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
    selectTaskException = null;
    try {
      synchronized (selectTask) {
        task.wait(taskTimeout);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    if (selectTaskException != null) {
      throw selectTaskException;
    }

    return task.result;
  }

  private boolean selectAndTest(Predicate<TestCaseInstanceMemo> predicate) {
    selectAndTestTask = new SelectAndTestTask(predicate);
    try {
      synchronized (selectAndTestTask) {
        selectAndTestTask.wait(taskTimeout);
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
