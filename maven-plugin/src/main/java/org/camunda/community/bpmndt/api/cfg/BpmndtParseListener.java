package org.camunda.community.bpmndt.api.cfg;

import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.community.bpmndt.api.TestCaseInstance;

/**
 * Custom post BPMN parse listener that overrides {@link CallActivityBehavior}s to make test cases
 * independent of sub processes and enables asynchronous continuation for multi instance activities.
 */
public class BpmndtParseListener extends AbstractBpmnParseListener {

  /** Activity ID suffix of multi instance scopes. */
  private final String multiInstanceScopeSuffix;

  public BpmndtParseListener() {
    multiInstanceScopeSuffix = "#" + ActivityTypes.MULTI_INSTANCE_BODY;
  }

  /** The current test case instance. */
  private TestCaseInstance instance;

  @Override
  public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
    if (instance == null) {
      return;
    }

    CallActivityBehavior behavior = (CallActivityBehavior) activity.getActivityBehavior();

    activity.setActivityBehavior(new BpmndtCallActivityBehavior(instance, behavior));

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
    if (!scope.isSubProcessScope() && scope.getId().endsWith(multiInstanceScopeSuffix)) {
      activity.setAsyncBefore(true);
      activity.setAsyncAfter(true);
    }
  }
}
