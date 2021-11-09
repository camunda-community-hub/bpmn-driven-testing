package org.camunda.community.bpmndt.api;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.EventSubscriptionQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * Fluent API to handle conditional, message and signal intermediate catch or boundary events.<br>
 * Please note: Since conditional events rely on an expression to evaluate true, their wait state
 * cannot be handled automatically - set variables or use {@link #execute(Consumer)} with a custom
 * action, if the variable event is {@code delete}.
 */
public class EventHandler {

  private final ProcessEngine processEngine;
  private final String activityId;
  private final String eventName;

  private final VariableMap variables;

  private BiConsumer<ProcessInstanceAssert, EventSubscription> verifier;

  private Consumer<EventSubscription> action;

  public EventHandler(ProcessEngine processEngine, String activityId, String eventName) {
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

    action.accept(eventSubscription);
  }

  /**
   * Executes a custom action that handles the intermediate catch event.
   * 
   * @param action A specific action that accepts an {@link EventSubscription}.
   */
  public void execute(Consumer<EventSubscription> action) {
    this.action = action;
  }

  /**
   * Continues the waiting execution with an action that calls {@code messageEventReceived},
   * {@code signalEventReceived} or {@code setVariables} (depending on the actual event type).
   * 
   * @see RuntimeService#messageEventReceived(String, String, java.util.Map)
   * @see RuntimeService#signalEventReceived(String, String, java.util.Map)
   * @see RuntimeService#setVariables(String, java.util.Map)
   */
  public void eventReceived() {
    action = this::eventReceived;
  }

  protected void eventReceived(EventSubscription eventSubscription) {
    RuntimeService runtimeService = processEngine.getRuntimeService();

    String eventType = eventSubscription.getEventType();
    if (eventType.equals(EventType.CONDITONAL.name())) {
      runtimeService.setVariables(eventSubscription.getExecutionId(), variables);
    } else if (eventType.equals(EventType.MESSAGE.name())) {
      runtimeService.messageEventReceived(eventSubscription.getEventName(), eventSubscription.getExecutionId(), variables);
    } else if (eventType.equals(EventType.SIGNAL.name())) {
      runtimeService.signalEventReceived(eventSubscription.getEventName(), eventSubscription.getExecutionId(), variables);
    } else {
      throw new RuntimeException(String.format("Unsupported event type '%s'", eventType));
    }
  }

  /**
   * Verifies the events's waiting state.
   * 
   * @param verifier Verifier that accepts an {@link ProcessInstanceAssert} instance and an
   *        {@link EventSubscription}.
   * 
   * @return The handler.
   */
  public EventHandler verify(BiConsumer<ProcessInstanceAssert, EventSubscription> verifier) {
    this.verifier = verifier;
    return this;
  }

  /**
   * Sets a variable, which is passed to the execution when the default action is used.
   * 
   * @param name The name of the variable.
   * 
   * @param value The variable's value.
   * 
   * @return The handler.
   */
  public EventHandler withVariable(String name, Object value) {
    variables.putValue(name, value);
    return this;
  }

  /**
   * Sets a typed variable, which is passed to the execution when the default action is used.
   * 
   * @param name The name of the variable.
   * 
   * @param value The variable's typed value.
   * 
   * @return The handler.
   */
  public EventHandler withVariableTyped(String name, TypedValue value) {
    variables.putValueTyped(name, value);
    return this;
  }
}
