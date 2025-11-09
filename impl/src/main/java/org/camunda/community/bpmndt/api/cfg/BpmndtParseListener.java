package org.camunda.community.bpmndt.api.cfg;

import java.util.List;

import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.CallActivityTestBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.community.bpmndt.api.TestCaseInstance;

/**
 * Custom BPMN parse listener that:
 * <br>
 * 1. Overrides {@link CallActivityBehavior}s to make it testable.
 * <br>
 * 2. Enables asynchronous continuation for multi instance activities.
 */
public class BpmndtParseListener extends AbstractBpmnParseListener {

  /**
   * Activity ID suffix of multi instance scopes.
   */
  private static final String MULTI_INSTANCE_SCOPE_SUFFIX = "#" + ActivityTypes.MULTI_INSTANCE_BODY;

  /**
   * The current test case instance.
   */
  private TestCaseInstance instance;

  private ActivityImpl findActivity(List<ActivityImpl> activities, String activityId) {
    boolean isMultiInstanceScope = activityId.endsWith(MULTI_INSTANCE_SCOPE_SUFFIX);

    for (ActivityImpl activity : activities) {
      String id;
      if (isMultiInstanceScope) {
        id = activity.getId();
      } else {
        id = stripMultiInstanceScopeSuffix(activity.getId());
      }

      if (id.equals(activityId)) {
        return activity;
      }

      ActivityImpl found = findActivity(activity.getActivities(), activityId);

      if (found != null) {
        return found;
      }
    }

    return null;
  }

  /**
   * Instruments the call activity by overriding its behavior to allow isolated testing.
   *
   * @param activity The current activity.
   */
  protected void instrumentCallActivity(ActivityImpl activity) {
    if (instance == null) {
      return;
    }

    CallActivityBehavior behavior = (CallActivityBehavior) activity.getActivityBehavior();
    activity.setActivityBehavior(new CallActivityTestBehavior(instance, behavior));

    // needed to verify the state before the call activity is executed
    // otherwise the process instance may not be available yet
    activity.setAsyncBefore(true);
  }

  /**
   * Instruments the end activity, if it does not end the process.
   *
   * @param activities A list of activities, which should also contain the end activity.
   */
  protected void instrumentEndActivity(List<ActivityImpl> activities) {
    if (instance == null || instance.isProcessEnd()) {
      return;
    }

    ActivityImpl endActivity = findActivity(activities, instance.getEnd());
    if (endActivity != null) {
      endActivity.setAsyncAfter(true);
    }
  }

  /**
   * Instruments the given activity, if it is a multi instance activity.
   *
   * @param scope    The surrounding scope.
   * @param activity The current activity.
   */
  protected void instrumentMultiInstanceActivity(ScopeImpl scope, ActivityImpl activity) {
    if (instance == null) {
      return;
    }

    if (!scope.isSubProcessScope() && scope.getId().endsWith(MULTI_INSTANCE_SCOPE_SUFFIX)) {
      activity.setAsyncBefore(true);
      activity.setAsyncAfter(true);
    }
  }

  /**
   * Instruments the given activity, if it is a multi instance scope.
   *
   * @param scope    The surrounding scope.
   * @param activity The current activity.
   */
  protected void instrumentMultiInstanceScope(ScopeImpl scope, ActivityImpl activity) {
    if (instance == null) {
      return;
    }

    if (!scope.isSubProcessScope() && scope.getId().endsWith(MULTI_INSTANCE_SCOPE_SUFFIX)) {
      activity.setAsyncBefore(true);
    }
  }

  @Override
  public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
    instrumentCallActivity(activity);
    instrumentMultiInstanceActivity(scope, activity);
  }

  @Override
  public void parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity) {
    instrumentMultiInstanceActivity(scope, activity);
  }

  @Override
  public void parseManualTask(Element manualTaskElement, ScopeImpl scope, ActivityImpl activity) {
    instrumentMultiInstanceActivity(scope, activity);
  }

  @Override
  public void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition) {
    instrumentEndActivity(processDefinition.getActivities());
  }

  @Override
  public void parseScriptTask(Element scriptTaskElement, ScopeImpl scope, ActivityImpl activity) {
    instrumentMultiInstanceActivity(scope, activity);
  }

  @Override
  public void parseSendTask(Element sendTaskElement, ScopeImpl scope, ActivityImpl activity) {
    instrumentMultiInstanceActivity(scope, activity);
  }

  @Override
  public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
    instrumentMultiInstanceActivity(scope, activity);
  }

  @Override
  public void parseSubProcess(Element subProcessElement, ScopeImpl scope, ActivityImpl activity) {
    instrumentMultiInstanceScope(scope, activity);
  }

  @Override
  public void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity) {
    instrumentMultiInstanceActivity(scope, activity);
  }

  @Override
  public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
    instrumentMultiInstanceActivity(scope, activity);
  }

  /**
   * Sets a reference to the related test case instance.
   *
   * @param instance The related instance.
   */
  public void setInstance(TestCaseInstance instance) {
    this.instance = instance;
  }

  protected String stripMultiInstanceScopeSuffix(String activityId) {
    if (activityId.endsWith(MULTI_INSTANCE_SCOPE_SUFFIX)) {
      return activityId.substring(0, activityId.length() - MULTI_INSTANCE_SCOPE_SUFFIX.length());
    } else {
      return activityId;
    }
  }
}
