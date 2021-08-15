package org.camunda.community.bpmndt.api;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.helper.BpmnExceptionHandler;
import org.camunda.bpm.engine.impl.bpmn.helper.EscalationHandler;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.core.model.CallableElement;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.community.bpmndt.api.cfg.BpmndtParseListener;

public class TestCaseInstance {

  /** Name of the process engine to use. */
  public static final String PROCESS_ENGINE_NAME = "bpmndt";

  private final ProcessEngine processEngine;

  /** Key of the test case related process definition. */
  private final String processDefinitionKey;

  private final String start;
  private final String end;

  private final Map<String, CallActivityHandler> callActivityHandlerMap;

  private ProcessInstance pi;

  public TestCaseInstance(ProcessEngine processEngine, String processDefinitionKey, String start, String end) {
    this.processEngine = processEngine;
    this.processDefinitionKey = processDefinitionKey;
    this.start = start;
    this.end = end;

    callActivityHandlerMap = new HashMap<>(4);

    findParseListener(processEngine).ifPresent((parseListener) -> parseListener.setInstance(this));
  }

  protected void addCallActivityHandler(String activityId, CallActivityHandler handler) {
    callActivityHandlerMap.put(activityId, handler);
  }

  public void apply(EventHandler handler) {
    handler.apply(pi);
  }

  public void apply(ExternalTaskHandler handler) {
    handler.apply(pi);
  }

  public void apply(JobHandler handler) {
    handler.apply(pi);
  }

  public void apply(UserTaskHandler handler) {
    handler.apply(pi);
  }

  public boolean execute(ActivityExecution execution, CallActivityBehavior behavior) throws Exception {
    CallableElement callableElement = behavior.getCallableElement();

    CallActivityDefinition callActivityDefinition = new CallActivityDefinition();
    callActivityDefinition.setBinding(callableElement.getBinding());
    callActivityDefinition.setBusinessKey(callableElement.getBusinessKey(execution));
    callActivityDefinition.setDefinitionKey(callableElement.getDefinitionKey(execution));
    callActivityDefinition.setDefinitionTenantId(callableElement.getDefinitionTenantId(execution));
    callActivityDefinition.setVersion(callableElement.getVersion(execution));
    callActivityDefinition.setVersionTag(callableElement.getVersionTag(execution));

    String activityId = execution.getCurrentActivityId();

    CallActivityHandler handler = callActivityHandlerMap.get(activityId);
    if (handler == null) {
      return true;
    }

    handler.verify(pi, callActivityDefinition);

    VariableMap subVariables = Variables.createVariables();

    DelegateVariableMapping variableMapping = (DelegateVariableMapping) behavior.resolveDelegateClass(execution);
    if (variableMapping != null) {
      variableMapping.mapInputVariables(execution, subVariables);
    }

    ActivityExecution subInstance = execution.createExecution();
    subInstance.setVariables(subVariables);

    handler.verifyInput(subInstance);

    if (variableMapping != null) {
      variableMapping.mapOutputVariables(execution, subInstance);
    }

    handler.verifyOutput(execution);

    if (handler.isErrorEnd()) {
      BpmnExceptionHandler.propagateError(handler.getErrorCode(), handler.getErrorMessage(), null, subInstance);
      return false;
    }

    if (handler.isEscalationEnd()) {
      EscalationHandler.propagateEscalation(subInstance, handler.getEscalationCode());
      return false;
    }

    subInstance.remove();

    return handler.shouldWaitForBoundaryEvent() ? false : true;
  }

  protected void clear() {
    findParseListener(processEngine).ifPresent((parseListener) -> parseListener.setInstance(null));

    callActivityHandlerMap.clear();
  }

  protected String deploy(String deploymentName, InputStream bpmnResource) {
    RepositoryService repositoryService = processEngine.getRepositoryService();

    Deployment deployment = repositoryService.createDeployment()
        .name(deploymentName)
        .addInputStream("test.bpmn", bpmnResource)
        .deploy();

    return deployment.getId();
  }

  protected String deploy(String deploymentName, String bpmnResourceName) {
    RepositoryService repositoryService = processEngine.getRepositoryService();

    Deployment deployment = repositoryService.createDeployment()
        .name(deploymentName)
        .addClasspathResource(bpmnResourceName)
        .deploy();

    return deployment.getId();
  }

  protected Optional<BpmndtParseListener> findParseListener(ProcessEngine processEngine) {
    ProcessEngineConfigurationImpl processEngineConfiguration =
        (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();

    return processEngineConfiguration.getCustomPostBPMNParseListeners().stream()
        .filter((parseListener) -> (parseListener instanceof BpmndtParseListener))
        .map(BpmndtParseListener.class::cast)
        .findFirst();
  }

  public String getEnd() {
    return end;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public ProcessEngine getProcessEngine() {
    return processEngine;
  }

  public String getStart() {
    return start;
  }

  protected void setProcessInstance(ProcessInstance pi) {
    this.pi = pi;
  }

  protected void undeploy(String deploymentId) {
    if (deploymentId == null) {
      return;
    }

    processEngine.getRepositoryService().deleteDeployment(deploymentId, true, true, true);
  }
}
