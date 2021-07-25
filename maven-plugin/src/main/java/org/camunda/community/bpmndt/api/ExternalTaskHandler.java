package org.camunda.community.bpmndt.api;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * Fluent API to handle external tasks.
 */
public class ExternalTaskHandler {
  
  private static final String WORKER_ID = "bpmndt-worker";
  
  private final ProcessEngine processEngine;
  private final String topicName;

  private final VariableMap variables;
  private final VariableMap localVariables;

  private BiConsumer<ProcessInstanceAssert, String> verifier;

  private Consumer<String> action;

  public ExternalTaskHandler(ProcessEngine processEngine, String topicName) {
    this.processEngine = processEngine;
    this.topicName = topicName;

    variables = Variables.createVariables();
    localVariables = Variables.createVariables();

    action = this::complete;
  }

  protected void apply(ProcessInstance pi) {
    if (verifier != null) {
      verifier.accept(ProcessEngineTests.assertThat(pi), topicName);
    }

    action.accept(topicName);
  }

  /**
   * Completes the external task with an action that fetches/locks the task and calls
   * {@code complete}.
   *
   * @see ExternalTaskService#complete(String, String, java.util.Map, java.util.Map)
   */
  public void complete() {
    action = this::complete;
  }

  /**
   * Completes the external task with a custom action that is executed when the handler is applied.
   * 
   * @param action A specific action that accepts the related topic name (String).
   */
  public void complete(Consumer<String> action) {
    this.action = action;
  }

  protected void complete(String topicName) {
    ExternalTaskService externalTaskService = processEngine.getExternalTaskService();
    
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
        .topic(topicName, 1000L)
        .execute();

    if (externalTasks.isEmpty()) {
      throw new RuntimeException("Expected at least one fetched external task");
    }

    externalTaskService.complete(externalTasks.get(0).getId(), WORKER_ID, variables, localVariables);
  }

  /**
   * Verifies the external task's waiting state.
   * 
   * @param verifier Verifier that accepts an {@link ProcessInstanceAssert} instance and the related
   *        topic name (String).
   * 
   * @return The handler.
   */
  public ExternalTaskHandler verify(BiConsumer<ProcessInstanceAssert, String> verifier) {
    this.verifier = verifier;
    return this;
  }

  /**
   * Sets a local variable, which is passed to the execution when the default action is used.
   * 
   * @param name The name of the local variable.
   * 
   * @param value The local variable's value.
   * 
   * @return The handler.
   */
  public ExternalTaskHandler withLocalVariable(String name, Object value) {
    localVariables.putValue(name, value);
    return this;
  }

  /**
   * Sets a typed local variable, which is passed to the execution when the default action is used.
   * 
   * @param name The name of the local variable.
   * 
   * @param value The local variable's typed value.
   * 
   * @return The handler.
   */
  public ExternalTaskHandler withLocalVariableTyped(String name, TypedValue value) {
    localVariables.putValueTyped(name, value);
    return this;
  }

  /**
   * Sets a variable, which is passed to the execution when the default action is used.
   * 
   * @param name The name of the variable.
   * 
   * @param value The variable's value.
   * 
   * @return The handler.
   */
  public ExternalTaskHandler withVariable(String name, Object value) {
    variables.putValue(name, value);
    return this;
  }

  /**
   * Sets a typed variable, which is passed to the execution when the default action is used.
   * 
   * @param name The name of the variable.
   * 
   * @param value The variable's typed value.
   * 
   * @return The handler.
   */
  public ExternalTaskHandler withVariableTyped(String name, TypedValue value) {
    variables.putValueTyped(name, value);
    return this;
  }
}
