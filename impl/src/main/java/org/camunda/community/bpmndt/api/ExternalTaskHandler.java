package org.camunda.community.bpmndt.api;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ExternalTaskAssert;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * Fluent API to handle external tasks.
 */
public class ExternalTaskHandler {

  protected static final String WORKER_ID = "bpmndt-worker";

  private final ProcessEngine processEngine;
  private final String activityId;
  private final String topicName;

  private final VariableMap variables;
  private final VariableMap localVariables;

  private String errorCode;
  private String errorMessage;

  private BiConsumer<ProcessInstanceAssert, String> verifier;
  private BiConsumer<ExternalTaskAssert, Map<String, Object>> taskVerifier;

  private Consumer<String> action;
  private Consumer<ExternalTask> taskAction;

  public ExternalTaskHandler(ProcessEngine processEngine, String activityId, String topicName) {
    this.processEngine = processEngine;
    this.activityId = activityId;
    this.topicName = topicName;

    variables = Variables.createVariables();
    localVariables = Variables.createVariables();

    taskAction = this::complete;
  }

  protected void apply(ProcessInstance pi) {
    if (verifier != null) {
      verifier.accept(ProcessEngineTests.assertThat(pi), topicName);
    }
    if (taskVerifier != null) {
      ExternalTask task = query(pi);

      Map<String, Object> localVariables = processEngine.getRuntimeService().getVariablesLocal(task.getExecutionId());

      taskVerifier.accept(ProcessEngineTests.assertThat(task), localVariables);
    }

    if (action != null) {
      action.accept(topicName);
    } else if (taskAction != null) {
      ExternalTask task = queryAndLock(pi);
      taskAction.accept(task);
    }
  }

  /**
   * Completes the external task, which is locked for 60 seconds by calling {@code complete}, using
   * the specified variables and local variables, when the process instance is waiting at the
   * corresponding activity. Please note: this is the default behavior.
   *
   * @see ExternalTaskService#complete(String, String, java.util.Map, java.util.Map)
   */
  public void complete() {
    taskAction = this::complete;
    action = null;
  }

  protected void complete(ExternalTask task) {
    processEngine.getExternalTaskService().complete(task.getId(), task.getWorkerId(), variables, localVariables);
  }

  /**
   * Customizes the handler, using the given {@link Consumer} function. This method can be used to
   * apply a common customization needed for different test cases.
   * 
   * <pre>
   * tc.handleExternalTask().customize(this::prepareExternalTask);
   * </pre>
   * 
   * @param customizer A function that accepts a {@link ExternalTaskHandler}.
   * 
   * @return The handler.
   */
  public ExternalTaskHandler customize(Consumer<ExternalTaskHandler> customizer) {
    if (customizer != null) {
      customizer.accept(this);
    }
    return this;
  }

  /**
   * Executes a custom action that handles the external task, when the process instance is waiting at
   * the corresponding activity.
   * 
   * @param action A specific action that accepts the related topic name (String).
   */
  public void execute(Consumer<String> action) {
    this.action = action;
    this.taskAction = null;
  }

  /**
   * Executes a custom action that handles the external task, which has been queried (by process
   * instance ID, activity ID and topic name) and locked before, when the process instance is waiting
   * at the corresponding activity.
   * 
   * @param action A specific action that accepts an {@link ExternalTask}.
   * 
   * @see ExternalTaskService#lock(String, String, long)
   */
  public void executeExternalTask(Consumer<ExternalTask> action) {
    this.taskAction = action;
    this.action = null;
  }

  /**
   * Executes a custom action that handles the external task, which has been queried (by process
   * instance ID, activity ID and topic name) and locked before, when the process instance is waiting
   * at the corresponding activity.
   * 
   * @param action A specific action that accepts an {@link LockedExternalTask}.
   * 
   * @see ExternalTaskService#lock(String, String, long)
   */
  public void executeLockedExternalTask(Consumer<LockedExternalTask> action) {
    this.taskAction = new WrappedTaskAction(this, action);
    this.action = null;
  }

  /**
   * Continues the execution with an action that calls {@code handleBpmnError} using the given error
   * code and message as well as the specified variables.
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

    taskAction = this::handleBpmnError;
    action = null;
  }

  protected void handleBpmnError(ExternalTask task) {
    processEngine.getExternalTaskService().handleBpmnError(task.getId(), task.getWorkerId(), errorCode, errorMessage, variables);
  }

  /**
   * Determines if the external task is waiting for a boundary message, signal or timer event.
   * 
   * @return {@code true}, if it is waiting for a boundary event. {@code false}, if not.
   */
  public boolean isWaitingForBoundaryEvent() {
    return action == null && taskAction == null;
  }

  private ExternalTask query(ProcessInstance pi) {
    ExternalTaskService externalTaskService = processEngine.getExternalTaskService();

    ExternalTask externalTask = externalTaskService.createExternalTaskQuery()
        .processInstanceId(pi.getId())
        .activityId(activityId)
        .topicName(topicName)
        .singleResult();

    if (externalTask == null) {
      String msg = String.format("Expected exactly one external task for activity '%s' and topic '%s'", activityId, topicName);
      throw new AssertionError(msg);
    }

    return externalTask;
  }

  private ExternalTask queryAndLock(ProcessInstance pi) {
    ExternalTask externalTask = query(pi);

    ExternalTaskService externalTaskService = processEngine.getExternalTaskService();

    try {
      externalTaskService.lock(externalTask.getId(), WORKER_ID, TimeUnit.SECONDS.toMillis(60L));
    } catch (BadUserRequestException e) {
      String msg = String.format("External task for activity '%s' and topic '%s' could not be locked: %s", activityId, topicName, e.getMessage());
      throw new AssertionError(msg, e);
    }

    // query again to get worker ID and lock expiration time
    return externalTaskService.createExternalTaskQuery()
        .externalTaskId(externalTask.getId())
        .singleResult();
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
   * Verifies the external task's waiting state.
   * 
   * @param taskVerifier Verifier that accepts an {@link ExternalTaskAssert} instance and the task's
   *        local variables.
   * 
   * @return The handler.
   */
  public ExternalTaskHandler verifyTask(BiConsumer<ExternalTaskAssert, Map<String, Object>> taskVerifier) {
    this.taskVerifier = taskVerifier;
    return this;
  }

  /**
   * Applies no action at the external task's wait state. This is required to wait for events (e.g.
   * message, signal or timer events) that are attached as boundary events on the activity itself or
   * on the surrounding scope (e.g. embedded subprocess).
   */
  public void waitForBoundaryEvent() {
    action = null;
    taskAction = null;
  }

  /**
   * Sets the error message, which is used when the next activity is an error boundary event - in this
   * case the handler's action is {@code handleBpmnError}.
   * 
   * @param errorMessage An error message or {@code null}.
   * 
   * @return The handler.
   * 
   * @see #handleBpmnError(String, String)
   */
  public ExternalTaskHandler withErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }

  /**
   * Sets a local variable, which is passed to the execution when the default behavior is used.
   * 
   * @param name The name of the local variable.
   * 
   * @param value The local variable's value.
   * 
   * @return The handler.
   * 
   * @see #complete()
   */
  public ExternalTaskHandler withLocalVariable(String name, Object value) {
    localVariables.putValue(name, value);
    return this;
  }

  /**
   * Sets local variables, which are passed to the execution when the default behavior is used.
   * 
   * @param localVariables A map of local variables to set.
   * 
   * @return The handler.
   * 
   * @see #complete()
   */
  public ExternalTaskHandler withLocalVariables(Map<String, Object> localVariables) {
    this.localVariables.putAll(localVariables);
    return this;
  }

  /**
   * Sets a typed local variable, which is passed to the execution when the default behavior is used.
   * 
   * @param name The name of the local variable.
   * 
   * @param value The local variable's typed value.
   * 
   * @return The handler.
   * 
   * @see #complete()
   */
  public ExternalTaskHandler withLocalVariableTyped(String name, TypedValue value) {
    localVariables.putValueTyped(name, value);
    return this;
  }

  /**
   * Sets a variable, which is passed to the execution when a predefined behavior is used.
   * 
   * @param name The name of the variable.
   * 
   * @param value The variable's value.
   * 
   * @return The handler.
   * 
   * @see #complete()
   * @see #handleBpmnError(String, String)
   */
  public ExternalTaskHandler withVariable(String name, Object value) {
    variables.putValue(name, value);
    return this;
  }

  /**
   * Sets variables, which are passed to the execution when a predefined behavior is used.
   * 
   * @param variables A map of variables to set.
   * 
   * @return The handler.
   * 
   * @see #complete()
   * @see #handleBpmnError(String, String)
   */
  public ExternalTaskHandler withVariables(Map<String, Object> variables) {
    this.variables.putAll(variables);
    return this;
  }

  /**
   * Sets a typed variable, which is passed to the execution when a predefined behavior is used.
   * 
   * @param name The name of the variable.
   * 
   * @param value The variable's typed value.
   * 
   * @return The handler.
   * 
   * @see #complete()
   * @see #handleBpmnError(String, String)
   */
  public ExternalTaskHandler withVariableTyped(String name, TypedValue value) {
    variables.putValueTyped(name, value);
    return this;
  }

  /**
   * Wraps the given {@link ExternalTask} into a {@link LockedExternalTask}.
   * 
   * @param task An external task that has been queried by the handler.
   * 
   * @return The wrapped task.
   */
  protected LockedExternalTask wrap(ExternalTask task) {
    String errorDetails = processEngine.getExternalTaskService().getExternalTaskErrorDetails(task.getId());
    Map<String, Object> variables = processEngine.getRuntimeService().getVariables(task.getExecutionId());
    Map<String, Object> localVariables = processEngine.getRuntimeService().getVariablesLocal(task.getExecutionId());

    return new WrappedTask(task, errorDetails, variables, localVariables);
  }

  /**
   * Interal class that represents an {@link ExternalTask} as {@link LockedExternalTask}, since those
   * interfaces are not inherited and most of the worker implementations deal with instances of
   * {@link LockedExternalTask}s.
   */
  private static class WrappedTask implements LockedExternalTask {

    private final ExternalTask task;
    private final String errorDetails;
    private final VariableMap variables;

    private WrappedTask(ExternalTask task, String errorDetails, Map<String, Object> variables, Map<String, Object> localVariables) {
      this.task = task;
      this.errorDetails = errorDetails;
      this.variables = Variables.createVariables();
      this.variables.putAll(variables);
      this.variables.putAll(localVariables);
    }

    @Override
    public String getId() {
      return task.getId();
    }

    @Override
    public String getTopicName() {
      return task.getTopicName();
    }

    @Override
    public String getWorkerId() {
      return task.getWorkerId();
    }

    @Override
    public Date getLockExpirationTime() {
      return task.getLockExpirationTime();
    }

    @Override
    public Date getCreateTime() {
      return task.getCreateTime();
    }

    @Override
    public String getProcessInstanceId() {
      return task.getProcessInstanceId();
    }

    @Override
    public String getExecutionId() {
      return task.getExecutionId();
    }

    @Override
    public String getActivityId() {
      return task.getActivityId();
    }

    @Override
    public String getActivityInstanceId() {
      return task.getActivityInstanceId();
    }

    @Override
    public String getProcessDefinitionId() {
      return task.getProcessDefinitionId();
    }

    @Override
    public String getProcessDefinitionKey() {
      return task.getProcessDefinitionKey();
    }

    @Override
    public String getProcessDefinitionVersionTag() {
      return task.getProcessDefinitionVersionTag();
    }

    @Override
    public Integer getRetries() {
      return task.getRetries();
    }

    @Override
    public String getErrorMessage() {
      return task.getErrorMessage();
    }

    @Override
    public String getErrorDetails() {
      return errorDetails;
    }

    @Override
    public VariableMap getVariables() {
      return variables;
    }

    @Override
    public String getTenantId() {
      return task.getTenantId();
    }

    @Override
    public long getPriority() {
      return task.getPriority();
    }

    @Override
    public String getBusinessKey() {
      return task.getBusinessKey();
    }

    @Override
    public Map<String, String> getExtensionProperties() {
      return task.getExtensionProperties();
    }
  }

  /**
   * Internal class that wraps an action that accepts {@link LockedExternalTask}s, so that it can work
   * with {@link ExternalTask}s, which has been queried and locked by the handler.
   */
  private static class WrappedTaskAction implements Consumer<ExternalTask> {

    private final ExternalTaskHandler handler;
    private final Consumer<LockedExternalTask> action;

    private WrappedTaskAction(ExternalTaskHandler handler, Consumer<LockedExternalTask> action) {
      this.handler = handler;
      this.action = action;
    }

    @Override
    public void accept(ExternalTask task) {
      action.accept(handler.wrap(task));
    }
  }
}
