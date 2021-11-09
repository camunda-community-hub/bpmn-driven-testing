package org.camunda.community.bpmndt.api;

import java.util.List;
import java.util.concurrent.TimeUnit;
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
  private final String activityId;
  private final String topicName;

  private final VariableMap variables;
  private final VariableMap localVariables;

  private String errorCode;
  private String errorMessage;

  private BiConsumer<ProcessInstanceAssert, String> verifier;

  private Consumer<String> action;

  public ExternalTaskHandler(ProcessEngine processEngine, String activityId, String topicName) {
    this.processEngine = processEngine;
    this.activityId = activityId;
    this.topicName = topicName;

    variables = Variables.createVariables();
    localVariables = Variables.createVariables();

    action = this::complete;
  }

  protected void apply(ProcessInstance pi) {
    if (verifier != null) {
      verifier.accept(ProcessEngineTests.assertThat(pi), topicName);
    }

    if (action != null) {
      action.accept(topicName);
    }
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

  protected void complete(String topicName) {
    LockedExternalTask externalTask = fetchAndLock();
    processEngine.getExternalTaskService().complete(externalTask.getId(), WORKER_ID, variables, localVariables);
  }

  /**
   * Executes a custom action that handles the external task.
   * 
   * @param action A specific action that accepts the related topic name (String).
   */
  public void execute(Consumer<String> action) {
    this.action = action;
  }
  
  private LockedExternalTask fetchAndLock() {
    ExternalTaskService externalTaskService = processEngine.getExternalTaskService();

    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
        .topic(topicName, TimeUnit.SECONDS.toMillis(60L))
        .execute();

    if (externalTasks.isEmpty()) {
      throw new AssertionError(String.format("Expected to fetch at least one external task for topic '%s'", topicName));
    }

    LockedExternalTask externalTask = externalTasks.get(0);
    if (!externalTask.getActivityId().equals(activityId)) {
      throw new AssertionError(String.format("Expected to fetch at least one external task for activity '%s'", activityId));
    }

    return externalTask;
  }

  /**
   * Continues the execution with an action that calls {@code handleBpmnError} using the given error
   * code and message.
   * 
   * @param errorCode The error code of the attached boundary error event.
   * 
   * @param errorMessage An error message or {@code null}.
   * 
   * @see ExternalTaskService#handleBpmnError(String, String, String, String, java.util.Map)
   */
  public void handleBpmnError(String errorCode, String errorMessage) {
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;

    action = this::handleBpmnError;
  }

  protected void handleBpmnError(String topicName) {
    LockedExternalTask externalTask = fetchAndLock();
    processEngine.getExternalTaskService().handleBpmnError(externalTask.getId(), WORKER_ID, errorCode, errorMessage, variables);
  }

  /**
   * Determines if the external task is waiting for a boundary message, signal or timer event.
   * 
   * @return {@code true}, if it is waiting for a boundary event. {@code false}, if not.
   */
  public boolean isWaitingForBoundaryEvent() {
    return action == null;
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
   * Applies no action at the external task's wait state. This is required to wait for events (e.g.
   * message, signal or timer events) that are attached as boundary events on the activity itself or
   * on the surrounding scope (e.g. embedded subprocess).
   */
  public void waitForBoundaryEvent() {
    action = null;
  }

  /**
   * Sets the error message, which is used when the next activity is an error boundary event - in this
   * case the handler's default action is {@code handleBpmnError}.
   * 
   * @param errorMessage An error message or {@code null}.
   * 
   * @return The handler.
   */
  public ExternalTaskHandler withErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
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
