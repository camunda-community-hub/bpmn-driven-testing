package org.camunda.community.bpmndt.api;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.camunda.community.bpmndt.api.TestCaseInstanceMemo.JobMemo;
import org.camunda.community.bpmndt.api.TestCaseInstanceMemo.MessageSubscriptionMemo;
import org.camunda.community.bpmndt.api.TestCaseInstanceMemo.ProcessInstanceMemo;
import org.camunda.community.bpmndt.api.TestCaseInstanceMemo.SignalSubscriptionMemo;
import org.camunda.community.bpmndt.api.TestCaseInstanceMemo.TimerMemo;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.filters.RecordStream;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.intent.IncidentIntent;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;
import io.camunda.zeebe.protocol.record.value.BpmnElementType;
import io.camunda.zeebe.protocol.record.value.IncidentRecordValue;
import io.camunda.zeebe.protocol.record.value.ProcessInstanceRecordValue;

/**
 * Link between a test case and its execution, utilizing a process instance that was instantiated by a {@link TestCaseExecutor} and handlers (e.g.
 * {@code UserTaskHandler}) that are part of a test case.
 */
public class TestCaseInstance implements AutoCloseable {

  private final ZeebeTestEngine engine;
  private final ZeebeClient client;

  private final long taskTimeout;
  private final boolean printRecordStreamEnabled;

  private final ExecutorService executorService;

  private Future<?> consumeRecordStreamTask;

  private volatile SelectTask<?> selectTask;
  private volatile SelectAndTestTask selectAndTestTask;

  TestCaseInstance(ZeebeTestEngine engine, ZeebeClient client, long taskTimeout, boolean printRecordStreamEnabled) {
    this.engine = engine;
    this.client = client;

    this.taskTimeout = taskTimeout;
    this.printRecordStreamEnabled = printRecordStreamEnabled;

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

  public void apply(long processInstanceKey, CallActivityHandler handler) {
    handler.apply(this, processInstanceKey);
  }

  public void apply(long processInstanceKey, CustomMultiInstanceHandler handler) {
    handler.apply(this, processInstanceKey);
  }

  public void apply(long processInstanceKey, JobHandler handler) {
    handler.apply(this, processInstanceKey);
  }

  public void apply(long processInstanceKey, MessageEventHandler handler) {
    handler.apply(this, processInstanceKey);
  }

  public void apply(long processInstanceKey, OutboundConnectorHandler handler) {
    handler.apply(this, processInstanceKey);
  }

  public void apply(long processInstanceKey, ReceiveTaskHandler handler) {
    handler.apply(this, processInstanceKey);
  }

  public void apply(long processInstanceKey, SignalEventHandler handler) {
    handler.apply(this, processInstanceKey);
  }

  public void apply(long processInstanceKey, TimerEventHandler handler) {
    handler.apply(this, processInstanceKey);
  }

  public void apply(long processInstanceKey, UserTaskHandler handler) {
    handler.apply(this, processInstanceKey);
  }

  public ZeebeClient getClient() {
    return client;
  }

  public ZeebeTestEngine getEngine() {
    return engine;
  }

  public void hasPassed(long processInstanceKey, String elementId) {
    boolean hasPassed = selectAndTest(memo -> {
      var processInstance = memo.getProcessInstance(processInstanceKey);
      if (processInstance == null) {
        return false;
      }

      var element = processInstance.getElement(elementId);
      return element != null && element.state == ProcessInstanceIntent.ELEMENT_COMPLETED;
    });

    if (!hasPassed) {
      var message = withDetails("expected process instance %d to have passed BPMN element %s, but has not", processInstanceKey);
      throw new AssertionError(String.format(message, processInstanceKey, elementId));
    }
  }

  public void hasPassedMultiInstance(long processInstanceKey, String elementId) {
    boolean hasPassed = selectAndTest(memo -> {
      var processInstance = memo.getProcessInstance(processInstanceKey);
      if (processInstance == null) {
        return false;
      }

      var element = processInstance.getMultiInstance(elementId);
      return element != null && element.state == ProcessInstanceIntent.ELEMENT_COMPLETED;
    });

    if (!hasPassed) {
      var message = withDetails("expected process instance %d to have passed BPMN multi instance element %s, but has not", processInstanceKey);
      throw new AssertionError(String.format(message, processInstanceKey, elementId));
    }
  }

  public void hasTerminated(long processInstanceKey, String bpmnElementId) {
    boolean hasTerminated = selectAndTest(memo -> {
      var processInstance = memo.getProcessInstance(processInstanceKey);
      if (processInstance == null) {
        return false;
      }

      var element = processInstance.getElement(bpmnElementId);
      return element != null && element.state == ProcessInstanceIntent.ELEMENT_TERMINATED;
    });

    if (!hasTerminated) {
      var message = withDetails("expected process instance %d to have terminated BPMN element %s, but has not", processInstanceKey);
      throw new AssertionError(String.format(message, processInstanceKey, bpmnElementId));
    }
  }

  public void hasTerminatedMultiInstance(long processInstanceKey, String elementId) {
    boolean hasTerminated = selectAndTest(memo -> {
      var processInstance = memo.getProcessInstance(processInstanceKey);
      if (processInstance == null) {
        return false;
      }

      var element = processInstance.getMultiInstance(elementId);
      return element != null && element.state == ProcessInstanceIntent.ELEMENT_TERMINATED;
    });

    if (!hasTerminated) {
      var message = withDetails("expected process instance %d to have terminated BPMN multi instance element %s, but has not", processInstanceKey);
      throw new AssertionError(String.format(message, processInstanceKey, elementId));
    }
  }

  public void isCompleted(long processInstanceKey) {
    boolean isCompleted = selectAndTest(memo -> {
      var processInstance = memo.getProcessInstance(processInstanceKey);
      if (processInstance == null) {
        return false;
      }

      return processInstance.state == ProcessInstanceIntent.ELEMENT_COMPLETED;
    });

    if (!isCompleted) {
      String message = withDetails("expected process instance %d to be completed, but was not", processInstanceKey);
      throw new AssertionError(String.format(message, processInstanceKey));
    }
  }

  public void isWaitingAt(long processInstanceKey, String elementId) {
    boolean isWaitingAt = selectAndTest(memo -> {
      var processInstance = memo.getProcessInstance(processInstanceKey);
      if (processInstance == null) {
        return false;
      }

      var element = processInstance.getElement(elementId);
      if (element == null) {
        return false;
      }
      if (element.state == ProcessInstanceIntent.ELEMENT_ACTIVATED) {
        return true;
      }

      // special check for elements with a task definition
      // needed because a job worker could already have completed or terminated the related job
      var job = processInstance.getJob(elementId);
      if (job == null) {
        return false;
      }

      return element.state == ProcessInstanceIntent.ELEMENT_COMPLETED || element.state == ProcessInstanceIntent.ELEMENT_TERMINATED;
    });

    if (!isWaitingAt) {
      var message = withIncidents("expected process instance %d to be waiting at BPMN element %s, but was not", processInstanceKey);
      throw new AssertionError(String.format(message, processInstanceKey, elementId));
    }
  }

  ProcessInstanceMemo getCalledProcessInstance(long processInstanceKey, String callActivityId) {
    return select(memo -> {
      var processInstance = memo.getProcessInstance(processInstanceKey);
      if (processInstance == null) {
        var message = withDetails("process instance %d could not be found", processInstanceKey);
        throw new IllegalStateException(String.format(message, processInstanceKey));
      }

      var callActivity = processInstance.getElement(callActivityId);
      if (callActivity == null) {
        var message = withDetails("call activity %s of process instance %d could not be found", processInstanceKey);
        throw new IllegalStateException(String.format(message, callActivityId, processInstanceKey));
      }

      return memo.getProcessInstances().stream()
          .filter(pi -> pi.parentElementInstanceKey == callActivity.key)
          .findFirst()
          .orElseThrow(() -> {
            var message = withDetails("call activity %s of process instance %d has not called a process", processInstanceKey);
            return new IllegalStateException(String.format(message, callActivityId, processInstanceKey));
          });
    });
  }

  JobMemo getJob(long processInstanceKey, String elementId) {
    return select(memo -> {
      var processInstance = memo.getProcessInstance(processInstanceKey);
      if (processInstance == null) {
        var message = withDetails("process instance %d could not be found", processInstanceKey);
        throw new IllegalStateException(String.format(message, processInstanceKey));
      }

      var job = processInstance.getJob(elementId);
      if (job == null) {
        var message = withDetails("job %s could not be found", processInstanceKey);
        throw new IllegalStateException(String.format(message, elementId));
      }

      return job;
    });
  }

  MessageSubscriptionMemo getMessageSubscription(long processInstanceKey, String elementId) {
    return select(memo -> {
      var processInstance = memo.getProcessInstance(processInstanceKey);
      if (processInstance == null) {
        var message = withDetails("process instance %d could not be found", processInstanceKey);
        throw new IllegalStateException(String.format(message, processInstanceKey));
      }

      var messageSubscription = processInstance.getMessageSubscription(elementId);
      if (messageSubscription == null) {
        var message = withDetails("message subscription %s could not be found", processInstanceKey);
        throw new IllegalStateException(String.format(message, elementId));
      }

      return messageSubscription;
    });
  }

  SignalSubscriptionMemo getSignalSubscription(long processInstanceKey, String elementId) {
    return select(memo -> {
      var processInstance = memo.getProcessInstance(processInstanceKey);
      if (processInstance == null) {
        var message = withDetails("process instance %d could not be found", processInstanceKey);
        throw new IllegalStateException(String.format(message, processInstanceKey));
      }

      return memo.getSignalSubscriptions().stream()
          .filter(signalSubscription -> signalSubscription.catchEventId.equals(elementId))
          .findFirst()
          .orElseThrow(() -> {
            var message = withDetails("element %s of process instance %d has no signal subscription", processInstanceKey);
            return new IllegalStateException(String.format(message, elementId, processInstanceKey));
          });
    });
  }

  TimerMemo getTimer(long processInstanceKey, String elementId) {
    return select(memo -> {
      var processInstance = memo.getProcessInstance(processInstanceKey);
      if (processInstance == null) {
        var message = withDetails("process instance %d could not be found", processInstanceKey);
        throw new IllegalStateException(String.format(message, processInstanceKey));
      }

      var timer = processInstance.getTimer(elementId);
      if (timer == null) {
        var message = withDetails("timer %s could not be found", processInstanceKey);
        throw new IllegalStateException(String.format(message, elementId));
      }

      return timer;
    });
  }

  /**
   * Consumes all records that has not been consumed already. Additionally select and/or select and test tasks that are waiting to be executed are handled using
   * the memorization.
   */
  private void consumeRecordStream() {
    var recordStream = RecordStream.of(engine.getRecordStreamSource());

    var memo = new TestCaseInstanceMemo(printRecordStreamEnabled);

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

          if (task.result != null) {
            task.notify();
          }
        }
      }

      if (selectAndTestTask != null) {
        var task = selectAndTestTask;

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

  /**
   * Selects information from the memorization, using the given function. The function is applied every 100ms until the result is not {@code null} or the task
   * timeout expired.
   *
   * @param selector Selector function.
   * @param <T>      The result type.
   * @return The result.
   */
  private <T> T select(Function<TestCaseInstanceMemo, T> selector) {
    SelectTask<T> task = new SelectTask<>(selector);

    selectTask = task;
    try {
      synchronized (selectTask) {
        task.wait(taskTimeout);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    if (task.e != null) {
      throw new RuntimeException(task.e);
    }

    return task.result;
  }

  /**
   * Selects and tests information from the memorization, using the given predicate. The predicate is tested every 100ms until it is {@code true} or the task
   * timeout expired.
   *
   * @param predicate The predicate to test.
   * @return The test result.
   */
  private boolean selectAndTest(Predicate<TestCaseInstanceMemo> predicate) {
    selectAndTestTask = new SelectAndTestTask(predicate);
    try {
      synchronized (selectAndTestTask) {
        selectAndTestTask.wait(taskTimeout);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    return selectAndTestTask == null; // if true, the reference is set to null
  }

  private String withDetails(String message, long processInstanceKey) {
    return withIncidents(withElementInstances(message, processInstanceKey), processInstanceKey);
  }

  private String withIncidents(String message, long processInstanceKey) {
    var incidents = new LinkedList<IncidentRecordValue>();

    var recordStream = RecordStream.of(engine.getRecordStreamSource());
    for (Record<?> record : recordStream.records()) {
      if (record.getValueType() == ValueType.INCIDENT && record.getIntent() == IncidentIntent.CREATED) {
        var incident = (IncidentRecordValue) record.getValue();
        if (incident.getProcessInstanceKey() == processInstanceKey) {
          incidents.add(incident);
        }
      }
    }

    if (incidents.isEmpty()) {
      return message;
    }

    var messageBuilder = new StringBuilder(message);
    messageBuilder.append("\nfound incidents:");

    for (IncidentRecordValue incident : incidents) {
      messageBuilder.append("\n  - element ");
      messageBuilder.append(incident.getElementId());
      messageBuilder.append(": ");
      messageBuilder.append(incident.getErrorType().name());
      messageBuilder.append(": ");
      messageBuilder.append(incident.getErrorMessage());
    }

    return messageBuilder.toString();
  }

  private String withElementInstances(String message, long processInstanceKey) {
    var elements = new LinkedHashMap<String, ProcessInstanceIntent>();

    var recordStream = RecordStream.of(engine.getRecordStreamSource());
    for (Record<?> record : recordStream.records()) {
      if (record.getValueType() != ValueType.PROCESS_INSTANCE) {
        continue;
      }

      var recordValue = (ProcessInstanceRecordValue) record.getValue();
      if (recordValue.getBpmnElementType() == BpmnElementType.PROCESS || recordValue.getProcessInstanceKey() != processInstanceKey) {
        continue;
      }

      var state = (ProcessInstanceIntent) record.getIntent();

      elements.put(recordValue.getElementId(), state);
    }

    var states = elements.keySet().stream()
        .filter(k -> {
          switch (elements.get(k)) {
            case ELEMENT_ACTIVATED:
            case ELEMENT_COMPLETED:
            case ELEMENT_TERMINATED:
              return true;
            default:
              return false;
          }
        })
        .map(k -> {
          switch (elements.get(k)) {
            case ELEMENT_ACTIVATED:
              return k + " (activated)";
            case ELEMENT_COMPLETED:
              return k + " (completed)";
            case ELEMENT_TERMINATED:
              return k + " (terminated)";
            default:
              return null;
          }
        })
        .collect(Collectors.toList());

    if (states.isEmpty()) {
      return message;
    }

    var messageBuilder = new StringBuilder(message);
    messageBuilder.append("\nfound element instances:");

    for (String state : states) {
      messageBuilder.append("\n  - ");
      messageBuilder.append(state);
    }

    return messageBuilder.toString();
  }

  private static class SelectTask<T> {

    final Function<TestCaseInstanceMemo, T> selector;

    RuntimeException e;
    T result;

    SelectTask(Function<TestCaseInstanceMemo, T> selector) {
      this.selector = selector;
    }

    void select(TestCaseInstanceMemo memo) {
      try {
        result = selector.apply(memo);
      } catch (RuntimeException e) {
        this.e = e;
      }
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
