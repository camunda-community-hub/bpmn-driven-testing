package org.camunda.community.bpmndt8.api;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.filters.RecordStream;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordType;
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.intent.JobIntent;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;
import io.camunda.zeebe.protocol.record.value.BpmnElementType;
import io.camunda.zeebe.protocol.record.value.JobRecordValue;
import io.camunda.zeebe.protocol.record.value.ProcessInstanceRecordValue;

/**
 * Link between a test case and its execution, utilizing a process instance that was instantiated by a {@link TestCaseExecutor} and handlers (e.g.
 * {@code UserTaskHandler}) that are part of a test case.
 */
public class TestCaseInstance implements AutoCloseable {

  final ZeebeTestEngine engine;
  final ZeebeClient client;

  private final ExecutorService executorService;

  private Future<?> consumeRecordStreamTask;

  private volatile Assertion currentAssertion;
  private volatile Selection currentSelection;

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

  void apply(long processInstanceKey, JobHandler handler) {
    handler.apply(this, processInstanceKey);
  }

  void consumeRecordStream() {
    var recordStream = RecordStream.of(engine.getRecordStreamSource());

    var memos = new HashMap<Long, ProcessInstanceMemo>();

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

        if (record.getRecordType() != RecordType.EVENT) {
          continue;
        }

        if (record.getValueType() == ValueType.JOB) {
          var recordValue = (JobRecordValue) record.getValue();

          if (record.getIntent() == JobIntent.CREATED) {
            var jobMemo = new JobMemo();
            jobMemo.id = recordValue.getElementId();
            jobMemo.state = JobIntent.CREATED;
            jobMemo.type = recordValue.getType();

            var memo = memos.get(recordValue.getProcessInstanceKey());
            memo.jobs.put(jobMemo.id, jobMemo);
          }
        }

        if (record.getValueType() == ValueType.PROCESS_INSTANCE) {
          var recordValue = (ProcessInstanceRecordValue) record.getValue();

          if (record.getIntent() == ProcessInstanceIntent.ELEMENT_ACTIVATED) {
            if (recordValue.getBpmnElementType() == BpmnElementType.PROCESS) {
              var memo = new ProcessInstanceMemo();
              memo.key = recordValue.getProcessInstanceKey();
              memo.state = ProcessInstanceIntent.ELEMENT_ACTIVATED;
              memos.put(memo.key, memo);
            }
          }

          if (record.getIntent() == ProcessInstanceIntent.ELEMENT_COMPLETED || record.getIntent() == ProcessInstanceIntent.ELEMENT_TERMINATED) {
            var state = (ProcessInstanceIntent) record.getIntent();

            if (recordValue.getBpmnElementType() == BpmnElementType.PROCESS) {
              var memo = memos.get(recordValue.getProcessInstanceKey());
              memo.state = state;
            } else {
              var elementMemo = new ElementMemo();
              elementMemo.id = recordValue.getElementId();
              elementMemo.state = state;

              var memo = memos.get(recordValue.getProcessInstanceKey());
              memo.elements.put(elementMemo.id, elementMemo);
            }
          }
        }
      }

      var assertion = currentAssertion;
      if (assertion != null && assertion.test(memos)) {
        currentAssertion = null;

        synchronized (assertion) {
          assertion.notify();
        }
      }

      var selection = currentSelection;
      if (selection != null) {
        currentSelection = null;

        synchronized (selection) {
          selection.select(memos);
          selection.notify();
        }
      }

      try {
        TimeUnit.MILLISECONDS.sleep(100);
      } catch (InterruptedException e) {
        return;
      }
    }
  }

  String getJobType(long processInstanceKey, String bpmnElementId) {
    return submitSelection(new GetJobType(processInstanceKey, bpmnElementId));
  }

  void hasJobType(long processInstanceKey, String bpmnElementId, String expectedType) {
    submitAssertion(new HasJobType(processInstanceKey, bpmnElementId, expectedType));
  }

  void hasPassed(long processInstanceKey, String bpmnElementId) {
    submitAssertion(new HasPassed(processInstanceKey, bpmnElementId));
  }

  void isCompleted(long processInstanceKey) {
    submitAssertion(new IsCompleted(processInstanceKey));
  }

  void isWaitingAt(long processInstanceKey, String bpmnElementId) {
    submitAssertion(new IsWaitingAt(processInstanceKey, bpmnElementId));
  }

  private void submitAssertion(Assertion assertion) {
    currentAssertion = assertion;

    try {
      synchronized (currentAssertion) {
        currentAssertion.wait(5000);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    if (currentAssertion != null) {
      throw new AssertionError(assertion.errorMessage());
    }
  }

  private <T> T submitSelection(Selection<T> selection) {
    currentSelection = selection;
    try {
      synchronized (currentSelection) {
        currentSelection.wait(5000);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    return selection.result;
  }

  private interface Assertion {

    String errorMessage();

    boolean test(Map<Long, ProcessInstanceMemo> memos);
  }

  private static abstract class Selection<T> {

    T result;

    abstract void select(Map<Long, ProcessInstanceMemo> memos);
  }

  private static class ElementMemo {

    String id;
    ProcessInstanceIntent state;
  }

  private static class JobMemo {

    String id;
    JobIntent state;
    String type;
  }

  private static class ProcessInstanceMemo {

    final Map<String, ElementMemo> elements = new HashMap<>();
    final Map<String, JobMemo> jobs = new HashMap<>();

    long key;
    ProcessInstanceIntent state;
  }

  private static class GetJobType extends Selection<String> {

    final long processInstanceKey;
    final String bpmnElementId;

    GetJobType(long processInstanceKey, String bpmnElementId) {
      this.processInstanceKey = processInstanceKey;
      this.bpmnElementId = bpmnElementId;
    }

    @Override
    void select(Map<Long, ProcessInstanceMemo> memos) {
      var memo = memos.get(processInstanceKey);
      if (memo == null) {
        throw new IllegalStateException("process instance %d could not be found".formatted(processInstanceKey));
      }

      var jobMemo = memo.jobs.get(bpmnElementId);
      if (jobMemo == null) {
        throw new IllegalStateException("job %s could not be found".formatted(bpmnElementId));
      }

      result = jobMemo.type;
    }
  }

  private static class HasJobType implements Assertion {

    final long processInstanceKey;
    final String bpmnElementId;
    final String expectedType;

    String actualType;

    HasJobType(long processInstanceKey, String bpmnElementId, String expectedType) {
      this.processInstanceKey = processInstanceKey;
      this.bpmnElementId = bpmnElementId;
      this.expectedType = expectedType;
    }

    @Override
    public String errorMessage() {
      return "expected job %s to be of type %s, but was %s".formatted(bpmnElementId, expectedType, actualType);
    }

    @Override
    public boolean test(Map<Long, ProcessInstanceMemo> memos) {
      var memo = memos.get(processInstanceKey);
      if (memo == null) {
        return false;
      }

      var jobMemo = memo.jobs.get(bpmnElementId);
      if (jobMemo == null) {
        return false;
      }

      actualType = jobMemo.type;
      return expectedType.equals(actualType);
    }
  }

  private record HasPassed(long processInstanceKey, String bpmnElementId) implements Assertion {

    @Override
    public String errorMessage() {
      return "expected process instance %d to has passed BPMN element %s, but was not".formatted(processInstanceKey, bpmnElementId);
    }

    @Override
    public boolean test(Map<Long, ProcessInstanceMemo> memos) {
      var memo = memos.get(processInstanceKey);
      if (memo == null) {
        return false;
      }

      var elementMemo = memo.elements.get(bpmnElementId);
      return elementMemo != null && elementMemo.state == ProcessInstanceIntent.ELEMENT_COMPLETED;
    }
  }

  private record IsCompleted(long processInstanceKey) implements Assertion {

    @Override
    public String errorMessage() {
      return "expected process instance %d to be completed, but was not".formatted(processInstanceKey);
    }

    @Override
    public boolean test(Map<Long, ProcessInstanceMemo> memos) {
      var memo = memos.get(processInstanceKey);
      return memo != null && memo.state == ProcessInstanceIntent.ELEMENT_COMPLETED;
    }
  }

  private record IsWaitingAt(long processInstanceKey, String bpmnElementId) implements Assertion {

    @Override
    public String errorMessage() {
      return "expected process instance %d to be waiting at %s, but was not".formatted(processInstanceKey, bpmnElementId);
    }

    @Override
    public boolean test(Map<Long, ProcessInstanceMemo> memos) {
      var memo = memos.get(processInstanceKey);
      if (memo == null) {
        return false;
      }

      var jobMemo = memo.jobs.get(bpmnElementId);
      return jobMemo != null && jobMemo.state == JobIntent.CREATED;
    }
  }
}
