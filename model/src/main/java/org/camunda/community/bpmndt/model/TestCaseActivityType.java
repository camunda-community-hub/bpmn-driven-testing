package org.camunda.community.bpmndt.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Handled activity types.
 */
public enum TestCaseActivityType {

  CALL_ACTIVITY,
  CONDITIONAL_BOUNDARY,
  CONDITIONAL_CATCH,
  ERROR_BOUNDARY,
  ESCALATION_BOUNDARY,
  EVENT_BASED_GATEWAY,
  EXTERNAL_TASK,
  LINK_THROW,
  MESSAGE_BOUNDARY,
  MESSAGE_CATCH,
  RECEIVE_TASK,
  SIGNAL_BOUNDARY,
  SIGNAL_CATCH,
  TIMER_BOUNDARY,
  TIMER_CATCH,
  USER_TASK,

  /** Other activities, which need no special handling (e.g. start event or service task). */
  OTHER,

  /** Special type for handling scopes (e.g. embedded sub processes) as activities. */
  SCOPE;

  private static final Set<TestCaseActivityType> BOUNDARY_EVENT_TYPES;
  private static final Set<TestCaseActivityType> WAIT_STATE_TYPES;

  static {
    BOUNDARY_EVENT_TYPES = EnumSet.noneOf(TestCaseActivityType.class);
    BOUNDARY_EVENT_TYPES.add(CONDITIONAL_BOUNDARY);
    BOUNDARY_EVENT_TYPES.add(ERROR_BOUNDARY);
    BOUNDARY_EVENT_TYPES.add(ESCALATION_BOUNDARY);
    BOUNDARY_EVENT_TYPES.add(MESSAGE_BOUNDARY);
    BOUNDARY_EVENT_TYPES.add(SIGNAL_BOUNDARY);
    BOUNDARY_EVENT_TYPES.add(TIMER_BOUNDARY);

    WAIT_STATE_TYPES = EnumSet.noneOf(TestCaseActivityType.class);
    WAIT_STATE_TYPES.add(CONDITIONAL_CATCH);
    WAIT_STATE_TYPES.add(EVENT_BASED_GATEWAY);
    WAIT_STATE_TYPES.add(EXTERNAL_TASK);
    WAIT_STATE_TYPES.add(MESSAGE_CATCH);
    WAIT_STATE_TYPES.add(RECEIVE_TASK);
    WAIT_STATE_TYPES.add(SIGNAL_CATCH);
    WAIT_STATE_TYPES.add(TIMER_CATCH);
    WAIT_STATE_TYPES.add(USER_TASK);
  }

  /**
   * Determines if the type refers to an BPMN boundary event element.
   * 
   * @return {@code true}, if the type is a boundary event. Otherwise {@code false}.
   */
  public boolean isBoundaryEvent() {
    return BOUNDARY_EVENT_TYPES.contains(this);
  }

  /**
   * Determines if the type refers to an BPMN element that causes a process instance to wait.
   * 
   * @return {@code true}, if the type is a wait state. Otherwise {@code false}.
   */
  public boolean isWaitState() {
    return WAIT_STATE_TYPES.contains(this);
  }
}
