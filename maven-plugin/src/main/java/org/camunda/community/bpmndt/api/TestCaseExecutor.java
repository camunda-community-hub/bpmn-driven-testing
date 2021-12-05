package org.camunda.community.bpmndt.api;

import java.util.Optional;
import java.util.function.Consumer;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.community.bpmndt.api.cfg.BpmndtParseListener;

/**
 * Fluent API to prepare and start the actual test case execution.
 */
public class TestCaseExecutor {

  private final TestCaseInstance instance;
  private final Consumer<ProcessInstance> executor;

  private final VariableMap variables;

  private String businessKey;

  private Consumer<ProcessInstanceAssert> verifier;

  public TestCaseExecutor(TestCaseInstance instance, Consumer<ProcessInstance> executor) {
    this.instance = instance;
    this.executor = executor;

    variables = Variables.createVariables();
  }

  /**
   * Create a new {@link ProcessInstance}, executes the actual test case and verifies the state after.
   * 
   * @return The ID of the created process instance.
   */
  public String execute() {
    RuntimeService runtimeService = instance.getProcessEngine().getRuntimeService();

    ProcessInstance pi = runtimeService.createProcessInstanceByKey(instance.getProcessDefinitionKey())
        .businessKey(businessKey)
        .setVariables(variables)
        .startBeforeActivity(instance.getStart())
        .execute();

    execute(pi);

    return pi.getId();
  }

  /**
   * Executes the actual test case and verifies the state after, using the given
   * {@link ProcessInstance}.
   * 
   * @param pi A process instance, used to execute the test case.
   */
  public void execute(ProcessInstance pi) {
    if (pi == null) {
      throw new IllegalArgumentException("The given process instance is null");
    }

    // announce test case instance for custom call activity behavior
    findParseListener().ifPresent((parseListener) -> parseListener.setInstance(instance));

    // announce process instance
    instance.setProcessInstance(pi);

    try {
      executor.accept(pi);
    } catch (ProcessEngineException e) {
      throw unwrapAssertionError(e);
    }

    if (verifier != null) {
      verifier.accept(ProcessEngineTests.assertThat(pi));
    }
  }

  /**
   * Executes the actual test case and verifies the state after, using the {@link ProcessInstance},
   * identified by the given ID.
   * 
   * @param processInstanceId The ID of an existing process instance.
   */
  public void execute(String processInstanceId) {
    if (processInstanceId == null) {
      throw new IllegalArgumentException("The given process instance ID is null");
    }

    RuntimeService runtimeService = instance.getProcessEngine().getRuntimeService();

    ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
    if (pi == null) {
      throw new IllegalArgumentException("No process instance found for the given ID");
    }

    execute(pi);
  }

  protected Optional<BpmndtParseListener> findParseListener() {
    ProcessEngineConfigurationImpl processEngineConfiguration =
        (ProcessEngineConfigurationImpl) instance.getProcessEngine().getProcessEngineConfiguration();

    return processEngineConfiguration.getCustomPostBPMNParseListeners().stream()
        .filter((parseListener) -> (parseListener instanceof BpmndtParseListener))
        .map(BpmndtParseListener.class::cast)
        .findFirst();
  }

  /**
   * Unwraps and throws a possible {@link AssertionError} in case of a failed assertion within a
   * {@link CallActivityHandler}'s verifier. If not unwrapped, a test will be marked as an error,
   * instead of a failure!
   * 
   * @param e An exception that has been catched during process instance execution.
   * 
   * @return The original exception, if the cause of the given exception is {@code null} or not an
   *         assertion error.
   */
  protected ProcessEngineException unwrapAssertionError(ProcessEngineException e) {
    if (e.getCause() instanceof AssertionError) {
      throw (AssertionError) e.getCause();
    }

    return e;
  }

  /**
   * Verifies that state after the test case execution has finished.
   * 
   * @param verifier Verifier that accepts an {@link ProcessInstanceAssert} instance.
   * 
   * @return The executor.
   */
  public TestCaseExecutor verify(Consumer<ProcessInstanceAssert> verifier) {
    this.verifier = verifier;
    return this;
  }

  /**
   * Registers a bean for the given key.<br>
   * Please note: If Spring is enabled, the beans will be provided via Spring's application context
   * (e.g. by providing a specific test configuration).
   * 
   * @param key The key, under which the bean is registered.
   * 
   * @param value The value.
   * 
   * @return The executor.
   * 
   * @see Mocks#register(String, Object)
   */
  public TestCaseExecutor withBean(String key, Object value) {
    Mocks.register(key, value);
    return this;
  }

  /**
   * Sets the business key of the process instance that will be created.
   * 
   * @param businessKey A specific business key.
   * 
   * @return The executor.
   */
  public TestCaseExecutor withBusinessKey(String businessKey) {
    this.businessKey = businessKey;
    return this;
  }

  /**
   * Sets a variable on the process instance that will be created.
   * 
   * @param name The name of the variable.
   * 
   * @param value The variable's value.
   * 
   * @return The executor.
   */
  public TestCaseExecutor withVariable(String name, Object value) {
    variables.putValue(name, value);
    return this;
  }

  /**
   * Sets a typed variable on the process instance that will be started.
   * 
   * @param name The name of the variable.
   * 
   * @param value The variable's typed value.
   * 
   * @return The executor.
   */
  public TestCaseExecutor withVariableTyped(String name, TypedValue value) {
    variables.putValueTyped(name, value);
    return this;
  }
}
