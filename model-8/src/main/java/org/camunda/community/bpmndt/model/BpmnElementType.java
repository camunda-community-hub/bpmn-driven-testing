package org.camunda.community.bpmndt.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Handled BPMN element types.
 */
public enum BpmnElementType {

  CALL_ACTIVITY,
  ERROR_BOUNDARY,
  ESCALATION_BOUNDARY,
  EVENT_BASED_GATEWAY,
  LINK_THROW,
  MESSAGE_BOUNDARY,
  MESSAGE_CATCH,
  OUTBOUND_CONNECTOR,
  SERVICE_TASK,
  SIGNAL_BOUNDARY,
  SIGNAL_CATCH,
  TIMER_BOUNDARY,
  TIMER_CATCH,
  USER_TASK,

  /**
   * Other element, which need no special handling (e.g. start event or manual task).
   */
  OTHER,

  /**
   * Special type for handling scopes (e.g. embedded sub processes) as elements.
   */
  SCOPE;

  private static final Set<BpmnElementType> BOUNDARY_EVENT_TYPES;

  static {
    BOUNDARY_EVENT_TYPES = EnumSet.noneOf(BpmnElementType.class);
    BOUNDARY_EVENT_TYPES.add(ERROR_BOUNDARY);
    BOUNDARY_EVENT_TYPES.add(ESCALATION_BOUNDARY);
    BOUNDARY_EVENT_TYPES.add(MESSAGE_BOUNDARY);
    BOUNDARY_EVENT_TYPES.add(SIGNAL_BOUNDARY);
    BOUNDARY_EVENT_TYPES.add(TIMER_BOUNDARY);
  }

  /**
   * Determines if the type refers to an BPMN boundary event element.
   *
   * @return {@code true}, if the type is a boundary event. Otherwise {@code false}.
   */
  public boolean isBoundaryEvent() {
    return BOUNDARY_EVENT_TYPES.contains(this);
  }
}
