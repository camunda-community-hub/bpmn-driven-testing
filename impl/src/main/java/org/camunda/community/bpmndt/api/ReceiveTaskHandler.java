package org.camunda.community.bpmndt.api;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.EventSubscriptionQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * Fluent API to handle receive tasks.
 */
public class ReceiveTaskHandler {

  private final ProcessEngine processEngine;
  private final String activityId;
  private final String eventName;

  private final VariableMap variables;

  private BiConsumer<ProcessInstanceAssert, EventSubscription> verifier;

  private Consumer<EventSubscription> action;

  public ReceiveTaskHandler(ProcessEngine processEngine, String activityId, String eventName) {
    this.processEngine = processEngine;
    this.activityId = activityId;
    this.eventName = eventName;

    variables = Variables.createVariables();

    action = this::eventReceived;
  }

  protected void apply(ProcessInstance pi) {
    RuntimeService runtimeService = processEngine.getRuntimeService();

    EventSubscriptionQuery eventSubscriptionQuery = runtimeService.createEventSubscriptionQuery().activityId(activityId);

    if (eventName != null) {
      eventSubscriptionQuery.eventName(eventName);
    }

    EventSubscription eventSubscription = eventSubscriptionQuery.singleResult();
    if (eventSubscription == null) {
      throw new AssertionError(String.format("No event subscription found for activity '%s'", activityId));
    }

    if (verifier != null) {
      verifier.accept(ProcessEngineTests.assertThat(pi), eventSubscription);
    }

    if (action != null) {
      action.accept(eventSubscription);
    }
  }

  /**
   * Customizes the handler, using the given {@link Consumer} function. This method can be used to apply a common customization needed for different test
   * cases.
   *
   * <pre>
   * tc.handleReceiveTask().customize(this::prepareEvent);
   * </pre>
   *
   * @param customizer A function that accepts a {@link ReceiveTaskHandler}.
   * @return The handler.
   */
  public ReceiveTaskHandler customize(Consumer<ReceiveTaskHandler> customizer) {
    if (customizer != null) {
      customizer.accept(this);
    }
    return this;
  }

  /**
   * Executes a custom action that handles the receive task, when the process instance is waiting at the corresponding activity.
   *
   * @param action A specific action that accepts an {@link EventSubscription}.
   * @throws IllegalArgumentException if action is {@code null}.
   */
  public void execute(Consumer<EventSubscription> action) {
    if (action == null) {
      throw new IllegalArgumentException("action is null");
    }
    this.action = action;
  }

  /**
   * Continues the waiting execution with an action that calls {@code messageEventReceived}. Please note: this is the default behavior.
   *
   * @see RuntimeService#messageEventReceived(String, String, Map)
   */
  public void eventReceived() {
    action = this::eventReceived;
  }

  protected void eventReceived(EventSubscription eventSubscription) {
    RuntimeService runtimeService = processEngine.getRuntimeService();

    runtimeService.messageEventReceived(eventSubscription.getEventName(), eventSubscription.getExecutionId(), variables);
  }

  /**
   * Determines if the receive task is waiting for a boundary message, signal or timer event.
   *
   * @return {@code true}, if it is waiting for a boundary event. {@code false}, if not.
   */
  public boolean isWaitingForBoundaryEvent() {
    return action == null;
  }

  /**
   * Verifies the receive task's waiting state.
   *
   * @param verifier Verifier that accepts an {@link ProcessInstanceAssert} instance and an {@link EventSubscription}.
   * @return The handler.
   */
  public ReceiveTaskHandler verify(BiConsumer<ProcessInstanceAssert, EventSubscription> verifier) {
    this.verifier = verifier;
    return this;
  }

  /**
   * Applies no action at the receive task's wait state. This is required to wait for events ( message, signal or timer events) that are attached as boundary
   * events on the activity itself or on the surrounding scope (e.g. embedded subprocess).
   */
  public void waitForBoundaryEvent() {
    action = null;
  }

  /**
   * Sets a variable, which is passed to the execution when the default behavior is used.
   *
   * @param name  The name of the variable.
   * @param value The variable's value.
   * @return The handler.
   */
  public ReceiveTaskHandler withVariable(String name, Object value) {
    variables.putValue(name, value);
    return this;
  }

  /**
   * Sets variables, which are passed to the execution when the default behavior is used.
   *
   * @param variables A map of variables to set.
   * @return The handler.
   */
  public ReceiveTaskHandler withVariables(Map<String, Object> variables) {
    this.variables.putAll(variables);
    return this;
  }

  /**
   * Sets a typed variable, which is passed to the execution when the default behavior is used.
   *
   * @param name  The name of the variable.
   * @param value The variable's typed value.
   * @return The handler.
   */
  public ReceiveTaskHandler withVariableTyped(String name, TypedValue value) {
    variables.putValueTyped(name, value);
    return this;
  }
}
