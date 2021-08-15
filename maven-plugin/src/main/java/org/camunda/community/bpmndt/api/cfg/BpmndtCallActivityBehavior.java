package org.camunda.community.bpmndt.api.cfg;

import org.camunda.bpm.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.community.bpmndt.api.TestCaseInstance;

/**
 * Custom behavior to stub call activities for isolated testing.
 */
public class BpmndtCallActivityBehavior extends CallActivityBehavior {

  /** The related test case instance. */
  private final TestCaseInstance instance;

  /** The activity's original behavior. */
  private final CallActivityBehavior behavior;

  public BpmndtCallActivityBehavior(TestCaseInstance instance, CallActivityBehavior behavior) {
    this.instance = instance;
    this.behavior = behavior;
  }

  @Override
  public void execute(ActivityExecution execution) throws Exception {
    boolean shouldLeave = instance.execute(execution, behavior);

    if (shouldLeave) {
      leave(execution);
    }
  }
}
