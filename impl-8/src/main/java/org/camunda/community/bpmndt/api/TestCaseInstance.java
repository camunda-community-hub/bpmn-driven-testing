package org.camunda.community.bpmndt.api;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.command.ProblemException;
import io.camunda.client.api.search.enums.ElementInstanceState;
import io.camunda.client.api.search.enums.ElementInstanceType;
import io.camunda.client.api.search.response.ElementInstance;
import io.camunda.client.api.search.response.Job;
import io.camunda.client.api.search.response.MessageSubscription;
import io.camunda.process.test.api.CamundaAssert;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceSelectors;
import io.camunda.process.test.impl.assertions.util.AwaitilityBehavior;

/**
 * Link between a test case and its execution.
 * <p>
 * This class is utilizing a process instance that was instantiated by a {@link TestCaseExecutor} and handlers (e.g. {@code UserTaskHandler}) that are part of a
 * test case.
 */
public class TestCaseInstance {

  private final CamundaClient client;
  private final CamundaProcessTestContext processTestContext;

  private final AwaitilityBehavior awaitilityBehavior;

  TestCaseInstance(CamundaClient client, CamundaProcessTestContext processTestContext, AwaitilityBehavior awaitilityBehavior) {
    this.client = client;
    this.processTestContext = processTestContext;
    this.awaitilityBehavior = awaitilityBehavior;
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

  public CamundaClient getClient() {
    return client;
  }

  /**
   * Returns the element instance key for a BPMN element within a given flow scope.
   *
   * @param flowScopeKey The key of an existing flow scope.
   * @param elementId    The BPMN element ID.
   * @return The element instance key.
   * @throws AssertionError If no such element instance exists.
   */
  public long getElementInstanceKey(long flowScopeKey, String elementId) {
    return getElementInstance(flowScopeKey, elementId).getElementInstanceKey();
  }

  /**
   * Returns the flow scope key of an element instance.
   *
   * @param elementInstanceKey The key of an existing element instance.
   * @return The flow scope key.
   * @throws AssertionError If the flow scope key could not be determined.
   */
  public long getFlowScopeKey(long elementInstanceKey) {
    return await(() -> {
      var multiInstanceBodies = client.newElementInstanceSearchRequest()
          .execute()
          .items()
          .stream()
          .filter(elementInstance -> elementInstance.getType() == ElementInstanceType.MULTI_INSTANCE_BODY)
          .collect(Collectors.toList());

      for (ElementInstance multiInstanceBody : multiInstanceBodies) {
        var isElementInstance = client.newElementInstanceSearchRequest()
            .filter(filter -> filter.elementInstanceScopeKey(multiInstanceBody.getElementInstanceKey()))
            .execute()
            .items()
            .stream()
            .anyMatch(elementInstance -> elementInstance.getElementInstanceKey() == elementInstanceKey);

        if (isElementInstance) {
          return multiInstanceBody.getElementInstanceKey();
        }
      }

      var message = String.format("failed to get flow scope key of element instance %d", elementInstanceKey);
      throw new AssertionError(message);
    });
  }

  /**
   * Returns the process instance key of the given flow scope. If the flow scope is the root scope, the key is the process instance key.
   *
   * @param flowScopeKey The key of an existing flow scope.
   * @return The process instance key.
   * @throws AssertionError If the process instance key could not be determined.
   */
  public long getProcessInstanceKey(long flowScopeKey) {
    return await(() -> {
      try {
        return client.newProcessInstanceGetRequest(flowScopeKey).execute().getProcessInstanceKey();
      } catch (ProblemException e) {
        // ignore
      }

      try {
        return client.newElementInstanceGetRequest(flowScopeKey).execute().getProcessInstanceKey();
      } catch (ProblemException e) {
        throw new AssertionError(e.getMessage());
      }
    });
  }

  public CamundaProcessTestContext getProcessTestContext() {
    return processTestContext;
  }

  /**
   * Checks if a BPMN element has been passed within the given flow scope.
   *
   * @param flowScopeKey The key of an existing flow scope.
   * @param elementId    The BPMN element ID to test.
   * @throws AssertionError If the BPMN element has not been passed (element instance does not exist or is not completed).
   */
  public void hasPassed(long flowScopeKey, String elementId) {
    awaitilityBehavior.untilAsserted(() -> {
      var elementInstances = client.newElementInstanceSearchRequest()
          .filter(filter -> filter.elementInstanceScopeKey(flowScopeKey).elementId(elementId))
          .execute()
          .items();

      if (elementInstances.isEmpty()) {
        var message = String.format("expected flow scope %d to have element %s, but has not", flowScopeKey, elementId);
        throw new AssertionError(message);
      }

      if (elementInstances.get(0).getState() != ElementInstanceState.COMPLETED) {
        var message = String.format("expected flow scope %d to have passed element %s, but has not", flowScopeKey, elementId);
        throw new AssertionError(message);
      }
    });
  }

  /**
   * Checks if a BPMN element has been terminated within the given flow scope.
   * <br>
   * If the flow scope is a multi instance body, the flow scope's state is asserted.
   *
   * @param flowScopeKey The key of an existing flow scope.
   * @param elementId    The BPMN element ID to test.
   * @throws AssertionError If the BPMN element has not been terminated (element instance does not exist or is not terminated).
   */
  public void hasTerminated(long flowScopeKey, String elementId) {
    awaitilityBehavior.untilAsserted(() -> {
      ElementInstance flowScope;
      try {
        flowScope = client.newElementInstanceGetRequest(flowScopeKey).execute();
      } catch (ProblemException e) {
        flowScope = null; // if flow scope is process instance
      }

      if (flowScope != null && flowScope.getType() == ElementInstanceType.MULTI_INSTANCE_BODY) {
        if (flowScope.getState() != ElementInstanceState.TERMINATED) {
          var message = String.format("expected multi instance element %s with ID %d to be terminated, but is not", flowScope.getElementId(), flowScopeKey);
          throw new AssertionError(message);
        }
        return;
      }

      var elementInstances = client.newElementInstanceSearchRequest()
          .filter(filter -> filter.elementInstanceScopeKey(flowScopeKey).elementId(elementId))
          .execute()
          .items();

      if (elementInstances.isEmpty()) {
        var message = String.format("expected flow scope %d to have element %s, but has not", flowScopeKey, elementId);
        throw new AssertionError(message);
      }

      if (elementInstances.get(0).getState() != ElementInstanceState.TERMINATED) {
        var message = String.format("expected flow scope %d to have terminated element %s, but has not", flowScopeKey, elementId);
        throw new AssertionError(message);
      }
    });
  }

  /**
   * Checks if the given process instance is completed.
   *
   * @param processInstanceKey The key of an existing process instance.
   * @throws AssertionError If the process instance is not completed.
   */
  public void isCompleted(long processInstanceKey) {
    var processInstanceSelector = ProcessInstanceSelectors.byKey(processInstanceKey);
    CamundaAssert.assertThatProcessInstance(processInstanceSelector).isCompleted();
  }

  /**
   * Checks if a flow is waiting at a specific BPMN element.
   *
   * @param flowScopeKey The key of an existing flow scope.
   * @param elementId    The BPMN element to test.
   * @throws AssertionError If the flow is not waiting at the BPMN element.
   */
  public void isWaitingAt(long flowScopeKey, String elementId) {
    awaitilityBehavior.untilAsserted(() -> {
      var elementInstances = client.newElementInstanceSearchRequest()
          .filter(filter -> filter.elementInstanceScopeKey(flowScopeKey).elementId(elementId))
          .execute()
          .items();

      if (elementInstances.isEmpty()) {
        var message = String.format("expected flow scope %d to have element %s, but has not", flowScopeKey, elementId);
        throw new AssertionError(message);
      }

      // do not compare the element instance state
      // since a job handler might already have completed a job
      // which results in a COMPLETED state
    });
  }

  /**
   * Awaits that the given callable returns a specific value after one or multiple tries until a timeout is reached.
   *
   * @param callable The callable, calling Camunda using the {@link CamundaClient}.
   * @param <R>      The return type.
   * @return A desired value.
   * @see AwaitilityBehavior
   */
  <R> R await(Callable<R> callable) {
    var holder = new Holder<R>();
    awaitilityBehavior.untilAsserted(callable, holder);
    return holder.get();
  }

  /**
   * Get the element instance within the given flow scope and ID.
   *
   * @param flowScopeKey The key of an existing flow scope.
   * @param elementId    The BPMN element ID.
   * @throws AssertionError If the BPMN element could not be found.
   */
  ElementInstance getElementInstance(long flowScopeKey, String elementId) {
    return await(() -> {
      var elementInstances = client.newElementInstanceSearchRequest()
          .filter(filter -> filter.elementInstanceScopeKey(flowScopeKey).elementId(elementId))
          .execute()
          .items();

      if (elementInstances.isEmpty()) {
        var message = String.format("expected flow scope %d to have element %s, but has not", flowScopeKey, elementId);
        throw new AssertionError(message);
      }

      return elementInstances.get(elementInstances.size() - 1);
    });
  }

  Job getJob(long flowScopeKey, String elementId) {
    return await(() -> {
      var elementInstances = client.newElementInstanceSearchRequest()
          .filter(filter -> filter.elementInstanceScopeKey(flowScopeKey).elementId(elementId))
          .execute()
          .items();

      if (elementInstances.isEmpty()) {
        var message = String.format("expected flow scope %d to have element %s, but has not", flowScopeKey, elementId);
        throw new AssertionError(message);
      }

      var elementInstance = elementInstances.get(elementInstances.size() - 1);

      var job = client.newJobSearchRequest()
          .filter(filter -> filter.elementInstanceKey(elementInstance.getElementInstanceKey()))
          .execute()
          .singleItem();

      if (job == null) {
        var message = String.format("element %s of flow scope %d has no job", elementId, flowScopeKey);
        throw new AssertionError(message);
      }

      return job;
    });
  }

  MessageSubscription getMessageSubscription(long flowScopeKey, String elementId, String attachedTo) {
    ElementInstance flowScope = null;
    if (attachedTo != null) {
      flowScope = await(() -> {
        try {
          return client.newElementInstanceGetRequest(flowScopeKey).execute();
        } catch (ProblemException e) {
          return null; // if flow scope is process instance
        }
      });
    }

    if (flowScope != null && flowScope.getType() == ElementInstanceType.MULTI_INSTANCE_BODY) {
      return await(() -> {
        var messageSubscription = client.newMessageSubscriptionSearchRequest()
            .filter(filter -> filter.elementInstanceKey(flowScopeKey))
            .execute()
            .singleItem();

        if (messageSubscription == null) {
          var message = String.format("multi instance element %s with ID %d has no message subscription", attachedTo, flowScopeKey);
          throw new AssertionError(message);
        }

        return messageSubscription;
      });
    }

    ElementInstance elementInstance;
    if (attachedTo == null) {
      elementInstance = getElementInstance(flowScopeKey, elementId);
    } else {
      elementInstance = getElementInstance(flowScopeKey, attachedTo);
    }

    return await(() -> {
      var messageSubscription = client.newMessageSubscriptionSearchRequest()
          .filter(filter -> filter.elementInstanceKey(elementInstance.getElementInstanceKey()))
          .execute()
          .singleItem();

      if (messageSubscription == null) {
        var message = String.format("element %s of flow scope %d has no message subscription", elementInstance.getElementId(), flowScopeKey);
        throw new AssertionError(message);
      }

      return messageSubscription;
    });
  }

  /**
   * Generic value holder, needed for {@link AwaitilityBehavior#untilAsserted(Callable, Consumer)}.
   *
   * @param <T> Any type.
   */
  static class Holder<T> implements Consumer<T> {

    private T value;

    @Override
    public void accept(T value) {
      this.value = value;
    }

    public T get() {
      return value;
    }
  }
}
