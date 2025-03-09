package org.camunda.community.bpmndt.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.camunda.community.bpmndt.api.TestCaseInstanceMemo.IncidentMemo;
import org.camunda.community.bpmndt.api.TestCaseInstanceMemo.JobMemo;
import org.camunda.community.bpmndt.api.TestCaseInstanceMemo.MessageSubscriptionMemo;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.filters.RecordStream;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;

/**
 * Link between a test case and its execution.
 * <p>
 * This class is utilizing a process instance that was instantiated by a {@link TestCaseExecutor} and handlers (e.g. {@code UserTaskHandler}) that are part of a
 * test case.
 */
public class TestCaseInstance implements AutoCloseable {

  private final ZeebeTestEngine engine;
  private final ZeebeClient client;

  private final long waitTimeout;

  private final RecordStream recordStream;
  private final TestCaseInstanceMemo memo;

  private int recordCount;

  TestCaseInstance(ZeebeTestEngine engine, ZeebeClient client, long waitTimeout, boolean printRecordStreamEnabled) {
    this.engine = engine;
    this.client = client;

    this.waitTimeout = waitTimeout;

    recordStream = RecordStream.of(engine.getRecordStreamSource());
    memo = new TestCaseInstanceMemo(printRecordStreamEnabled);
  }

  public void close() {
    memo.clear();
  }

  public void apply(long flowScopeKey, CallActivityHandler handler) {
    handler.apply(this, flowScopeKey);
  }

  public void apply(long flowScopeKey, CustomMultiInstanceHandler handler) {
    handler.apply(this, flowScopeKey);
  }

  public void apply(long flowScopeKey, JobHandler handler) {
    handler.apply(this, flowScopeKey);
  }

  public void apply(long flowScopeKey, MessageEventHandler handler) {
    handler.apply(this, flowScopeKey);
  }

  public void apply(long flowScopeKey, OutboundConnectorHandler handler) {
    handler.apply(this, flowScopeKey);
  }

  public void apply(long flowScopeKey, ReceiveTaskHandler handler) {
    handler.apply(this, flowScopeKey);
  }

  public void apply(long flowScopeKey, SignalEventHandler handler) {
    handler.apply(this, flowScopeKey);
  }

  public void apply(long flowScopeKey, TimerEventHandler handler) {
    handler.apply(this, flowScopeKey);
  }

  public void apply(long flowScopeKey, UserTaskHandler handler) {
    handler.apply(this, flowScopeKey);
  }

  public RuntimeException createException(String message, long flowScopeKey) {
    return new Exception(memo, message, flowScopeKey);
  }

  public ZeebeClient getClient() {
    return client;
  }

  public ZeebeTestEngine getEngine() {
    return engine;
  }

  /**
   * Returns the element instance key for a BPMN element within a given flow scope.
   *
   * @param flowScopeKey The key of an existing flow scope.
   * @param elementId    The BPMN element ID.
   * @return The element instance key.
   * @throws RuntimeException If no such element instance exists.
   */
  public long getElementInstanceKey(long flowScopeKey, String elementId) {
    return select(memo -> {
      var element = memo.elements.stream().filter(e ->
          e.flowScopeKey == flowScopeKey && Objects.equals(e.id, elementId)
      ).findFirst();

      if (element.isEmpty()) {
        var message = String.format("element %s of flow scope %d could not be found", elementId, flowScopeKey);
        throw createException(message, flowScopeKey);
      }

      return element.get().key;
    });
  }

  /**
   * Returns the flow scope key of an element instance.
   *
   * @param elementInstanceKey The key of an existing element instance.
   * @return The flow scope key or {@code -1} if there is no mapping for the given element instance.
   */
  public long getFlowScopeKey(long elementInstanceKey) {
    var flowScopeKey = memo.keys.get(elementInstanceKey);
    return flowScopeKey != null ? flowScopeKey : -1;
  }

  /**
   * Returns the process instance key of an element instance.
   *
   * @param elementInstanceKey The key of an existing element instance.
   * @return The process instance key.
   */
  public long getProcessInstanceKey(long elementInstanceKey) {
    return memo.getProcessInstanceKey(elementInstanceKey);
  }

  /**
   * Checks if a BPMN element has been passed within the given flow scope.
   *
   * @param flowScopeKey The key of an existing flow scope.
   * @param elementId    The BPMN element ID to test.
   * @throws RuntimeException If the BPMN element has not been passed (is not completed).
   */
  public void hasPassed(long flowScopeKey, String elementId) {
    select(memo -> {
      var hasPassed = memo.elements.stream().anyMatch(e ->
          e.flowScopeKey == flowScopeKey
              && Objects.equals(e.id, elementId)
              && e.state == ProcessInstanceIntent.ELEMENT_COMPLETED
      );

      if (!hasPassed) {
        var message = String.format("expected flow scope %d to have passed element %s, but has not", flowScopeKey, elementId);
        throw createException(message, flowScopeKey);
      }

      return true;
    });
  }

  /**
   * Checks if a BPMN multi instance element has been passed within the given flow scope.
   *
   * @param flowScopeKey The key of an existing flow scope.
   * @param elementId    The BPMN element ID to test.
   * @throws RuntimeException If the BPMN multi instance element has not been passed (is not completed).
   */
  public void hasPassedMultiInstance(long flowScopeKey, String elementId) {
    select(memo -> {
      var hasPassed = memo.multiInstanceElements.stream().anyMatch(e ->
          e.flowScopeKey == flowScopeKey
              && Objects.equals(e.id, elementId)
              && e.state == ProcessInstanceIntent.ELEMENT_COMPLETED
      );

      if (!hasPassed) {
        var message = String.format("expected flow scope %d to have passed multi instance element %s, but has not", flowScopeKey, elementId);
        throw createException(message, flowScopeKey);
      }

      return true;
    });
  }

  /**
   * Checks if a BPMN element has been terminated within the given flow scope.
   *
   * @param flowScopeKey The key of an existing flow scope.
   * @param elementId    The BPMN element ID to test.
   * @throws RuntimeException If the BPMN element has not been terminated.
   */
  public void hasTerminated(long flowScopeKey, String elementId) {
    select(memo -> {
      var hasTerminated = memo.elements.stream().anyMatch(e ->
          e.flowScopeKey == flowScopeKey
              && Objects.equals(e.id, elementId)
              && e.state == ProcessInstanceIntent.ELEMENT_TERMINATED
      );

      if (!hasTerminated) {
        var message = String.format("expected flow scope %d to have terminated element %s, but has not", flowScopeKey, elementId);
        throw createException(message, flowScopeKey);
      }

      return true;
    });
  }

  /**
   * Checks if a BPMN multi instance element has been terminated within the given flow scope.
   *
   * @param flowScopeKey The key of an existing flow scope.
   * @param elementId    The BPMN element ID to test.
   * @throws RuntimeException If the BPMN multi instance element has not been terminated.
   */
  public void hasTerminatedMultiInstance(long flowScopeKey, String elementId) {
    select(memo -> {
      var hasTerminated = memo.multiInstanceElements.stream().anyMatch(e ->
          e.flowScopeKey == flowScopeKey
              && Objects.equals(e.id, elementId)
              && e.state == ProcessInstanceIntent.ELEMENT_TERMINATED
      );

      if (!hasTerminated) {
        var message = String.format("expected flow scope %d to have terminated multi instance element %s, but has not", flowScopeKey, elementId);
        throw createException(message, flowScopeKey);
      }

      return true;
    });
  }

  /**
   * Checks if a BPMN element is being activated within the given flow scope. This method is used to verify that a terminating end event (error or escalation)
   * has been reached.
   *
   * @param flowScopeKey The key of an existing flow scope.
   * @param elementId    The BPMN element ID to test.
   * @throws RuntimeException If the BPMN element is not being activated.
   */
  public void isActivating(long flowScopeKey, String elementId) {
    select(memo -> {
      var hasTerminated = memo.elements.stream().anyMatch(e ->
          e.flowScopeKey == flowScopeKey
              && Objects.equals(e.id, elementId)
              && e.state == ProcessInstanceIntent.ELEMENT_ACTIVATING
      );

      if (!hasTerminated) {
        var message = String.format("expected flow scope %d to have activating element %s, but has not", flowScopeKey, elementId);
        throw createException(message, flowScopeKey);
      }

      return true;
    });
  }

  /**
   * Checks if the given process instance is completed.
   *
   * @param processInstanceKey The key of an existing process instance.
   * @throws RuntimeException If the process instance is not completed.
   */
  public void isCompleted(long processInstanceKey) {
    select(memo -> {
      var isCompleted = memo.processInstances.stream().anyMatch(processInstance ->
          processInstance.key == processInstanceKey && processInstance.state == ProcessInstanceIntent.ELEMENT_COMPLETED
      );

      if (!isCompleted) {
        var message = String.format("expected process instance %d to be completed, but was not", processInstanceKey);
        throw createException(message, processInstanceKey);
      }

      return true;
    });
  }

  /**
   * Checks if a flow is waiting at a specific BPMN element.
   *
   * @param flowScopeKey The key of an existing flow scope.
   * @param elementId    The BPMN element to test.
   * @throws RuntimeException If the flow is not waiting at the BPMN element.
   */
  public void isWaitingAt(long flowScopeKey, String elementId) {
    select(memo -> {
      var isWaitingAt = memo.elements.stream().anyMatch(e ->
          e.flowScopeKey == flowScopeKey
              && Objects.equals(e.id, elementId)
              && e.state == ProcessInstanceIntent.ELEMENT_ACTIVATED
      );

      if (!isWaitingAt) {
        var message = String.format("expected flow scope %d to be waiting at element %s, but was not", flowScopeKey, elementId);
        throw createException(message, flowScopeKey);
      }

      return true;
    });
  }

  List<Long> getKeys(long key) {
    var keys = new ArrayList<Long>(1);

    var current = key;
    while (true) {
      keys.add(current);

      var parent = memo.keys.get(current);
      if (parent == null) {
        break;
      }
      current = parent;
    }

    return keys;
  }

  JobMemo getJob(long flowScopeKey, String elementId) {
    return select(memo -> {
      var job = memo.jobs.stream().filter(j ->
          j.flowScopeKey == flowScopeKey && Objects.equals(j.elementId, elementId)
      ).findFirst();

      if (job.isEmpty()) {
        var message = String.format("job for element %s of flow scope %d could not be found", elementId, flowScopeKey);
        throw createException(message, flowScopeKey);
      }

      memo.jobs.remove(job.get());

      return job.get();
    });
  }

  MessageSubscriptionMemo getMessageSubscription(long flowScopeKey, String elementId) {
    var flowScopeKeys = getKeys(flowScopeKey);

    return select(memo -> {
      var messageSubscription = memo.messageSubscriptions.stream().filter(s ->
          flowScopeKeys.contains(s.flowScopeKey) && Objects.equals(s.elementId, elementId)
      ).findFirst();

      if (messageSubscription.isEmpty()) {
        var message = String.format("element %s of flow scope %d has no message subscription", elementId, flowScopeKey);
        throw createException(message, flowScopeKey);
      }

      memo.messageSubscriptions.remove(messageSubscription.get());

      return messageSubscription.get();
    });
  }

  /**
   * Selects information from the memorization, using the given function.
   * <p>
   * The function is applied every 100ms until the result is not {@code null} or the wait timeout expired.
   * <p>
   *
   * @param selector Selector function.
   * @param <T>      The result type.
   * @return The result.
   */
  <T> T select(Function<TestCaseInstanceMemo, T> selector) {
    T result = null;
    RuntimeException ex = null;

    var a = System.currentTimeMillis();
    var b = a;

    while ((b - a) < waitTimeout) {
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

      try {
        result = selector.apply(memo);
      } catch (RuntimeException e) {
        ex = e;
      }

      if (result != null) {
        return result;
      }

      try {
        TimeUnit.MILLISECONDS.sleep(100);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      b = System.currentTimeMillis();
    }

    if (ex != null) {
      throw new RuntimeException(ex);
    }

    return null;
  }

  private static class Exception extends RuntimeException {

    private final TestCaseInstanceMemo memo;
    private final String message;
    private final long flowScopeKey;

    private Exception(TestCaseInstanceMemo memo, String message, long flowScopeKey) {
      this.memo = memo;
      this.message = message;
      this.flowScopeKey = flowScopeKey;
    }

    @Override
    public String getMessage() {
      var processInstanceKey = memo.getProcessInstanceKey(flowScopeKey);

      var b = new StringBuilder(message);

      var incidents = memo.incidents.stream().filter(i -> i.processInstanceKey == processInstanceKey).collect(Collectors.toList());
      if (!incidents.isEmpty()) {
        b.append("\nfound incidents:");

        for (IncidentMemo incident : incidents) {
          b.append("\n  - element ");
          b.append(incident.elementId);
          b.append(": ");
          b.append(incident.errorType.name());
          b.append(": ");
          b.append(incident.errorMessage);
        }
      }

      var elements = new LinkedHashMap<String, ProcessInstanceIntent>();

      memo.elements.stream().filter(e -> e.processInstanceKey == processInstanceKey).forEach(e -> elements.put(e.id, e.state));

      var states = elements.keySet().stream()
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

      if (!states.isEmpty()) {
        b.append("\nfound element instances:");

        for (String state : states) {
          b.append("\n  - ");
          b.append(state);
        }
      }

      return b.toString();
    }
  }
}
