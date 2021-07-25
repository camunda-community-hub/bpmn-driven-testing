package org.camunda.community.bpmndt.api;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * Fluent API to handle intermediate (message and signal) catch events.
 */
public class IntermediateCatchEventHandler {

  private final ProcessEngine processEngine;
  private final String activityId;
  private final String eventName;

  private final VariableMap variables;

  private BiConsumer<ProcessInstanceAssert, EventSubscription> verifier;

  private Consumer<EventSubscription> action;

  public IntermediateCatchEventHandler(ProcessEngine processEngine, String activityId, String eventName) {
    this.processEngine = processEngine;
    this.activityId = activityId;
    this.eventName = eventName;

    variables = Variables.createVariables();

    action = this::eventReceived;
  }

  protected void apply(ProcessInstance pi) {
    EventSubscription eventSubscription = processEngine.getRuntimeService().createEventSubscriptionQuery()
        .processInstanceId(pi.getId())
        .activityId(activityId)
        .eventName(eventName)
        .singleResult();

    if (verifier != null) {
      verifier.accept(ProcessEngineTests.assertThat(pi), eventSubscription);
    }

    action.accept(eventSubscription);
  }

  /**
   * Notifies the waiting execution with an action that calls {@code messageEventReceived} or
   * {@code signalEventReceived} (depeding on the actual event type).
   * 
   * @see RuntimeService#messageEventReceived(String, String, java.util.Map)
   * @see RuntimeService#signalEventReceived(String, String, java.util.Map)
   */
  public void eventReceived() {
    action = this::eventReceived;
  }

  /**
   * Notifies the waiting execution with a custom action that is executed when the handler is applied.
   * 
   * @param action A specific action that accepts an {@link EventSubscription}.
   */
  public void eventReceived(Consumer<EventSubscription> action) {
    this.action = action;
  }

  protected void eventReceived(EventSubscription eventSubscription) {
    RuntimeService runtimeService = processEngine.getRuntimeService();

    String eventType = eventSubscription.getEventType();
    if (eventType.equals(EventType.MESSAGE.name())) {
      runtimeService.messageEventReceived(eventSubscription.getEventName(), eventSubscription.getExecutionId(), variables);
    } else if (eventType.equals(EventType.SIGNAL.name())) {
      runtimeService.signalEventReceived(eventSubscription.getEventName(), eventSubscription.getExecutionId(), variables);
    } else {
      throw new RuntimeException(String.format("Unsupported event type '%s'", eventType));
    }
  }

  /**
   * Verifies the intermediate catch events's waiting state.
   * 
   * @param verifier Verifier that accepts an {@link ProcessInstanceAssert} instance and an
   *        {@link EventSubscription}.
   * 
   * @return The handler.
   */
  public IntermediateCatchEventHandler verify(BiConsumer<ProcessInstanceAssert, EventSubscription> verifier) {
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
  public IntermediateCatchEventHandler withVariable(String name, Object value) {
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
  public IntermediateCatchEventHandler withVariableTyped(String name, TypedValue value) {
    variables.putValueTyped(name, value);
    return this;
  }
}
