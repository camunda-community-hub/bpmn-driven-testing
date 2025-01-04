package org.camunda.community.bpmndt.api;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.camunda.community.bpmndt.api.TestCaseInstanceElement.OutboundConnectorElement;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;

/**
 * Fluent API to handle outbound connectors. Please note: an outbound connector is completed by default.
 */
public class OutboundConnectorHandler {

  private final OutboundConnectorElement element;

  private final Map<String, Object> variableMap = new HashMap<>();

  private Consumer<ProcessInstanceAssert> verifier;
  private BiConsumer<ZeebeClient, Long> action;
  private String errorCode;
  private String errorMessage;
  private Object variables;

  private Consumer<Map<String, String>> inputMappingConsumer;
  private Consumer<Map<String, String>> outputMappingConsumer;
  private Consumer<String> taskDefinitionTypeConsumer;
  private Consumer<Map<String, String>> taskHeadersConsumer;

  private Integer expectedRetries;
  private String expectedTaskDefinitionType;

  private Consumer<Integer> retriesConsumer;

  public OutboundConnectorHandler(String elementId) {
    if (elementId == null) {
      throw new IllegalArgumentException("element ID is null");
    }

    element = new OutboundConnectorElement();
    element.id = elementId;

    complete();
  }

  public OutboundConnectorHandler(OutboundConnectorElement element) {
    if (element == null) {
      throw new IllegalArgumentException("element is null");
    }
    if (element.id == null) {
      throw new IllegalArgumentException("element ID is null");
    }

    this.element = element;

    complete();
  }

  void apply(TestCaseInstance instance, long flowScopeKey) {
    if (verifier != null) {
      var processInstanceKey = instance.getProcessInstanceKey(flowScopeKey);
      verifier.accept(new ProcessInstanceAssert(processInstanceKey, BpmnAssert.getRecordStream()));
    }

    if (expectedTaskDefinitionType != null && !expectedTaskDefinitionType.equals(element.taskDefinitionType)) {
      var message = "expected outbound connector %s to have task definition type '%s', but was '%s'";
      throw new AssertionError(String.format(message, element.id, expectedTaskDefinitionType, element.taskDefinitionType));
    }
    if (taskDefinitionTypeConsumer != null) {
      taskDefinitionTypeConsumer.accept(element.taskDefinitionType);
    }

    if (taskHeadersConsumer != null) {
      taskHeadersConsumer.accept(element.taskHeaders);
    }

    if (inputMappingConsumer != null) {
      inputMappingConsumer.accept(element.inputs);
    }
    if (outputMappingConsumer != null) {
      outputMappingConsumer.accept(element.outputs);
    }

    var job = instance.getJob(flowScopeKey, element.id);

    if (expectedRetries != null && !expectedRetries.equals(job.retries)) {
      var message = "expected job %s to have a retry count of %d, but was %d";
      throw new AssertionError(String.format(message, element.id, expectedRetries, job.retries));
    }
    if (retriesConsumer != null) {
      retriesConsumer.accept(job.retries);
    }

    if (action != null) {
      action.accept(instance.getClient(), job.key);
    }
  }

  /**
   * Executes an action that completes the underlying job, when the process instance is waiting at the corresponding element, using specified variables.
   *
   * @see #withVariable(String, Object)
   * @see #withVariables(Object)
   * @see #withVariableMap(Map)
   */
  public void complete() {
    action = this::complete;
  }

  /**
   * Customizes the handler, using the given {@link Consumer} function. This method can be used to apply a common customization needed for different test
   * cases.
   *
   * <pre>
   * tc.handleOutboundConnector().customize(this::prepare);
   * </pre>
   *
   * @param customizer A function that accepts a {@link OutboundConnectorHandler}.
   * @return The handler.
   */
  public OutboundConnectorHandler customize(Consumer<OutboundConnectorHandler> customizer) {
    if (customizer != null) {
      customizer.accept(this);
    }
    return this;
  }

  /**
   * Executes a custom action that handles the underlying job, when the process instance is waiting at the corresponding element.
   *
   * @param action A specific action that accepts a {@link ZeebeClient} and the related job key.
   * @see ZeebeClient#newCompleteCommand(long)
   */
  public void execute(BiConsumer<ZeebeClient, Long> action) {
    if (action == null) {
      throw new IllegalArgumentException("action is null");
    }
    this.action = action;
  }

  /**
   * Throws an BPMN error using the given error code and message as well as the specified variables.
   *
   * @see #withVariable(String, Object)
   * @see #withVariables(Object)
   * @see #withVariableMap(Map)
   */
  public void throwBpmnError(String errorCode, String errorMessage) {
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
    this.action = this::throwBpmnError;
  }

  /**
   * Verifies the outbound connector's waiting state. This method can be used to assert the variables, created by the connector's input mapping.
   *
   * @param verifier Verifier that accepts an {@link ProcessInstanceAssert} instance.
   * @return The handler.
   */
  public OutboundConnectorHandler verify(Consumer<ProcessInstanceAssert> verifier) {
    this.verifier = verifier;
    return this;
  }

  /**
   * Verifies the modeled inputs of the connector's IO mapping, using a consumer function.
   *
   * @param inputMappingConsumer A consumer asserting the input mapping.
   * @return The handler.
   */
  public OutboundConnectorHandler verifyInputMapping(Consumer<Map<String, String>> inputMappingConsumer) {
    this.inputMappingConsumer = inputMappingConsumer;
    return this;
  }

  /**
   * Verifies the modeled outputs of the connector's IO mapping, using a consumer function.
   *
   * @param outputMappingConsumer A consumer asserting the output mapping.
   * @return The handler.
   */
  public OutboundConnectorHandler verifyOutputMapping(Consumer<Map<String, String>> outputMappingConsumer) {
    this.outputMappingConsumer = outputMappingConsumer;
    return this;
  }

  /**
   * Verifies that the underlying job has a specific number of retries.
   *
   * @param expectedRetries The expected retry count.
   * @return The handler.
   */
  public OutboundConnectorHandler verifyRetries(Integer expectedRetries) {
    this.expectedRetries = expectedRetries;
    return this;
  }

  /**
   * Verifies that the underlying job has a specific number of retries, using a consumer.
   *
   * @param retriesConsumer A consumer asserting the retry count.
   * @return The handler.
   */
  public OutboundConnectorHandler verifyRetries(Consumer<Integer> retriesConsumer) {
    this.retriesConsumer = retriesConsumer;
    return this;
  }

  /**
   * Verifies that the outbound connector has the given task definition type.
   *
   * @param expectedTaskDefinitionType The expected task definition type.
   * @return The handler.
   */
  public OutboundConnectorHandler verifyTaskDefinitionType(String expectedTaskDefinitionType) {
    this.expectedTaskDefinitionType = expectedTaskDefinitionType;
    return this;
  }

  /**
   * Verifies that the outbound connector has a specific task definition type, using a consumer.
   *
   * @param taskDefinitionTypeConsumer A consumer asserting the task definition type.
   * @return The handler.
   */
  public OutboundConnectorHandler verifyTaskDefinitionType(Consumer<String> taskDefinitionTypeConsumer) {
    this.taskDefinitionTypeConsumer = taskDefinitionTypeConsumer;
    return this;
  }

  /**
   * Verifies the modeled task headers of the connector, using a consumer function.
   *
   * @param taskHeadersConsumer A consumer asserting the task headers.
   * @return The handler.
   */
  public OutboundConnectorHandler verifyTaskHeaders(Consumer<Map<String, String>> taskHeadersConsumer) {
    this.taskHeadersConsumer = taskHeadersConsumer;
    return this;
  }

  /**
   * Sets a variable that is used to complete the underlying job.
   *
   * @param name  The name of the variable.
   * @param value The variable's value.
   * @return The handler.
   * @see #complete()
   */
  public OutboundConnectorHandler withVariable(String name, Object value) {
    if (variables != null) {
      throw new IllegalStateException("either use an object (POJO) as variables or a variable map");
    }
    variableMap.put(name, value);
    return this;
  }

  /**
   * Sets an object as variables that is used to complete the underlying job.
   *
   * @param variables The variables as POJO.
   * @return The handler.
   * @see #complete()
   */
  public OutboundConnectorHandler withVariables(Object variables) {
    if (!variableMap.isEmpty()) {
      throw new IllegalStateException("either use an object (POJO) as variables or a variable map");
    }
    this.variables = variables;
    return this;
  }

  /**
   * Sets variables that are used to complete the underlying job.
   *
   * @param variableMap A map of variables.
   * @return The handler.
   * @see #complete()
   */
  public OutboundConnectorHandler withVariableMap(Map<String, Object> variableMap) {
    if (variables != null) {
      throw new IllegalStateException("either use an object (POJO) as variables or a variable map");
    }
    this.variableMap.putAll(variableMap);
    return this;
  }

  void complete(ZeebeClient client, long jobKey) {
    if (variables != null) {
      client.newCompleteCommand(jobKey).variables(variables).send().join();
    } else {
      client.newCompleteCommand(jobKey).variables(variableMap).send().join();
    }
  }

  void throwBpmnError(ZeebeClient client, long jobKey) {
    var throwErrorCommandStep2 = client.newThrowErrorCommand(jobKey).errorCode(errorCode).errorMessage(errorMessage);

    if (variables != null) {
      throwErrorCommandStep2.variables(variables).send().join();
    } else {
      throwErrorCommandStep2.variables(variableMap).send().join();
    }
  }
}
