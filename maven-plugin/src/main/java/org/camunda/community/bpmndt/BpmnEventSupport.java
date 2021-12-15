package org.camunda.community.bpmndt;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_CONDITIONAL_EVENT_DEFINITION;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_ERROR_EVENT_DEFINITION;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_ESCALATION_EVENT_DEFINITION;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_MESSAGE_EVENT_DEFINITION;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_SIGNAL_EVENT_DEFINITION;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_TIMER_EVENT_DEFINITION;

import java.util.Collection;

import org.camunda.bpm.model.bpmn.instance.CatchEvent;
import org.camunda.bpm.model.bpmn.instance.ConditionalEventDefinition;
import org.camunda.bpm.model.bpmn.instance.Error;
import org.camunda.bpm.model.bpmn.instance.ErrorEventDefinition;
import org.camunda.bpm.model.bpmn.instance.Escalation;
import org.camunda.bpm.model.bpmn.instance.EscalationEventDefinition;
import org.camunda.bpm.model.bpmn.instance.EventDefinition;
import org.camunda.bpm.model.bpmn.instance.Message;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.camunda.bpm.model.bpmn.instance.Signal;
import org.camunda.bpm.model.bpmn.instance.SignalEventDefinition;
import org.camunda.bpm.model.bpmn.instance.ThrowEvent;
import org.camunda.bpm.model.bpmn.instance.TimerEventDefinition;

/**
 * BPMN event support, allows easier working with {@link EventDefinition}s (conditional, error,
 * escalation, message, signal or timer) of {@link CatchEvent} or {@link ThrowEvent} instances.
 */
public class BpmnEventSupport {

  private final EventDefinition eventDefinition;

  public BpmnEventSupport(CatchEvent event) {
    this(event.getEventDefinitions());
  }

  public BpmnEventSupport(ThrowEvent event) {
    this(event.getEventDefinitions());
  }

  protected BpmnEventSupport(Collection<EventDefinition> eventDefinitions) {
    eventDefinition = eventDefinitions.stream().findFirst().orElse(null);
  }

  public ConditionalEventDefinition getConditionalDefinition() {
    return (ConditionalEventDefinition) eventDefinition;
  }

  public Error getError() {
    return getErrorDefinition().getError();
  }

  public ErrorEventDefinition getErrorDefinition() {
    return (ErrorEventDefinition) eventDefinition;
  }

  public Escalation getEscalation() {
    return getEscalationDefinition().getEscalation();
  }

  public EscalationEventDefinition getEscalationDefinition() {
    return (EscalationEventDefinition) eventDefinition;
  }

  public Message getMessage() {
    return getMessageDefinition().getMessage();
  }

  public MessageEventDefinition getMessageDefinition() {
    return (MessageEventDefinition) eventDefinition;
  }

  public Signal getSignal() {
    return getSignalDefinition().getSignal();
  }

  public SignalEventDefinition getSignalDefinition() {
    return (SignalEventDefinition) eventDefinition;
  }

  public TimerEventDefinition getTimerDefinition() {
    return (TimerEventDefinition) eventDefinition;
  }

  protected boolean is(String typeName) {
    if (eventDefinition != null) {
      return eventDefinition.getElementType().getTypeName().equals(typeName);
    } else {
      return false;
    }
  }

  public boolean isConditional() {
    return is(BPMN_ELEMENT_CONDITIONAL_EVENT_DEFINITION);
  }

  public boolean isError() {
    return is(BPMN_ELEMENT_ERROR_EVENT_DEFINITION);
  }

  public boolean isEscalation() {
    return is(BPMN_ELEMENT_ESCALATION_EVENT_DEFINITION);
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
