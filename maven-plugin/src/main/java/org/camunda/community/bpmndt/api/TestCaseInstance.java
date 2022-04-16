package org.camunda.community.bpmndt.api;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.community.bpmndt.api.cfg.BpmndtParseListener;

public class TestCaseInstance {

  /** Name of the process engine to use. */
  public static final String PROCESS_ENGINE_NAME = "bpmndt";

  private final Map<String, CallActivityHandler> callActivityHandlerMap;

  private ProcessEngine processEngine;

  /** Key of the test case related process definition. */
  private String processDefinitionKey;

  private String start;
  private String end;

  private boolean processEnd;

  /** ID of BPMN resource deployment. */
  private String deploymentId;

  private String tenantId;

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

  public void apply(UserTaskHandler handler) {
    handler.apply(pi);
  }

  protected void deploy(String deploymentName, InputStream bpmnResource) {
    this.register();

    RepositoryService repositoryService = processEngine.getRepositoryService();

    Deployment deployment = repositoryService.createDeployment()
        .name(deploymentName)
        .addInputStream(String.format("%s.bpmn", getProcessDefinitionKey()), bpmnResource)
        .enableDuplicateFiltering(false)
        .tenantId(tenantId)
        .deploy();

    deploymentId = deployment.getId();
  }

  protected void deploy(String deploymentName, String bpmnResourceName) {
    this.register();

    RepositoryService repositoryService = processEngine.getRepositoryService();

    Deployment deployment = repositoryService.createDeployment()
        .name(deploymentName)
        .addClasspathResource(bpmnResourceName)
        .enableDuplicateFiltering(false)
        .tenantId(tenantId)
        .deploy();

    deploymentId = deployment.getId();
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

  private Optional<BpmndtParseListener> findParseListener() {
    ProcessEngineConfigurationImpl processEngineConfiguration =
        (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();

    return processEngineConfiguration.getCustomPostBPMNParseListeners().stream()
        .filter((parseListener) -> (parseListener instanceof BpmndtParseListener))
        .map(BpmndtParseListener.class::cast)
        .findFirst();
  }

  public String getDeploymentId() {
    return deploymentId;
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

  public boolean isProcessEnd() {
    return processEnd;
  }

  /**
   * Registers the test case instance by providing a reference to the {@link BpmndtParseListener} that
   * is used during BPMN model parsing.
   */
  protected void register() {
    findParseListener().ifPresent((parseListener) -> parseListener.setInstance(this));
  }

  protected void registerCallActivityHandler(String activityId, CallActivityHandler handler) {
    callActivityHandlerMap.put(activityId, handler);
  }

  protected void setEnd(String end) {
    this.end = end;
  }

  protected void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  protected void setProcessEnd(boolean processEnd) {
    this.processEnd = processEnd;
  }

  protected void setProcessEngine(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }

  protected void setProcessInstance(ProcessInstance pi) {
    this.pi = pi;
  }

  protected void setStart(String start) {
    this.start = start;
  }

  protected void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  protected void undeploy() {
    // deregister instance
    findParseListener().ifPresent((parseListener) -> parseListener.setInstance(null));

    callActivityHandlerMap.clear();

    if (deploymentId == null) {
      return;
    }

    processEngine.getRepositoryService().deleteDeployment(deploymentId, true, true, true);
  }
}
