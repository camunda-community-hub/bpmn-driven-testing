package org.camunda.bpm.engine.impl.bpmn.behavior;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmProcessInstance;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.util.CallableElementUtil;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.community.bpmndt.api.AbstractTestCase;
import org.camunda.community.bpmndt.api.TestCaseInstance;

/**
 * Custom behavior to simulate or execute call activities during testing.
 */
public class CallActivityTestBehavior extends CallActivityBehavior {

  /**
   * Related test case instance.
   */
  private final TestCaseInstance instance;

  public CallActivityTestBehavior(TestCaseInstance instance, CallActivityBehavior behavior) {
    this.instance = instance;

    callableElement = behavior.callableElement;
    expression = behavior.expression;
    className = behavior.className;
  }

  @Override
  public void execute(ActivityExecution execution) throws Exception {
    AbstractTestCase<?> subTestCase = instance.getSubTestCase(execution);
    if (subTestCase == null) {
      // simulate call activity
      boolean shouldLeave = instance.simulateCallActivity(execution, this);

      if (shouldLeave) {
        leave(execution);
      }
    } else {
      // execute call activity
      instance.verifyCallActivity(execution, this);
      super.execute(execution);
    }
  }

  @Override
  public void passOutputVariables(ActivityExecution execution, VariableScope subInstance) {
    super.passOutputVariables(execution, subInstance);
    instance.verifyCallActivityOutput(execution);
  }

  @Override
  protected void startInstance(ActivityExecution execution, VariableMap variables, String businessKey) {
    instance.verifyCallActivityInput(execution, variables);

    AbstractTestCase<?> subTestCase = instance.getSubTestCase(execution);
    if (subTestCase == null) {
      throw new IllegalStateException("Sub test case is null");
    }

    DeploymentCache deploymentCache = CallableElementUtil.getDeploymentCache();

    ProcessDefinitionEntity processDefinition = deploymentCache.findDeployedProcessDefinitionByDeploymentAndKey(
        subTestCase.getDeploymentId(),
        subTestCase.getProcessDefinitionKey()
    );

    PvmProcessInstance pvmProcessInstance = execution.createSubProcessInstance(processDefinition, businessKey);
    pvmProcessInstance.start(variables);
  }
}
