package org.camunda.community.bpmndt.api;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.community.bpmndt.api.cfg.BpmndtParseListener;

/**
 * Link between test case and it's execution, utilizing a process instance that was instantiated by
 * a {@link TestCaseExecutor} and handlers (e.g. {@code UserTaskHandler}) that are part of a test
 * case.
 */
public class TestCaseInstance {

  /** Name of the process engine to use. */
  public static final String PROCESS_ENGINE_NAME = "bpmndt";

  private final Map<String, CallActivityHandler> callActivityHandlerMap;

  protected String end;
  /** Key of the test case related process definition. */
  protected String processDefinitionKey;
  protected boolean processEnd;
  protected ProcessEngine processEngine;
  protected String start;
  protected String tenantId;

  /** ID of BPMN resource deployment. */
  private String deploymentId;

  /** ID of the deployed process definition. */
  private String processDefinitionId;

  private ProcessInstance pi;

  public TestCaseInstance() {
    callActivityHandlerMap = new HashMap<>(4);
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

  public void apply(MultiInstanceHandler<?, ?> handler) {
    handler.apply(pi);
  }

  public void apply(MultiInstanceScopeHandler<?> handler) {
    handler.apply(pi);
  }

  public void apply(UserTaskHandler handler) {
    handler.apply(pi);
  }

  protected void deploy(String deploymentName, InputStream bpmnResource) {
    DeploymentBuilder deploymentBuilder = processEngine.getRepositoryService().createDeployment()
        .addInputStream(String.format("%s.bpmn", getProcessDefinitionKey()), bpmnResource);

    deploy(deploymentBuilder, deploymentName);
  }

  protected void deploy(String deploymentName, String bpmnResourceName) {
    DeploymentBuilder deploymentBuilder = processEngine.getRepositoryService().createDeployment()
        .addClasspathResource(bpmnResourceName);

    deploy(deploymentBuilder, deploymentName);
  }

  private void deploy(DeploymentBuilder deploymentBuilder, String deploymentName) {
    BpmndtParseListener parseListener = findParseListener();

    // register instance at BpmndtParseListener,
    // used for instrumentation of the process definition during BPMN model parsing
    parseListener.setInstance(this);

    DeploymentWithDefinitions deployment = deploymentBuilder.name(deploymentName)
        .enableDuplicateFiltering(false)
        .tenantId(tenantId)
        .deployWithResult();

    // deregister instance
    parseListener.setInstance(null);

    deploymentId = deployment.getId();

    processDefinitionId = deployment.getDeployedProcessDefinitions().stream()
        .filter(pd -> pd.getKey().equals(processDefinitionKey))
        .map(ProcessDefinition::getId)
        .findFirst()
        .get();
  }

  /**
   * Executes a stubbed call activity using a {@link CallActivityHandler} that was registered for the
   * given activity.
   * 
   * @param execution The current execution.
   * 
   * @param behavior The call activity's original behavior.
   * 
   * @return {@code true}, if the execution should leave the call activity. {@code false}, if the
   *         execution should wait at the call activity.
   * 
   * @throws Exception If the occurrence of an error end event is simulated and the error propagation
   *         fails.
   * 
   * @see CallActivityHandler#simulateBpmnError(String, String)
   */
  public boolean execute(ActivityExecution execution, CallActivityBehavior behavior) throws Exception {
    String activityId = execution.getCurrentActivityId();

    CallActivityHandler handler = callActivityHandlerMap.get(activityId);
    if (handler == null) {
      return true;
    } else {
      return handler.execute(pi, execution, behavior);
    }
  }

  private BpmndtParseListener findParseListener() {
    ProcessEngineConfigurationImpl processEngineConfiguration =
        (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();

    return processEngineConfiguration.getCustomPostBPMNParseListeners().stream()
        .filter((parseListener) -> (parseListener instanceof BpmndtParseListener))
        .map(BpmndtParseListener.class::cast)
        .findFirst()
        .get();
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public String getEnd() {
    return end;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
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

  public boolean isProcessEnd() {
    return processEnd;
  }

  protected void registerCallActivityHandler(String activityId, CallActivityHandler handler) {
    callActivityHandlerMap.put(activityId, handler);
  }

  protected void setProcessInstance(ProcessInstance pi) {
    this.pi = pi;
  }

  protected void undeploy() {
    callActivityHandlerMap.clear();

    if (deploymentId == null) {
      return;
    }

    processEngine.getRepositoryService().deleteDeployment(deploymentId, true, true, true);
  }
}
