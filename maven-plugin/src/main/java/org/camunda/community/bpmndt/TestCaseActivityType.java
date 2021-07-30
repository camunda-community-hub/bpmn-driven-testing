package org.camunda.community.bpmndt;

/**
 * Possible types of {@link TestCaseActivity} instances.
 */
public enum TestCaseActivityType {

  CALL_ACTIVITY,
  EXTERNAL_TASK,
  /** Intermediate message catch event. */
  MESSAGE_CATCH_EVENT,
  /** Intermediate signal catch event. */
  SIGNAL_CATCH_EVENT,
  /** Intermediate timer catch event. */
  TIMER_CATCH_EVENT,
  USER_TASK,

  /** Other activities, which need no special handling (e.g. start event). */
  OTHER
}
