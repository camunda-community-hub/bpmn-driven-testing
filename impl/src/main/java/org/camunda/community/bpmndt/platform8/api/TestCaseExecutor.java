package org.camunda.community.bpmndt.platform8.api;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.JsonMapper;
import io.camunda.zeebe.client.api.command.CreateProcessInstanceCommandStep1.CreateProcessInstanceCommandStep3;
import io.camunda.zeebe.client.api.command.DeployResourceCommandStep1;
import io.camunda.zeebe.client.api.command.DeployResourceCommandStep1.DeployResourceCommandStep2;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.impl.ZeebeObjectMapper;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.filters.RecordStream;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordType;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;
import io.camunda.zeebe.protocol.record.value.BpmnElementType;
import io.camunda.zeebe.protocol.record.value.ProcessInstanceRecordValue;

/**
 * Fluent API to prepare and start the actual test case execution.
 */
public class TestCaseExecutor {

  private final AbstractTestCase testCase;
  private final ZeebeTestEngine engine;

  private final Map<String, Object> variableMap = new HashMap<>();

  private ObjectMapper objectMapper;
  private String tenantId;
  private Object variables;
  private Consumer<ProcessInstanceAssert> verifier;

  public TestCaseExecutor(AbstractTestCase testCase, ZeebeTestEngine engine) {
    this.testCase = testCase;
    this.engine = engine;

    BpmnAssert.initRecordStream(RecordStream.of(engine.getRecordStreamSource()));
  }

  /**
   * Customizes the executor, using the given {@link Consumer} function. This method can be used to apply a common customization needed for different test
   * cases.
   *
   * <pre>
   * tc.createExecutor().customize(this::prepareVariables).execute();
   * </pre>
   *
   * @param customizer A function that accepts a {@link TestCaseExecutor}.
   * @return The executor.
   */
  public TestCaseExecutor customize(Consumer<TestCaseExecutor> customizer) {
    if (customizer != null) {
      customizer.accept(this);
    }
    return this;
  }

  /**
   * Create a new process instance, executes the actual test case and verifies the state after.
   *
   * @return The event related to the newly created process instance.
   */
  public ProcessInstanceEvent execute() {
    try (ZeebeClient client = createClient()) {

      DeployResourceCommandStep1 deployResourceCommandStep1 = client.newDeployResourceCommand();
      DeployResourceCommandStep2 deployResourceCommandStep2;
      if (testCase.getBpmnResourceName() != null) {
        deployResourceCommandStep2 = deployResourceCommandStep1.addResourceFromClasspath(testCase.getBpmnResourceName());
      } else {
        String resourceName = String.format("%s.%s.bpmn", testCase.testClass.getSimpleName(), testCase.testMethodName);
        deployResourceCommandStep2 = deployResourceCommandStep1.addResourceStream(testCase.getBpmnResource(), resourceName);
      }

      if (tenantId != null) {
        deployResourceCommandStep2 = deployResourceCommandStep2.tenantId(tenantId);
      }

      DeploymentEvent deployEvent = deployResourceCommandStep2.send().join();

      BpmnAssert.assertThat(deployEvent).containsProcessesByBpmnProcessId(testCase.getBpmnProcessId());

      CreateProcessInstanceCommandStep3 createProcessInstanceCommandStep3 = client.newCreateInstanceCommand()
          .bpmnProcessId(testCase.getBpmnProcessId())
          .latestVersion();

      // TODO check start activity type otherwise:
      // Expected to create instance of process with start instructions but the element with id 'startEvent' targets unsupported element type 'START_EVENT'.
      // Supported element types are: [PROCESS, EVENT_SUB_PROCESS, SUB_PROCESS, END_EVENT, SEND_TASK, INTERMEDIATE_THROW_EVENT, INCLUSIVE_GATEWAY, CALL_ACTIVITY, MULTI_INSTANCE_BODY, INTERMEDIATE_CATCH_EVENT, USER_TASK, PARALLEL_GATEWAY, SCRIPT_TASK, MANUAL_TASK, SERVICE_TASK, EXCLUSIVE_GATEWAY, RECEIVE_TASK, BUSINESS_RULE_TASK, TASK, EVENT_BASED_GATEWAY]

      if (!testCase.isProcessStart()) {
        createProcessInstanceCommandStep3 = createProcessInstanceCommandStep3.startBeforeElement(testCase.getStart());
      }

      if (variables != null && !variableMap.isEmpty()) {
        throw new IllegalStateException("either use an object (POJO) as variables or a variable map");
      }

      if (variables != null) {
        createProcessInstanceCommandStep3 = createProcessInstanceCommandStep3.variables(variables);
      } else {
        createProcessInstanceCommandStep3 = createProcessInstanceCommandStep3.variables(variableMap);
      }

      if (tenantId != null) {
        createProcessInstanceCommandStep3 = createProcessInstanceCommandStep3.tenantId(tenantId);
      }

      ProcessInstanceEvent processInstanceEvent = createProcessInstanceCommandStep3.send().join();

      executeTestCase(processInstanceEvent, client);

      return processInstanceEvent;
    }
  }

  /**
   * Executes the actual test case and verifies the state after, using the given event.
   *
   * @param processInstanceEvent The event related to an existing process instance, used to execute the test case.
   */
  public void execute(ProcessInstanceEvent processInstanceEvent) {
    if (processInstanceEvent == null) {
      throw new IllegalArgumentException("process instance event is null");
    }

    try (ZeebeClient client = createClient()) {
      executeTestCase(processInstanceEvent, client);
    }
  }

  /**
   * Executes the actual test case and verifies the state after, using the process instance, identified by the given key.
   *
   * @param processInstanceKey The key of an existing process instance.
   * @return The identified process instance.
   */
  public ProcessInstanceEvent execute(long processInstanceKey) {
    ProcessInstanceEvent processInstanceEvent = null;
    for (Record<ProcessInstanceRecordValue> record : BpmnAssert.getRecordStream().processInstanceRecords()) {
      boolean condition = record.getRecordType() == RecordType.EVENT
          && record.getIntent() == ProcessInstanceIntent.ELEMENT_ACTIVATED
          && record.getValue().getBpmnElementType() == BpmnElementType.PROCESS;

      if (condition) {
        processInstanceEvent = new ProcessInstanceEventImpl(record);
        break;
      }
    }

    if (processInstanceEvent == null) {
      throw new IllegalArgumentException(String.format("no process instance with key %d found", processInstanceKey));
    }

    try (ZeebeClient client = createClient()) {
      executeTestCase(processInstanceEvent, client);
    }

    return processInstanceEvent;
  }

  /**
   * Verifies the state after the test case execution has finished.
   *
   * @param verifier Verifier that accepts an {@link ProcessInstanceAssert} instance.
   * @return The executor.
   */
  public TestCaseExecutor verify(Consumer<ProcessInstanceAssert> verifier) {
    this.verifier = verifier;
    return this;
  }

  /**
   * Sets the object mapper that is used by the {@link ZeebeClient}.
   *
   * @param objectMapper A specific object mapper.
   * @return The executor.
   */
  public TestCaseExecutor withObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    return this;
  }

  /**
   * Sets the tenant ID to be used for the automatic process deployment.
   *
   * @param tenantId A specific tenant ID.
   * @return The executor.
   */
  public TestCaseExecutor withTenantId(String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  /**
   * Sets a variable that is used for the process instance creation.
   *
   * @param name  The name of the variable.
   * @param value The variable's value.
   * @return The executor.
   */
  public TestCaseExecutor withVariable(String name, Object value) {
    variableMap.put(name, value);
    return this;
  }

  /**
   * Sets an object as variables that is used for the process instance creation.
   *
   * @param variables The variables as POJO.
   * @return The executor.
   */
  public TestCaseExecutor withVariables(Object variables) {
    this.variables = variables;
    return this;
  }

  /**
   * Sets variables that are used for the process instance creation.
   *
   * @param variableMap A map of variables.
   * @return The executor.
   */
  public TestCaseExecutor withVariableMap(Map<String, Object> variableMap) {
    this.variableMap.putAll(variableMap);
    return this;
  }

  ZeebeClient createClient() {
    JsonMapper jsonMapper;
    if (objectMapper != null) {
      jsonMapper = new ZeebeObjectMapper(objectMapper);
    } else {
      jsonMapper = new ZeebeObjectMapper();
    }

    return ZeebeClient.newClientBuilder()
        .gatewayAddress(engine.getGatewayAddress())
        .usePlaintext()
        .withJsonMapper(jsonMapper)
        .build();
  }

  void executeTestCase(ProcessInstanceEvent processInstanceEvent, ZeebeClient client) {
    try (TestCaseInstance testCaseInstance = new TestCaseInstance(engine, client)) {
      testCase.execute(testCaseInstance, processInstanceEvent);
    }

    if (verifier != null) {
      verifier.accept(BpmnAssert.assertThat(processInstanceEvent));
    }
  }

  /**
   * Internal class that fulfills the {@link ProcessInstanceEvent} contract by using a process instance record of the engine's record stream.
   */
  private static class ProcessInstanceEventImpl implements ProcessInstanceEvent {

    private final ProcessInstanceRecordValue value;

    private ProcessInstanceEventImpl(Record<ProcessInstanceRecordValue> record) {
      this.value = record.getValue();
    }

    @Override
    public long getProcessDefinitionKey() {
      return value.getProcessDefinitionKey();
    }

    @Override
    public String getBpmnProcessId() {
      return value.getBpmnProcessId();
    }

    @Override
    public int getVersion() {
      return value.getVersion();
    }

    @Override
    public long getProcessInstanceKey() {
      return value.getProcessInstanceKey();
    }

    @Override
    public String getTenantId() {
      return value.getTenantId();
    }
  }
}
