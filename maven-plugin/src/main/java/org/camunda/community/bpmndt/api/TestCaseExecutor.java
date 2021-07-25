package org.camunda.community.bpmndt.api;

import java.util.function.Consumer;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;

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
   */
  public void execute() {
    RuntimeService runtimeService = instance.getProcessEngine().getRuntimeService();

    ProcessInstance pi = runtimeService.createProcessInstanceByKey(instance.getProcessDefinitionKey())
        .businessKey(businessKey)
        .setVariables(variables)
        .startBeforeActivity(instance.getStart())
        .execute();

    // announce process instance
    instance.setProcessInstance(pi);

    executor.accept(pi);

    if (verifier != null) {
      verifier.accept(ProcessEngineTests.assertThat(pi));
    }
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
   * Registers a mock (bean) for the given key.<br>
   * Please note: If Spring is enabled, the beans will be provided via Spring's application context
   * (e.g. by providing a specific test configuration).
   * 
   * @param key The key, under which the mock is registered.
   * 
   * @param value The mock's value.
   * 
   * @return The executor.
   * 
   * @see Mocks#register(String, Object)
   */
  public TestCaseExecutor withMock(String key, Object value) {
    Mocks.register(key, value);
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
