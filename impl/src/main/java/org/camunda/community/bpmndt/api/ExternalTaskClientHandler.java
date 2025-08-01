package org.camunda.community.bpmndt.api;

import java.util.Date;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * Fluent API to handle external tasks, implemented through an external task client.
 *
 * @param <T> The handler implementation.
 */
public class ExternalTaskClientHandler<T extends ExternalTaskHandler<?>> extends ExternalTaskHandler<ExternalTaskClientHandler<?>> {

  public ExternalTaskClientHandler(ProcessEngine processEngine, String activityId, String topicName) {
    super(processEngine, activityId, topicName);
  }

  /**
   * Executes a custom action that handles the external task, which has been queried (by process instance ID, activity ID and topic name) and locked before,
   * when the process instance is waiting at the corresponding activity.
   * <br>
   * Please note: This method must be used when the external task has been implemented using an external task client.
   *
   * @param action A specific action that accepts {@link ExternalTask} and {@link ExternalTaskService}.
   */
  public void executeExternalTask(BiConsumer<ExternalTask, ExternalTaskService> action) {
    executeLockedExternalTask(new TaskActionAdapter(this, action));
  }

  /**
   * Adapter class for external tasks of an external task client.
   */
  private static class TaskAdapter implements ExternalTask {

    private final ExternalTaskClientHandler<?> handler;
    private final LockedExternalTask task;

    private TaskAdapter(ExternalTaskClientHandler<?> handler, LockedExternalTask task) {
      this.handler = handler;
      this.task = task;
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
    public String getErrorMessage() {
      return task.getErrorMessage();
    }

    @Override
    public String getErrorDetails() {
      return handler.processEngine.getExternalTaskService().getExternalTaskErrorDetails(task.getId());
    }

    @Override
    public String getExecutionId() {
      return task.getExecutionId();
    }

    @Override
    public String getId() {
      return task.getId();
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
    public String getProcessInstanceId() {
      return task.getProcessInstanceId();
    }

    @Override
    public Integer getRetries() {
      return task.getRetries();
    }

    @Override
    public String getWorkerId() {
      return task.getWorkerId();
    }

    @Override
    public String getTopicName() {
      return task.getTopicName();
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
    @SuppressWarnings("unchecked")
    public <T> T getVariable(String variableName) {
      RuntimeService runtimeService = handler.processEngine.getRuntimeService();
      if (handler.fetchLocalVariablesOnly) {
        return (T) runtimeService.getVariableLocal(task.getExecutionId(), variableName);
      } else {
        return (T) runtimeService.getVariable(task.getExecutionId(), variableName);
      }
    }

    @Override
    public <T extends TypedValue> T getVariableTyped(String variableName) {
      RuntimeService runtimeService = handler.processEngine.getRuntimeService();
      if (handler.fetchLocalVariablesOnly) {
        return runtimeService.getVariableLocalTyped(task.getExecutionId(), variableName);
      } else {
        return runtimeService.getVariableTyped(task.getExecutionId(), variableName);
      }
    }

    @Override
    public <T extends TypedValue> T getVariableTyped(String variableName, boolean deserializeObjectValue) {
      RuntimeService runtimeService = handler.processEngine.getRuntimeService();
      if (handler.fetchLocalVariablesOnly) {
        return runtimeService.getVariableLocalTyped(task.getExecutionId(), variableName, deserializeObjectValue);
      } else {
        return runtimeService.getVariableTyped(task.getExecutionId(), variableName, deserializeObjectValue);
      }
    }

    @Override
    public Map<String, Object> getAllVariables() {
      RuntimeService runtimeService = handler.processEngine.getRuntimeService();
      if (handler.fetchLocalVariablesOnly) {
        return runtimeService.getVariablesLocal(task.getExecutionId());
      } else {
        return runtimeService.getVariables(task.getExecutionId());
      }
    }

    @Override
    public VariableMap getAllVariablesTyped() {
      RuntimeService runtimeService = handler.processEngine.getRuntimeService();
      if (handler.fetchLocalVariablesOnly) {
        return runtimeService.getVariablesLocalTyped(task.getExecutionId());
      } else {
        return runtimeService.getVariablesTyped(task.getExecutionId());
      }
    }

    @Override
    public VariableMap getAllVariablesTyped(boolean deserializeObjectValues) {
      RuntimeService runtimeService = handler.processEngine.getRuntimeService();
      if (handler.fetchLocalVariablesOnly) {
        return runtimeService.getVariablesLocalTyped(task.getExecutionId(), deserializeObjectValues);
      } else {
        return runtimeService.getVariablesTyped(task.getExecutionId(), deserializeObjectValues);
      }
    }

    @Override
    public String getBusinessKey() {
      return task.getBusinessKey();
    }

    @Override
    public String getExtensionProperty(String propertyKey) {
      return task.getExtensionProperties().get(propertyKey);
    }

    @Override
    public Map<String, String> getExtensionProperties() {
      return task.getExtensionProperties();
    }
  }

  /**
   * Adapter class for an external task service of an external task client.
   */
  private static class TaskServiceAdapter implements ExternalTaskService {

    private final org.camunda.bpm.engine.ExternalTaskService externalTaskService;
    private final RuntimeService runtimeService;

    private TaskServiceAdapter(ProcessEngine processEngine) {
      externalTaskService = processEngine.getExternalTaskService();
      runtimeService = processEngine.getRuntimeService();
    }

    @Override
    public void lock(String externalTaskId, long lockDuration) {
      externalTaskService.lock(externalTaskId, WORKER_ID, lockDuration);
    }

    @Override
    public void lock(ExternalTask externalTask, long lockDuration) {
      externalTaskService.lock(externalTask.getId(), WORKER_ID, lockDuration);
    }

    @Override
    public void unlock(ExternalTask externalTask) {
      externalTaskService.unlock(externalTask.getId());
    }

    @Override
    public void complete(ExternalTask externalTask) {
      externalTaskService.complete(externalTask.getId(), WORKER_ID);
    }

    @Override
    public void setVariables(String processInstanceId, Map<String, Object> variables) {
      runtimeService.setVariables(processInstanceId, variables);
    }

    @Override
    public void setVariables(ExternalTask externalTask, Map<String, Object> variables) {
      runtimeService.setVariables(externalTask.getProcessInstanceId(), variables);
    }

    @Override
    public void complete(ExternalTask externalTask, Map<String, Object> variables) {
      externalTaskService.complete(externalTask.getId(), WORKER_ID, variables);
    }

    @Override
    public void complete(ExternalTask externalTask, Map<String, Object> variables, Map<String, Object> localVariables) {
      externalTaskService.complete(externalTask.getId(), WORKER_ID, variables, localVariables);
    }

    @Override
    public void complete(String externalTaskId, Map<String, Object> variables, Map<String, Object> localVariables) {
      externalTaskService.complete(externalTaskId, WORKER_ID, variables, localVariables);
    }

    @Override
    public void handleFailure(ExternalTask externalTask, String errorMessage, String errorDetails, int retries, long retryTimeout) {
      externalTaskService.handleFailure(externalTask.getId(), WORKER_ID, errorMessage, errorDetails, retries, retryTimeout);
    }

    @Override
    public void handleFailure(String externalTaskId, String errorMessage, String errorDetails, int retries, long retryTimeout) {
      externalTaskService.handleFailure(externalTaskId, WORKER_ID, errorMessage, errorDetails, retries, retryTimeout);
    }

    @Override
    public void handleFailure(
        String externalTaskId,
        String errorMessage,
        String errorDetails,
        int retries,
        long retryTimeout,
        Map<String, Object> variables,
        Map<String, Object> localVariables
    ) {
      externalTaskService.handleFailure(externalTaskId, WORKER_ID, errorMessage, errorDetails, retries, retryTimeout, variables, localVariables);
    }

    @Override
    public void handleBpmnError(ExternalTask externalTask, String errorCode) {
      externalTaskService.handleBpmnError(externalTask.getId(), WORKER_ID, errorCode);
    }

    @Override
    public void handleBpmnError(ExternalTask externalTask, String errorCode, String errorMessage) {
      externalTaskService.handleBpmnError(externalTask.getId(), WORKER_ID, errorCode, errorMessage);
    }

    @Override
    public void handleBpmnError(ExternalTask externalTask, String errorCode, String errorMessage, Map<String, Object> variables) {
      externalTaskService.handleBpmnError(externalTask.getId(), WORKER_ID, errorCode, errorMessage, variables);
    }

    @Override
    public void handleBpmnError(String externalTaskId, String errorCode, String errorMessage, Map<String, Object> variables) {
      externalTaskService.handleBpmnError(externalTaskId, WORKER_ID, errorCode, errorMessage, variables);
    }

    @Override
    public void extendLock(ExternalTask externalTask, long newDuration) {
      externalTaskService.extendLock(externalTask.getId(), WORKER_ID, newDuration);
    }

    @Override
    public void extendLock(String externalTaskId, long newDuration) {
      externalTaskService.extendLock(externalTaskId, WORKER_ID, newDuration);
    }
  }

  /**
   * Adapter class for a task action that accepts {@link ExternalTask} and {@link ExternalTaskService}.
   */
  private static class TaskActionAdapter implements Consumer<LockedExternalTask> {

    private final ExternalTaskClientHandler<?> handler;
    private final BiConsumer<ExternalTask, ExternalTaskService> action;

    private TaskActionAdapter(ExternalTaskClientHandler<?> handler, BiConsumer<ExternalTask, ExternalTaskService> action) {
      this.handler = handler;
      this.action = action;
    }

    @Override
    public void accept(LockedExternalTask task) {
      action.accept(new TaskAdapter(handler, task), new TaskServiceAdapter(handler.processEngine));
    }
  }
}
