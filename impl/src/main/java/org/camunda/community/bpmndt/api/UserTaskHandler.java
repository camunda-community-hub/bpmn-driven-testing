package org.camunda.community.bpmndt.api;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.bpm.engine.test.assertions.bpmn.TaskAssert;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * Fluent API to handle user tasks.
 */
public class UserTaskHandler {

  private final ProcessEngine processEngine;
  private final String activityId;

  private final VariableMap variables;

  private String errorCode;
  private String errorMessage;
  private String escalationCode;

  private BiConsumer<ProcessInstanceAssert, TaskAssert> verifier;

  private Consumer<Task> action;

  public UserTaskHandler(ProcessEngine processEngine, String activityId) {
    this.processEngine = processEngine;
    this.activityId = activityId;

    variables = Variables.createVariables();

    action = this::complete;
  }

  protected void apply(ProcessInstance pi) {
    Task task = ProcessEngineTests.task(activityId, pi);

    if (verifier != null) {
      verifier.accept(ProcessEngineTests.assertThat(pi), ProcessEngineTests.assertThat(task));
    }

    if (action != null) {
      action.accept(task);
    }
  }

  /**
   * Completes the user task with an action that calls {@code complete}, when the process instance is
   * waiting at the corresponding activity. Please note: this is the default behavior.
   * 
   * @see TaskService#complete(String, java.util.Map)
   */
  public void complete() {
    action = this::complete;
  }

  protected void complete(Task task) {
    processEngine.getTaskService().complete(task.getId(), variables);
  }

  /**
   * Customizes the handler, using the given {@link Consumer} function. This method can be used to
   * apply a common customization needed for different test cases.
   * 
   * <pre>
   * tc.handleUserTask().customize(this::prepareUserTask);
   * </pre>
   * 
   * @param customizer A function that accepts a {@link UserTaskHandler}.
   * 
   * @return The handler.
   */
  public UserTaskHandler customize(Consumer<UserTaskHandler> customizer) {
    if (customizer != null) {
      customizer.accept(this);
    }
    return this;
  }

  /**
   * Executes a custom action that handles the user task, when the process instance is waiting at the
   * corresponding activity.
   * 
   * @param action A specific action that accepts the related {@link Task}.
   */
  public void execute(Consumer<Task> action) {
    this.action = action;
  }

  /**
   * Continues the execution with an action that calls {@code handleBpmnError} using the given error
   * code and message, when the process instance is waiting at the corresponding activity.
   * 
   * @param errorCode The error code of the attached boundary error event.
   * 
   * @param errorMessage An error message or {@code null}.
   * 
   * @see TaskService#handleBpmnError(String, String, String, java.util.Map)
   */
  public void handleBpmnError(String errorCode, String errorMessage) {
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;

    action = this::handleBpmnError;
  }

  protected void handleBpmnError(Task task) {
    processEngine.getTaskService().handleBpmnError(task.getId(), errorCode, errorMessage, variables);
  }

  /**
   * Continues the execution with an action that calls {@code handleEscalation} using the given
   * escalation code, when the process instance is waiting at the corresponding activity.
   * 
   * @param escalationCode The escalation code of the attached boundary escalation event.
   * 
   * @see TaskService#handleEscalation(String, String, java.util.Map)
   */
  public void handleEscalation(String escalationCode) {
    this.escalationCode = escalationCode;

    action = this::handleEscalation;
  }

  protected void handleEscalation(Task task) {
    processEngine.getTaskService().handleEscalation(task.getId(), escalationCode, variables);
  }

  /**
   * Determines if the user task is waiting for a boundary message, signal or timer event.
   * 
   * @return {@code true}, if it is waiting for a boundary event. {@code false}, if not.
   */
  public boolean isWaitingForBoundaryEvent() {
    return action == null;
  }

  /**
   * Verifies the user task's waiting state.
   * 
   * @param verifier Verifier that accepts an {@link ProcessInstanceAssert} and an {@link TaskAssert}
   *        instance.
   * 
   * @return The handler.
   */
  public UserTaskHandler verify(BiConsumer<ProcessInstanceAssert, TaskAssert> verifier) {
    this.verifier = verifier;
    return this;
  }

  /**
   * Applies no action at the user task's wait state. This is required to wait for events (e.g.
   * message, signal or timer events) that are attached as boundary events on the activity itself or
   * on the surrounding scope (e.g. embedded subprocess).
   */
  public void waitForBoundaryEvent() {
    action = null;
  }

  /**
   * Sets the error message, which is used when the next activity is an error boundary event - in this
   * case the handler's action is {@code handleBpmnError}.
   * 
   * @param errorMessage An error message or {@code null}.
   * 
   * @return The handler.
   */
  public UserTaskHandler withErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }

  /**
   * Sets a variable, which is passed to the execution when a predefined behavior ({@code complete},
   * {@code handleBpmnError}, or {@code handleEscalation}) is used.
   * 
   * @param name The name of the variable.
   * 
   * @param value The variable's value.
   * 
   * @return The handler.
   * 
   * @see #complete()
   * @see #handleBpmnError(String, String)
   * @see #handleEscalation(String)
   */
  public UserTaskHandler withVariable(String name, Object value) {
    variables.putValue(name, value);
    return this;
  }

  /**
   * Sets variables, which are passed to the execution when a predefined behavior ({@code complete},
   * {@code handleBpmnError}, or {@code handleEscalation}) is used.
   * 
   * @param variables A map of variables to set.
   * 
   * @return The handler.
   * 
   * @see #complete()
   * @see #handleBpmnError(String, String)
   * @see #handleEscalation(String)
   */
  public UserTaskHandler withVariables(Map<String, Object> variables) {
    this.variables.putAll(variables);
    return this;
  }

  /**
   * Sets a typed variable, which is passed to the execution when a predefined behavior
   * ({@code complete}, {@code handleBpmnError}, or {@code handleEscalation}) is used.
   * 
   * @param name The name of the variable.
   * 
   * @param value The variable's typed value.
   * 
   * @return The handler.
   * 
   * @see #complete()
   * @see #handleBpmnError(String, String)
   * @see #handleEscalation(String)
   */
  public UserTaskHandler withVariableTyped(String name, TypedValue value) {
    variables.putValueTyped(name, value);
    return this;
  }
}
