package org.camunda.community.bpmndt.api;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.JsonMapper;
import io.camunda.zeebe.client.api.command.ClientStatusException;
import io.camunda.zeebe.client.api.command.DeployResourceCommandStep1.DeployResourceCommandStep2;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.Process;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import io.camunda.zeebe.client.impl.ZeebeObjectMapper;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.filters.RecordStream;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordType;
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;
import io.camunda.zeebe.protocol.record.value.BpmnElementType;
import io.camunda.zeebe.protocol.record.value.MessageStartEventSubscriptionRecordValue;
import io.camunda.zeebe.protocol.record.value.ProcessEventRecordValue;
import io.camunda.zeebe.protocol.record.value.SignalSubscriptionRecordValue;
import io.camunda.zeebe.protocol.record.value.TimerRecordValue;

/**
 * Fluent API to prepare and start the actual test case execution.
 */
public class TestCaseExecutor {

  private final AbstractTestCase testCase;
  private final ZeebeTestEngine engine;
  private final String simulateSubProcessResource;

  private final Map<String, Object> variableMap = new HashMap<>();

  private final List<String> additionalResourceNames = new ArrayList<>(0);
  private final List<String> additionalResources = new ArrayList<>(0);

  private ObjectMapper objectMapper;
  private long taskTimeout = 5000;
  private boolean printRecordStreamEnabled;
  private String tenantId;
  private Object variables;
  private Consumer<ProcessInstanceAssert> verifier;

  public TestCaseExecutor(AbstractTestCase testCase, ZeebeTestEngine engine, String simulateSubProcessResource) {
    this.testCase = testCase;
    this.engine = engine;
    this.simulateSubProcessResource = simulateSubProcessResource;

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
   * @return The key of the newly created process instance.
   */
  public long execute() {
    try (ZeebeClient client = createClient()) {

      var deployResourceCommandStep1 = client.newDeployResourceCommand();

      DeployResourceCommandStep2 deployResourceCommandStep2;
      if (testCase.getBpmnResourceName() != null) {
        deployResourceCommandStep2 = deployResourceCommandStep1.addResourceFromClasspath(testCase.getBpmnResourceName());
      } else {
        var resourceName = String.format("%s.%s.bpmn", testCase.testClass.getSimpleName(), testCase.testMethodName);
        deployResourceCommandStep2 = deployResourceCommandStep1.addResourceStream(testCase.getBpmnResource(), resourceName);
      }

      for (int i = 0; i < additionalResources.size(); i++) {
        var resourceName = additionalResourceNames.get(i);
        var resource = additionalResources.get(i);

        deployResourceCommandStep2.addResourceStringUtf8(resource, resourceName);
      }

      if (tenantId != null) {
        deployResourceCommandStep2 = deployResourceCommandStep2.tenantId(tenantId);
      }

      var deploymentEvent = deployResourceCommandStep2.send().join();

      try {
        engine.waitForIdleState(Duration.ofSeconds(1L));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } catch (TimeoutException e) {
        throw new RuntimeException("failed to wait for engine idle state", e);
      }

      BpmnAssert.assertThat(deploymentEvent).containsProcessesByBpmnProcessId(testCase.getBpmnProcessId());

      var processDefinitionKey = findProcessDefinitionKey(deploymentEvent);

      if (variables != null && !variableMap.isEmpty()) {
        throw new IllegalStateException("either use an object (POJO) as variables or a variable map");
      }

      long processInstanceKey;
      if (testCase.isMessageStart()) {
        // handle message start event
        var publishMessageCommandStep3 = client.newPublishMessageCommand()
            .messageName(findStartMessageName(processDefinitionKey))
            .correlationKey(String.format("%s.%s", testCase.testClass.getSimpleName(), testCase.testMethodName));

        if (variables != null) {
          publishMessageCommandStep3 = publishMessageCommandStep3.variables(variables);
        } else {
          publishMessageCommandStep3 = publishMessageCommandStep3.variables(variableMap);
        }

        if (tenantId != null) {
          publishMessageCommandStep3 = publishMessageCommandStep3.tenantId(tenantId);
        }

        // publish message
        var publishMessageResponse = publishMessageCommandStep3.send().join();

        // find key of created process instance
        processInstanceKey = findProcessInstanceKey(publishMessageResponse);
      } else if (testCase.isSignalStart()) {
        // handle signal start event
        var broadcastSignalCommandStep2 = client.newBroadcastSignalCommand().signalName(findStartSignalName(processDefinitionKey));

        if (variables != null) {
          broadcastSignalCommandStep2 = broadcastSignalCommandStep2.variables(variables);
        } else {
          broadcastSignalCommandStep2 = broadcastSignalCommandStep2.variables(variableMap);
        }

        // broadcast signal
        broadcastSignalCommandStep2.send().join();

        // find key of created process instance
        processInstanceKey = findProcessInstanceKey(processDefinitionKey);
      } else if (testCase.isTimerStart()) {
        // handle timer start event
        if (variables != null || !variableMap.isEmpty()) {
          throw new IllegalStateException("not possible to create a process instance with variables, using a timer start event");
        }

        var startTimerDueDate = findStartTimerDueDate(processDefinitionKey);
        engine.increaseTime(Duration.ofMillis(startTimerDueDate - System.currentTimeMillis()));

        try {
          TimeUnit.SECONDS.sleep(1L);
          engine.waitForIdleState(Duration.ofSeconds(1L));
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } catch (TimeoutException e) {
          throw new RuntimeException("failed to wait for engine idle state", e);
        }

        // find key of created process instance
        processInstanceKey = findProcessInstanceKey(processDefinitionKey);
      } else {
        // handle none start event
        var createProcessInstanceCommandStep3 = client.newCreateInstanceCommand()
            .bpmnProcessId(testCase.getBpmnProcessId())
            .latestVersion();

        if (!testCase.isProcessStart()) {
          createProcessInstanceCommandStep3 = createProcessInstanceCommandStep3.startBeforeElement(testCase.getStart());
        }

        if (variables != null) {
          createProcessInstanceCommandStep3 = createProcessInstanceCommandStep3.variables(variables);
        } else {
          createProcessInstanceCommandStep3 = createProcessInstanceCommandStep3.variables(variableMap);
        }

        if (tenantId != null) {
          createProcessInstanceCommandStep3 = createProcessInstanceCommandStep3.tenantId(tenantId);
        }

        var processInstanceEvent = createProcessInstanceCommandStep3.send().join();

        processInstanceKey = processInstanceEvent.getProcessInstanceKey();
      }

      executeTestCase(client, processInstanceKey);

      return processInstanceKey;
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

    try {
      engine.waitForIdleState(Duration.ofSeconds(1L));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (TimeoutException e) {
      throw new RuntimeException("failed to wait for engine idle state", e);
    }

    try (ZeebeClient client = createClient()) {
      executeTestCase(client, processInstanceEvent.getProcessInstanceKey());
    }
  }

  /**
   * Executes the actual test case and verifies the state after, using the process instance, identified by the given key.
   *
   * @param processInstanceKey The key of an existing process instance.
   */
  public void execute(long processInstanceKey) {
    try {
      engine.waitForIdleState(Duration.ofSeconds(1L));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (TimeoutException e) {
      throw new RuntimeException("failed to wait for engine idle state", e);
    }

    boolean exists = StreamSupport.stream(BpmnAssert.getRecordStream().processInstanceRecords().spliterator(), false)
        .anyMatch(record ->
            record.getRecordType() == RecordType.EVENT
                && record.getIntent() == ProcessInstanceIntent.ELEMENT_ACTIVATED
                && record.getValue().getBpmnElementType() == BpmnElementType.PROCESS
                && record.getValue().getProcessInstanceKey() == processInstanceKey
        );

    if (!exists) {
      throw new IllegalArgumentException(String.format("failed to find process instance %d", processInstanceKey));
    }

    try (ZeebeClient client = createClient()) {
      executeTestCase(client, processInstanceKey);
    }
  }

  /**
   * Simulates the process with the given ID by adding a stub process to the resource deployment.
   *
   * @param processId The ID of the process to simulate.
   * @return The executor.
   */
  public TestCaseExecutor simulateProcess(String processId) {
    var resource = simulateSubProcessResource.replace("processId", processId);
    return withAdditionalResource(processId + ".bpmn", resource);
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
   * Adds a resource to the resource deployment ({@link ZeebeClient#newDeployResourceCommand()}).
   *
   * @param resourceName Name of the resource.
   * @param resource     The resource as UTF-8 string.
   * @return The executor.
   */
  public TestCaseExecutor withAdditionalResource(String resourceName, String resource) {
    additionalResourceNames.add(resourceName);
    additionalResources.add(resource);
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
   * Enables record streaming printing to stdout. This is useful for debugging or reporting bugs.
   *
   * @param printRecordStreamEnabled Enable or disable the printing of records.
   * @return The executor.
   */
  public TestCaseExecutor withPrintRecordStreamEnabled(boolean printRecordStreamEnabled) {
    this.printRecordStreamEnabled = printRecordStreamEnabled;
    return this;
  }

  /**
   * Specifies a timeout in milliseconds for tasks that audit the test engine's record stream - e.g. tasks that check process instance is waiting at or has
   * passed a certain BPMN element.
   *
   * @param taskTimeout The audit task timeout in milliseconds - the default value is {@code 5000}
   * @return The executor.
   */
  public TestCaseExecutor withTaskTimeout(long taskTimeout) {
    this.taskTimeout = taskTimeout;
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
        .grpcAddress(URI.create("https://" + engine.getGatewayAddress()))
        .usePlaintext()
        .withJsonMapper(jsonMapper)
        .build();
  }

  void executeTestCase(ZeebeClient client, long processInstanceKey) {
    try (TestCaseInstance testCaseInstance = new TestCaseInstance(engine, client, taskTimeout, printRecordStreamEnabled)) {
      testCase.execute(testCaseInstance, processInstanceKey);
    } catch (Throwable t) {
      // cancel not ended process instance of failed test
      // to ensure that message is not correlated with an old process instance
      // when the same correlation key is used
      try {
        client.newCancelInstanceCommand(processInstanceKey).send().join();
      } catch (ClientStatusException e) {
        // ignore exception
      }

      throw t;
    }

    if (verifier != null) {
      verifier.accept(new ProcessInstanceAssert(processInstanceKey, BpmnAssert.getRecordStream()));
    }
  }

  long findProcessDefinitionKey(DeploymentEvent deploymentEvent) {
    return deploymentEvent.getProcesses().stream()
        .filter(process -> process.getBpmnProcessId().equals(testCase.getBpmnProcessId()))
        .map(Process::getProcessDefinitionKey)
        .findFirst()
        .orElseThrow();
  }

  long findProcessInstanceKey(long processDefinitionKey) {
    for (Record<?> record : BpmnAssert.getRecordStream().records()) {
      if (record.getValueType() != ValueType.PROCESS_EVENT) {
        continue;
      }

      var recordValue = (ProcessEventRecordValue) record.getValue();
      if (recordValue.getProcessDefinitionKey() == processDefinitionKey && recordValue.getTargetElementId().equals(testCase.getStart())) {
        return recordValue.getProcessInstanceKey();
      }
    }
    throw new RuntimeException(String.format("failed to find process instance key for process definition key %d", processDefinitionKey));
  }

  long findProcessInstanceKey(PublishMessageResponse publishMessageResponse) {
    for (Record<MessageStartEventSubscriptionRecordValue> record : BpmnAssert.getRecordStream().messageStartEventSubscriptionRecords()) {
      var recordValue = record.getValue();

      if (recordValue.getMessageKey() == publishMessageResponse.getMessageKey()) {
        return record.getValue().getProcessInstanceKey();
      }
    }
    throw new RuntimeException("failed to find process instance key for message start");
  }

  String findStartMessageName(long processDefinitionKey) {
    for (Record<MessageStartEventSubscriptionRecordValue> record : BpmnAssert.getRecordStream().messageStartEventSubscriptionRecords()) {
      var recordValue = record.getValue();

      if (recordValue.getProcessDefinitionKey() == processDefinitionKey && recordValue.getStartEventId().equals(testCase.getStart())) {
        return recordValue.getMessageName();
      }
    }
    throw new RuntimeException(String.format("failed to find message name of message start event %s", testCase.getStart()));
  }

  String findStartSignalName(long processDefinitionKey) {
    for (Record<?> record : BpmnAssert.getRecordStream().records()) {
      if (record.getValueType() != ValueType.SIGNAL_SUBSCRIPTION) {
        continue;
      }

      var recordValue = (SignalSubscriptionRecordValue) record.getValue();
      if (recordValue.getProcessDefinitionKey() == processDefinitionKey && recordValue.getCatchEventId().equals(testCase.getStart())) {
        return recordValue.getSignalName();
      }
    }
    throw new RuntimeException(String.format("failed to find signal name of signal start event %s", testCase.getStart()));
  }

  long findStartTimerDueDate(long processDefinitionKey) {
    for (Record<TimerRecordValue> record : BpmnAssert.getRecordStream().timerRecords()) {
      var recordValue = record.getValue();

      if (recordValue.getProcessDefinitionKey() == processDefinitionKey && recordValue.getTargetElementId().equals(testCase.getStart())) {
        return recordValue.getDueDate();
      }
    }
    throw new RuntimeException(String.format("failed to find due date of timer start event %s", testCase.getStart()));
  }
}
