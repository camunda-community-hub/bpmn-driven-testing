package org.camunda.community.bpmndt.api;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.camunda.bpm.client.spi.DataFormat;
import org.camunda.bpm.client.spi.DataFormatConfigurator;
import org.camunda.bpm.client.spi.DataFormatProvider;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.client.variable.impl.DefaultValueMappers;
import org.camunda.bpm.client.variable.impl.TypedValueField;
import org.camunda.bpm.client.variable.impl.ValueMapper;
import org.camunda.bpm.client.variable.impl.ValueMappers;
import org.camunda.bpm.client.variable.impl.mapper.BooleanValueMapper;
import org.camunda.bpm.client.variable.impl.mapper.ByteArrayValueMapper;
import org.camunda.bpm.client.variable.impl.mapper.DateValueMapper;
import org.camunda.bpm.client.variable.impl.mapper.DoubleValueMapper;
import org.camunda.bpm.client.variable.impl.mapper.IntegerValueMapper;
import org.camunda.bpm.client.variable.impl.mapper.JsonValueMapper;
import org.camunda.bpm.client.variable.impl.mapper.LongValueMapper;
import org.camunda.bpm.client.variable.impl.mapper.NullValueMapper;
import org.camunda.bpm.client.variable.impl.mapper.ObjectValueMapper;
import org.camunda.bpm.client.variable.impl.mapper.ShortValueMapper;
import org.camunda.bpm.client.variable.impl.mapper.StringValueMapper;
import org.camunda.bpm.client.variable.impl.mapper.XmlValueMapper;
import org.camunda.bpm.client.variable.value.JsonValue;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.spin.impl.json.jackson.JacksonJsonNode;
import org.camunda.spin.plugin.variable.SpinValues;

/**
 * Fluent API to handle external tasks, implemented through an external task client.
 *
 * @param <T> The handler implementation.
 */
public class ExternalTaskClientHandler<T extends ExternalTaskHandler<?>> extends ExternalTaskHandler<ExternalTaskClientHandler<?>> {

  @SuppressWarnings({"rawtypes"})
  private static ValueMappers VALUE_MAPPERS;

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

    @SuppressWarnings("rawtypes")
    private final ValueMappers valueMappers;

    private TaskAdapter(ExternalTaskClientHandler<?> handler, LockedExternalTask task) {
      this.handler = handler;
      this.task = task;

      valueMappers = getOrCreateValueMappers(handler.processEngine);
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
        return (T) convertValue(runtimeService.getVariableLocal(task.getExecutionId(), variableName));
      } else {
        return (T) convertValue(runtimeService.getVariable(task.getExecutionId(), variableName));
      }
    }

    @Override
    public <T extends TypedValue> T getVariableTyped(String variableName) {
      return getVariableTyped(variableName, true);
    }

    @Override
    public <T extends TypedValue> T getVariableTyped(String variableName, boolean deserializeObjectValue) {
      RuntimeService runtimeService = handler.processEngine.getRuntimeService();

      T variable;
      if (handler.fetchLocalVariablesOnly) {
        variable = runtimeService.getVariableLocalTyped(task.getExecutionId(), variableName, deserializeObjectValue);
      } else {
        variable = runtimeService.getVariableTyped(task.getExecutionId(), variableName, deserializeObjectValue);
      }

      return convertTypedValue(variable, deserializeObjectValue);
    }

    @Override
    public Map<String, Object> getAllVariables() {
      RuntimeService runtimeService = handler.processEngine.getRuntimeService();
      if (handler.fetchLocalVariablesOnly) {
        return convert(runtimeService.getVariablesLocal(task.getExecutionId()));
      } else {
        return convert(runtimeService.getVariables(task.getExecutionId()));
      }
    }

    @Override
    public VariableMap getAllVariablesTyped() {
      return getAllVariablesTyped(true);
    }

    @Override
    public VariableMap getAllVariablesTyped(boolean deserializeObjectValues) {
      RuntimeService runtimeService = handler.processEngine.getRuntimeService();

      VariableMap variables;
      if (handler.fetchLocalVariablesOnly) {
        variables = runtimeService.getVariablesLocalTyped(task.getExecutionId(), deserializeObjectValues);
      } else {
        variables = runtimeService.getVariablesTyped(task.getExecutionId(), deserializeObjectValues);
      }

      if (deserializeObjectValues) {
        return convert(variables);
      } else {
        return variables;
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

    private VariableMap convert(Map<String, Object> variables) {
      VariableMap convertedVariables = Variables.createVariables();
      for (Map.Entry<String, Object> variable : variables.entrySet()) {
        Object value = variable.getValue();
        if (value instanceof TypedValue) {
          convertedVariables.put(variable.getKey(), convertTypedValue((TypedValue) value, true));
        } else {
          convertedVariables.put(variable.getKey(), value);
        }
      }
      return convertedVariables;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T extends TypedValue> T convertTypedValue(TypedValue value, boolean deserializeValue) {
      TypedValueField typedValueField = new TypedValueField();
      typedValueField.setType(value.getType().getName());
      typedValueField.setValueInfo(value.getType().getValueInfo(value));

      if (value instanceof org.camunda.spin.plugin.variable.value.JsonValue) {
        typedValueField.setValue(value.getValue() != null ? value.getValue().toString() : null);
      } else {
        typedValueField.setValue(value.getValue());
      }

      ValueMapper valueMapper = valueMappers.findMapperForTypedValueField(typedValueField);

      return (T) valueMapper.readValue(typedValueField, deserializeValue);
    }

    private Object convertValue(Object value) {
      if (value instanceof JacksonJsonNode) {
        return ((JacksonJsonNode) value).mapTo(Object.class);
      } else if (value instanceof TypedValue) {
        return convertTypedValue((TypedValue) value, true);
      } else {
        return value;
      }
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
      runtimeService.setVariables(processInstanceId, convert(variables));
    }

    @Override
    public void setVariables(ExternalTask externalTask, Map<String, Object> variables) {
      runtimeService.setVariables(externalTask.getProcessInstanceId(), convert(variables));
    }

    @Override
    public void complete(ExternalTask externalTask, Map<String, Object> variables) {
      externalTaskService.complete(externalTask.getId(), WORKER_ID, convert(variables));
    }

    @Override
    public void complete(ExternalTask externalTask, Map<String, Object> variables, Map<String, Object> localVariables) {
      externalTaskService.complete(externalTask.getId(), WORKER_ID, convert(variables), convert(localVariables));
    }

    @Override
    public void complete(String externalTaskId, Map<String, Object> variables, Map<String, Object> localVariables) {
      externalTaskService.complete(externalTaskId, WORKER_ID, convert(variables), convert(localVariables));
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
      externalTaskService.handleFailure(
          externalTaskId,
          WORKER_ID,
          errorMessage,
          errorDetails,
          retries,
          retryTimeout,
          convert(variables),
          convert(localVariables)
      );
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
      externalTaskService.handleBpmnError(externalTask.getId(), WORKER_ID, errorCode, errorMessage, convert(variables));
    }

    @Override
    public void handleBpmnError(String externalTaskId, String errorCode, String errorMessage, Map<String, Object> variables) {
      externalTaskService.handleBpmnError(externalTaskId, WORKER_ID, errorCode, errorMessage, convert(variables));
    }

    @Override
    public void extendLock(ExternalTask externalTask, long newDuration) {
      externalTaskService.extendLock(externalTask.getId(), WORKER_ID, newDuration);
    }

    @Override
    public void extendLock(String externalTaskId, long newDuration) {
      externalTaskService.extendLock(externalTaskId, WORKER_ID, newDuration);
    }

    private Map<String, Object> convert(Map<String, Object> variables) {
      Map<String, Object> convertedVariables = new HashMap<>();
      for (Map.Entry<String, Object> entry : variables.entrySet()) {
        Object value = entry.getValue();
        if (value instanceof TypedValue) {
          convertedVariables.put(entry.getKey(), convertTypedValue((TypedValue) value));
        } else {
          convertedVariables.put(entry.getKey(), value);
        }
      }
      return convertedVariables;
    }

    private Object convertTypedValue(TypedValue value) {
      if (value instanceof JsonValue) {
        return SpinValues.jsonValue(((JsonValue) value).getValue()).create();
      } else {
        return value;
      }
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

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static ValueMappers getOrCreateValueMappers(ProcessEngine processEngine) {
    if (VALUE_MAPPERS != null) {
      return VALUE_MAPPERS;
    }

    String defaultSerializationFormat = ((ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration()).getDefaultSerializationFormat();

    VALUE_MAPPERS = new DefaultValueMappers<>(defaultSerializationFormat);

    // see ExternalTaskClientBuilderImpl
    VALUE_MAPPERS.addMapper(new NullValueMapper());
    VALUE_MAPPERS.addMapper(new BooleanValueMapper());
    VALUE_MAPPERS.addMapper(new StringValueMapper());
    VALUE_MAPPERS.addMapper(new DateValueMapper("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
    VALUE_MAPPERS.addMapper(new ByteArrayValueMapper());

    VALUE_MAPPERS.addMapper(new IntegerValueMapper());
    VALUE_MAPPERS.addMapper(new LongValueMapper());
    VALUE_MAPPERS.addMapper(new ShortValueMapper());
    VALUE_MAPPERS.addMapper(new DoubleValueMapper());

    Map<String, DataFormat> dataFormats = lookupDataFormats();
    dataFormats.forEach((key, format) -> {
      VALUE_MAPPERS.addMapper(new ObjectValueMapper(key, format));
    });

    VALUE_MAPPERS.addMapper(new JsonValueMapper());
    VALUE_MAPPERS.addMapper(new XmlValueMapper());

    return VALUE_MAPPERS;
  }

  private static Map<String, DataFormat> lookupDataFormats() {
    Map<String, DataFormat> dataFormats = new HashMap<>();

    lookupCustomDataFormats(dataFormats);
    applyConfigurators(dataFormats);

    return dataFormats;
  }

  private static void lookupCustomDataFormats(Map<String, DataFormat> dataFormats) {
    // use java.util.ServiceLoader to load custom DataFormatProvider instances on the classpath
    ServiceLoader<DataFormatProvider> providerLoader = ServiceLoader.load(DataFormatProvider.class);

    for (DataFormatProvider provider : providerLoader) {
      lookupProvider(dataFormats, provider);
    }
  }

  private static void lookupProvider(Map<String, DataFormat> dataFormats, DataFormatProvider provider) {

    String dataFormatName = provider.getDataFormatName();

    if (!dataFormats.containsKey(dataFormatName)) {
      try {
        DataFormat dataFormatInstance = provider.createInstance();
        dataFormats.put(dataFormatName, dataFormatInstance);
      } catch (Throwable e) {
        // ignore
      }
    }
  }

  @SuppressWarnings("rawtypes")
  private static void applyConfigurators(Map<String, DataFormat> dataFormats) {
    ServiceLoader<DataFormatConfigurator> configuratorLoader = ServiceLoader.load(DataFormatConfigurator.class);

    for (DataFormatConfigurator configurator : configuratorLoader) {
      applyConfigurator(dataFormats, configurator);
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static void applyConfigurator(Map<String, DataFormat> dataFormats, DataFormatConfigurator configurator) {
    for (DataFormat dataFormat : dataFormats.values()) {
      if (configurator.getDataFormatClass().isAssignableFrom(dataFormat.getClass())) {
        configurator.configure(dataFormat);
      }
    }
  }
}
