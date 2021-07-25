package org.camunda.community.bpmndt.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.community.bpmndt.api.cfg.BpmndtParseListener;

public class TestCaseInstance {

  /** Name of the process engine to use. */
  public static final String PROCESS_ENGINE_NAME = "bpmndt";

  private final ProcessEngine processEngine;

  /** Key of the test case related process definition. */
  private final String processDefinitionKey;

  private final String start;
  private final String end;

  // CallActivity
  private final Map<String, BiConsumer<ProcessInstanceAssert, CallActivityDefinition>> callActivityVerifierMap;
  private final Map<String, Consumer<VariableScope>> callActivityInputVerifierMap;
  private final Map<String, Consumer<VariableScope>> callActivityOutputVerifierMap;

  private ProcessInstance pi;

  public TestCaseInstance(ProcessEngine processEngine, String processDefinitionKey, String start, String end) {
    this.processEngine = processEngine;
    this.processDefinitionKey = processDefinitionKey;
    this.start = start;
    this.end = end;

    callActivityVerifierMap = new HashMap<>(4);
    callActivityInputVerifierMap = new HashMap<>(4);
    callActivityOutputVerifierMap = new HashMap<>(4);

    findParseListener(processEngine).ifPresent((parseListener) -> parseListener.setInstance(this));
  }

  public void apply(ExternalTaskHandler handler) {
    handler.apply(pi);
  }

  public void apply(IntermediateCatchEventHandler handler) {
    handler.apply(pi);
  }

  public void apply(JobHandler handler) {
    handler.apply(pi);
  }

  public void apply(UserTaskHandler handler) {
    handler.apply(pi);
  }

  protected void clear() {
    findParseListener(processEngine).ifPresent((parseListener) -> parseListener.setInstance(null));

    callActivityVerifierMap.clear();
    callActivityInputVerifierMap.clear();
    callActivityOutputVerifierMap.clear();
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

  public void verifyCallActivity(String activityId, CallActivityDefinition callActivityDefinition) {
    BiConsumer<ProcessInstanceAssert, CallActivityDefinition> verifier = callActivityVerifierMap.get(activityId);
    if (verifier != null) {
      verifier.accept(ProcessEngineTests.assertThat(pi), callActivityDefinition);
    }
  }

  public void verifyCallActivity(String activityId, BiConsumer<ProcessInstanceAssert, CallActivityDefinition> verifier) {
    callActivityVerifierMap.put(activityId, verifier);
  }

  public void verifyCallActivityInput(String activityId, VariableScope variables) {
    Consumer<VariableScope> verifier = callActivityInputVerifierMap.get(activityId);
    if (verifier != null) {
      verifier.accept(variables);
    }
  }

  public void verifyCallActivityInput(String activityId, Consumer<VariableScope> verifier) {
    callActivityInputVerifierMap.put(activityId, verifier);
  }

  public void verifyCallActivityOutput(String activityId, VariableScope variables) {
    Consumer<VariableScope> verifier = callActivityOutputVerifierMap.get(activityId);
    if (verifier != null) {
      verifier.accept(variables);
    }
  }

  public void verifyCallActivityOutput(String activityId, Consumer<VariableScope> verifier) {
    callActivityOutputVerifierMap.put(activityId, verifier);
  }
}
