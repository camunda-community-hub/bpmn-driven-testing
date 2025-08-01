package org.camunda.community.bpmndt.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryTopicBuilder;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ExternalTaskAssert;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * Fluent API to handle external tasks.
 *
 * @param <T> The handler implementation.
 */
public class ExternalTaskHandler<T extends ExternalTaskHandler<?>> {

  protected static final String WORKER_ID = "bpmndt-worker";

  protected final ProcessEngine processEngine;
  private final String activityId;
  private final String topicName;

  private final VariableMap variables;
  private final VariableMap localVariables;

  private String errorCode;
  private String errorMessage;

  private BiConsumer<ProcessInstanceAssert, String> verifier;
  private BiConsumer<ExternalTaskAssert, Map<String, Object>> taskVerifier;

  private Consumer<String> action;
  private Consumer<LockedExternalTask> taskAction;

  protected boolean fetchExtensionProperties;
  protected boolean fetchLocalVariablesOnly;
  private long lockDuration;
  private String workerId;

  public ExternalTaskHandler(ProcessEngine processEngine, String activityId, String topicName) {
    this.processEngine = processEngine;
    this.activityId = activityId;
    this.topicName = topicName;

    variables = Variables.createVariables();
    localVariables = Variables.createVariables();

    taskAction = this::complete;

    // defaults as described here
    // https://docs.camunda.org/manual/latest/user-guide/ext-client/spring-boot-starter/#client-bootstrapping
    // https://docs.camunda.org/manual/latest/user-guide/ext-client/spring-boot-starter/#topic-subscription-1
    fetchExtensionProperties = false;
    fetchLocalVariablesOnly = false;
    lockDuration = TimeUnit.SECONDS.toMillis(20L);

    workerId = WORKER_ID;
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
      LockedExternalTask task = queryAndLock(pi);
      taskAction.accept(task);
    }
  }

  /**
   * Completes the locked external task by calling {@code complete}, using the specified variables and local variables, when the process instance is waiting at
   * the corresponding activity. Please note: this is the default behavior.
   *
   * @see ExternalTaskService#complete(String, String, java.util.Map, java.util.Map)
   */
  public void complete() {
    taskAction = this::complete;
    action = null;
  }

  protected void complete(LockedExternalTask task) {
    processEngine.getExternalTaskService().complete(task.getId(), task.getWorkerId(), variables, localVariables);
  }

  /**
   * Customizes the handler, using the given {@link Consumer} function. This method can be used to apply a common customization needed for different test
   * cases.
   *
   * <pre>
   * tc.handleExternalTask().customize(this::prepareExternalTask);
   * </pre>
   *
   * @param customizer A function that accepts a {@link ExternalTaskHandler}.
   * @return The handler.
   */
  @SuppressWarnings("unchecked")
  public T customize(Consumer<T> customizer) {
    if (customizer != null) {
      customizer.accept((T) this);
    }
    return (T) this;
  }

  /**
   * Executes a custom action that handles the external task, when the process instance is waiting at the corresponding activity.
   *
   * @param action A specific action that accepts the related topic name (String).
   */
  public void execute(Consumer<String> action) {
    this.action = action;
    this.taskAction = null;
  }

  /**
   * Executes a custom action that handles the external task, which has been queried (by process instance ID, activity ID and topic name) and locked before,
   * when the process instance is waiting at the corresponding activity.
   *
   * @param taskAction A specific action that accepts an {@link ExternalTask}.
   * @see ExternalTaskService#lock(String, String, long)
   */
  public void executeExternalTask(Consumer<ExternalTask> taskAction) {
    this.taskAction = new TaskActionAdapter(taskAction);
    this.action = null;
  }

  /**
   * Executes a custom action that handles the external task, which has been queried (by process instance ID, activity ID and topic name) and locked before,
   * when the process instance is waiting at the corresponding activity.
   *
   * @param taskAction A specific action that accepts an {@link LockedExternalTask}.
   * @see ExternalTaskService#lock(String, String, long)
   */
  public void executeLockedExternalTask(Consumer<LockedExternalTask> taskAction) {
    this.taskAction = taskAction;
    this.action = null;
  }

  /**
   * Continues the execution with an action that calls {@code handleBpmnError} using the given error code and message as well as the specified variables.
   *
   * @param errorCode    The error code of the attached boundary error event.
   * @param errorMessage An error message or {@code null}.
   * @see ExternalTaskService#handleBpmnError(String, String, String, String, java.util.Map)
   */
  public void handleBpmnError(String errorCode, String errorMessage) {
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;

    taskAction = this::handleBpmnError;
    action = null;
  }

  protected void handleBpmnError(LockedExternalTask task) {
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

  private LockedExternalTask queryAndLock(ProcessInstance pi) {
    ExternalTask externalTask = query(pi);

    ExternalTaskService externalTaskService = processEngine.getExternalTaskService();

    ExternalTaskQueryTopicBuilder externalTaskQueryTopicBuilder = externalTaskService.fetchAndLock()
        .maxTasks(Integer.MAX_VALUE)
        .workerId(workerId)
        .subscribe()
        .topic(topicName, lockDuration)
        .enableCustomObjectDeserialization();

    if (fetchExtensionProperties) {
      externalTaskQueryTopicBuilder.includeExtensionProperties();
    }

    if (fetchLocalVariablesOnly) {
      externalTaskQueryTopicBuilder.localVariables();
    }

    List<LockedExternalTask> lockedExternalTasks = externalTaskQueryTopicBuilder.execute();
    if (lockedExternalTasks.isEmpty()) {
      String msg = String.format("Expected to fetch and lock at least one external task for activity '%s' and topic '%s'", activityId, topicName);
      throw new AssertionError(msg);
    }

    LockedExternalTask expected = null;
    for (LockedExternalTask lockedExternalTask : lockedExternalTasks) {
      // find expected external task
      if (lockedExternalTask.getId().equals(externalTask.getId())) {
        expected = lockedExternalTask;
        continue;
      }

      // unlock all other external tasks
      externalTaskService.unlock(lockedExternalTask.getId());
    }

    if (expected == null) {
      String msg = String.format("External task for activity '%s' and topic '%s' could not be fetched and locked", activityId, topicName);
      throw new AssertionError(msg);
    }

    return expected;
  }

  /**
   * Verifies the external task's waiting state.
   *
   * @param verifier Verifier that accepts an {@link ProcessInstanceAssert} instance and the related topic name (String).
   * @return The handler.
   */
  @SuppressWarnings("unchecked")
  public T verify(BiConsumer<ProcessInstanceAssert, String> verifier) {
    this.verifier = verifier;
    return (T) this;
  }

  /**
   * Verifies the external task's waiting state.
   *
   * @param taskVerifier Verifier that accepts an {@link ExternalTaskAssert} instance and the task's local variables.
   * @return The handler.
   */
  @SuppressWarnings("unchecked")
  public T verifyTask(BiConsumer<ExternalTaskAssert, Map<String, Object>> taskVerifier) {
    this.taskVerifier = taskVerifier;
    return (T) this;
  }

  /**
   * Applies no action at the external task's wait state. This is required to wait for events (e.g. message, signal or timer events) that are attached as
   * boundary events on the activity itself or on the surrounding scope (e.g. embedded subprocess).
   */
  public void waitForBoundaryEvent() {
    action = null;
    taskAction = null;
  }

  /**
   * Sets the error message, which is used when the next activity is an error boundary event - in this case the handler's action is {@code handleBpmnError}.
   *
   * @param errorMessage An error message or {@code null}.
   * @return The handler.
   * @see #handleBpmnError(String, String)
   */
  @SuppressWarnings("unchecked")
  public T withErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
    return (T) this;
  }

  /**
   * Specifies if extension properties should be included when an external task is fetched - default: {@code false}
   *
   * @param fetchExtensionProperties Include extension properties, if available, or not.
   * @return The handler.
   * @see ExternalTaskQueryTopicBuilder#includeExtensionProperties()
   */
  @SuppressWarnings("unchecked")
  public T withFetchExtensionProperties(boolean fetchExtensionProperties) {
    this.fetchExtensionProperties = fetchExtensionProperties;
    return (T) this;
  }

  /**
   * Specifies if only local variables should be fetched when an external task is fetched - default: {@code false}
   *
   * @param fetchLocalVariablesOnly Include only variables of the external task itself, but no variables of parent scopes.
   * @return The handler.
   * @see ExternalTaskQueryTopicBuilder#localVariables()
   */
  @SuppressWarnings("unchecked")
  public T withFetchLocalVariablesOnly(boolean fetchLocalVariablesOnly) {
    this.fetchLocalVariablesOnly = fetchLocalVariablesOnly;
    return (T) this;
  }

  /**
   * Sets a local variable, which is passed to the execution when the default behavior is used.
   *
   * @param name  The name of the local variable.
   * @param value The local variable's value.
   * @return The handler.
   * @see #complete()
   */
  @SuppressWarnings("unchecked")
  public T withLocalVariable(String name, Object value) {
    localVariables.putValue(name, value);
    return (T) this;
  }

  /**
   * Sets local variables, which are passed to the execution when the default behavior is used.
   *
   * @param localVariables A map of local variables to set.
   * @return The handler.
   * @see #complete()
   */
  @SuppressWarnings("unchecked")
  public T withLocalVariables(Map<String, Object> localVariables) {
    this.localVariables.putAll(localVariables);
    return (T) this;
  }

  /**
   * Sets a typed local variable, which is passed to the execution when the default behavior is used.
   *
   * @param name  The name of the local variable.
   * @param value The local variable's typed value.
   * @return The handler.
   * @see #complete()
   */
  @SuppressWarnings("unchecked")
  public T withLocalVariableTyped(String name, TypedValue value) {
    localVariables.putValueTyped(name, value);
    return (T) this;
  }

  /**
   * Sets the duration in milliseconds for which the external task should be locked - default: {@code 20_000}
   *
   * @param lockDuration The lock duration to use, when fetching and locking an external task.
   * @return The handler.
   * @see ExternalTaskQueryTopicBuilder#topic(String, long)
   */
  @SuppressWarnings("unchecked")
  public T withLockDuration(long lockDuration) {
    this.lockDuration = lockDuration;
    return (T) this;
  }

  /**
   * Sets a variable, which is passed to the execution when a predefined behavior is used.
   *
   * @param name  The name of the variable.
   * @param value The variable's value.
   * @return The handler.
   * @see #complete()
   * @see #handleBpmnError(String, String)
   */
  @SuppressWarnings("unchecked")
  public T withVariable(String name, Object value) {
    variables.putValue(name, value);
    return (T) this;
  }

  /**
   * Sets variables, which are passed to the execution when a predefined behavior is used.
   *
   * @param variables A map of variables to set.
   * @return The handler.
   * @see #complete()
   * @see #handleBpmnError(String, String)
   */
  @SuppressWarnings("unchecked")
  public T withVariables(Map<String, Object> variables) {
    this.variables.putAll(variables);
    return (T) this;
  }

  /**
   * Sets a typed variable, which is passed to the execution when a predefined behavior is used.
   *
   * @param name  The name of the variable.
   * @param value The variable's typed value.
   * @return The handler.
   * @see #complete()
   * @see #handleBpmnError(String, String)
   */
  @SuppressWarnings("unchecked")
  public T withVariableTyped(String name, TypedValue value) {
    variables.putValueTyped(name, value);
    return (T) this;
  }

  /**
   * Sets a custom worker ID, which is used for {@code lock}, {@code complete} and {@code handleBpmnError} {@link ExternalTaskService} calls. If not set, the
   * default value "bpmndt-worker" is used.
   *
   * @param workerId A specific worker ID to use.
   * @return The handler.
   */
  @SuppressWarnings("unchecked")
  public T withWorkerId(String workerId) {
    if (workerId != null && !workerId.isBlank()) {
      this.workerId = workerId;
    }
    return (T) this;
  }

  /**
   * Adapter class for a task action that accepts {@link ExternalTask}s.
   */
  private static class TaskActionAdapter implements Consumer<LockedExternalTask> {

    private final Consumer<ExternalTask> taskAction;

    private TaskActionAdapter(Consumer<ExternalTask> taskAction) {
      this.taskAction = taskAction;
    }

    @Override
    public void accept(LockedExternalTask task) {
      ExternalTaskEntity externalTask = new ExternalTaskEntity();
      externalTask.setActivityId(task.getActivityId());
      externalTask.setActivityInstanceId(task.getActivityInstanceId());
      externalTask.setBusinessKey(task.getBusinessKey());
      externalTask.setCreateTime(task.getCreateTime());
      externalTask.setErrorMessage(task.getErrorMessage());
      externalTask.setExecutionId(task.getExecutionId());
      externalTask.setExtensionProperties(task.getExtensionProperties());
      externalTask.setId(task.getId());
      externalTask.setLockExpirationTime(task.getLockExpirationTime());
      externalTask.setPriority(task.getPriority());
      externalTask.setProcessDefinitionId(task.getProcessDefinitionId());
      externalTask.setProcessDefinitionKey(task.getProcessDefinitionKey());
      externalTask.setProcessDefinitionVersionTag(task.getProcessDefinitionVersionTag());
      externalTask.setProcessInstanceId(task.getProcessInstanceId());
      externalTask.setRetries(task.getRetries());
      externalTask.setTenantId(task.getTenantId());
      externalTask.setTopicName(task.getTopicName());
      externalTask.setWorkerId(task.getWorkerId());

      taskAction.accept(externalTask);
    }
  }
}
