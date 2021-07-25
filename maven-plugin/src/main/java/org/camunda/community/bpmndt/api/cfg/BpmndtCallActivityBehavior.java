package org.camunda.community.bpmndt.api.cfg;

import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.TaskActivityBehavior;
import org.camunda.bpm.engine.impl.core.model.CallableElement;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.community.bpmndt.api.CallActivityDefinition;
import org.camunda.community.bpmndt.api.TestCaseInstance;

/**
 * Custom behavior to stub call activities for isolated testing.
 */
public class BpmndtCallActivityBehavior extends TaskActivityBehavior {

  /** The current test case instance. */
  private final TestCaseInstance instance;

  /** The activity's original behavior. */
  private final CallActivityBehavior behavior;

  public BpmndtCallActivityBehavior(TestCaseInstance instance, CallActivityBehavior behavior) {
    this.instance = instance;
    this.behavior = behavior;
  }

  @Override
  public void execute(ActivityExecution execution) throws Exception {
    String activityId = execution.getCurrentActivityId();

    CallableElement callableElement = behavior.getCallableElement();

    CallActivityDefinition callActivityDefinition = new CallActivityDefinition();
    callActivityDefinition.setBinding(callableElement.getBinding());
    callActivityDefinition.setBusinessKey(callableElement.getBusinessKey(execution));
    callActivityDefinition.setDefinitionKey(callableElement.getDefinitionKey(execution));
    callActivityDefinition.setDefinitionTenantId(callableElement.getDefinitionTenantId(execution));
    callActivityDefinition.setVersion(callableElement.getVersion(execution));
    callActivityDefinition.setVersionTag(callableElement.getVersionTag(execution));

    instance.verifyCallActivity(activityId, callActivityDefinition);

    VariableMap subVariables = Variables.createVariables();

    DelegateVariableMapping variableMapping = (DelegateVariableMapping) behavior.resolveDelegateClass(execution);
    if (variableMapping != null) {
      variableMapping.mapInputVariables(execution, subVariables);
    }

    ActivityExecution subInstance = execution.createExecution();
    subInstance.setVariables(subVariables);

    instance.verifyCallActivityInput(activityId, subInstance);

    if (variableMapping != null) {
      variableMapping.mapOutputVariables(execution, subInstance);
    }

    subInstance.remove();

    instance.verifyCallActivityOutput(activityId, execution);

    super.execute(execution);
  }
}
