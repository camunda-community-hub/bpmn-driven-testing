package org.camunda.community.bpmndt.model;

import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_ERROR_EVENT_DEFINITION;
import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_ESCALATION_EVENT_DEFINITION;
import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_LINK_EVENT_DEFINITION;
import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_MESSAGE_EVENT_DEFINITION;
import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_SIGNAL_EVENT_DEFINITION;
import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_TIMER_EVENT_DEFINITION;

import java.util.Collection;

import io.camunda.zeebe.model.bpmn.instance.CatchEvent;
import io.camunda.zeebe.model.bpmn.instance.ErrorEventDefinition;
import io.camunda.zeebe.model.bpmn.instance.EscalationEventDefinition;
import io.camunda.zeebe.model.bpmn.instance.EventDefinition;
import io.camunda.zeebe.model.bpmn.instance.MessageEventDefinition;
import io.camunda.zeebe.model.bpmn.instance.SignalEventDefinition;
import io.camunda.zeebe.model.bpmn.instance.ThrowEvent;
import io.camunda.zeebe.model.bpmn.instance.TimerEventDefinition;

/**
 * BPMN event support allows easier working with {@link EventDefinition}s of {@link CatchEvent} or {@link ThrowEvent} nodes.
 */
public class BpmnEventSupport {

  private final EventDefinition eventDefinition;

  private ThrowEvent throwEvent;

  public BpmnEventSupport(CatchEvent event) {
    this(event.getEventDefinitions());
  }

  public BpmnEventSupport(ThrowEvent event) {
    this(event.getEventDefinitions());

    throwEvent = event;
  }

  private BpmnEventSupport(Collection<EventDefinition> eventDefinitions) {
    eventDefinition = eventDefinitions.stream().findFirst().orElse(null);
  }

  public String getErrorCode() {
    var errorEventDefinition = getErrorDefinition();
    if (errorEventDefinition != null) {
      var error = errorEventDefinition.getError();
      return error != null ? error.getErrorCode() : null;
    } else {
      return null;
    }
  }

  public ErrorEventDefinition getErrorDefinition() {
    return (ErrorEventDefinition) eventDefinition;
  }

  public String getEscalationCode() {
    var escalationEventDefinition = getEscalationDefinition();
    if (escalationEventDefinition != null) {
      var escalation = escalationEventDefinition.getEscalation();
      return escalation != null ? escalation.getEscalationCode() : null;
    } else {
      return null;
    }
  }

  public EscalationEventDefinition getEscalationDefinition() {
    return (EscalationEventDefinition) eventDefinition;
  }

  public MessageEventDefinition getMessageDefinition() {
    return (MessageEventDefinition) eventDefinition;
  }

  public SignalEventDefinition getSignalDefinition() {
    return (SignalEventDefinition) eventDefinition;
  }

  public TimerEventDefinition getTimerDefinition() {
    return (TimerEventDefinition) eventDefinition;
  }

  private boolean is(String typeName) {
    if (eventDefinition != null) {
      return eventDefinition.getElementType().getTypeName().equals(typeName);
    } else {
      return false;
    }
  }

  public boolean isError() {
    return is(BPMN_ELEMENT_ERROR_EVENT_DEFINITION);
  }

  public boolean isEscalation() {
    return is(BPMN_ELEMENT_ESCALATION_EVENT_DEFINITION);
  }

  public boolean isLink() {
    return is(BPMN_ELEMENT_LINK_EVENT_DEFINITION);
  }

  public boolean isMessage() {
    return is(BPMN_ELEMENT_MESSAGE_EVENT_DEFINITION);
  }

  public boolean isSignal() {
    return is(BPMN_ELEMENT_SIGNAL_EVENT_DEFINITION);
  }

  public boolean isTimer() {
    return is(BPMN_ELEMENT_TIMER_EVENT_DEFINITION);
  }
}
