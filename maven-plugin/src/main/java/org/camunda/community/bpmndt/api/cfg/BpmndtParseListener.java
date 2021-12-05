package org.camunda.community.bpmndt.api.cfg;

import java.util.function.Supplier;

import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.community.bpmndt.api.TestCaseInstance;

/**
 * Custom BPMN parse listener that:
 * 
 * 1. Overrides {@link CallActivityBehavior}s to make test cases independent of sub processes.
 * 
 * 2. Enables asynchronous continuation for multi instance activities.
 */
public class BpmndtParseListener extends AbstractBpmnParseListener {

  /** Activity ID suffix of multi instance scopes. */
  private static final String MULTI_INSTANCE_SCOPE_SUFFIX = "#" + ActivityTypes.MULTI_INSTANCE_BODY;

  /** The current test case instance. */
  private TestCaseInstance instance;

  protected TestCaseInstance getInstance() {
    return instance;
  }

  @Override
  public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
    CallActivityBehavior behavior = (CallActivityBehavior) activity.getActivityBehavior();

    activity.setActivityBehavior(new CustomCallActivityBehavior(this::getInstance, behavior));

    // needed to verify the state before the call activity is executed
    activity.setAsyncBefore(true);

    setMultiInstanceAsync(scope, activity);
  }

  @Override
  public void parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity) {
    setMultiInstanceAsync(scope, activity);
  }

  @Override
  public void parseManualTask(Element manualTaskElement, ScopeImpl scope, ActivityImpl activity) {
    setMultiInstanceAsync(scope, activity);
  }

  @Override
  public void parseScriptTask(Element scriptTaskElement, ScopeImpl scope, ActivityImpl activity) {
    setMultiInstanceAsync(scope, activity);
  }

  @Override
  public void parseSendTask(Element sendTaskElement, ScopeImpl scope, ActivityImpl activity) {
    setMultiInstanceAsync(scope, activity);
  }

  @Override
  public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
    setMultiInstanceAsync(scope, activity);
  }

  @Override
  public void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity) {
    setMultiInstanceAsync(scope, activity);
  }

  @Override
  public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
    setMultiInstanceAsync(scope, activity);
  }

  /**
   * Sets a reference to the current test case instance.
   * 
   * @param instance The current instance.
   */
  public void setInstance(TestCaseInstance instance) {
    this.instance = instance;
  }

  /**
   * Sets the {@code asyncBefore} and {@code asyncAfter} flag of the given activity to {@code true},
   * if the surrounding scope is a multi instance activity.
   * 
   * @param scope The surrounding scope.
   * 
   * @param activity The current activity.
   */
  protected void setMultiInstanceAsync(ScopeImpl scope, ActivityImpl activity) {
    if (!scope.isSubProcessScope() && scope.getId().endsWith(MULTI_INSTANCE_SCOPE_SUFFIX)) {
      activity.setAsyncBefore(true);
      activity.setAsyncAfter(true);
    }
  }

  /**
   * Custom behavior to stub call activities for isolated testing.
   */
  private static class CustomCallActivityBehavior extends CallActivityBehavior {

    /** Current test case instance supplier. */
    private final Supplier<TestCaseInstance> instanceSupplier;

    /** The activity's original behavior. */
    private final CallActivityBehavior behavior;

    private CustomCallActivityBehavior(Supplier<TestCaseInstance> instanceSupplier, CallActivityBehavior behavior) {
      this.instanceSupplier = instanceSupplier;
      this.behavior = behavior;
    }

    @Override
    public void execute(ActivityExecution execution) throws Exception {
      TestCaseInstance instance = instanceSupplier.get();

      boolean shouldLeave = instance != null ? instance.execute(execution, behavior) : true;

      if (shouldLeave) {
        leave(execution);
      }
    }
  }
}
