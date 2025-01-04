package org.camunda.community.bpmndt.api;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.camunda.community.bpmndt.api.MessageEventHandler.Correlation;
import org.camunda.community.bpmndt.api.TestCaseInstanceElement.MessageEventElement;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;

/**
 * Fluent API to handle receive tasks. Please note: a message is correlated by default.
 */
public class ReceiveTaskHandler {

  private final MessageEventElement element;

  private final Map<String, Object> variableMap = new HashMap<>();

  private Consumer<ProcessInstanceAssert> verifier;
  private Correlation action;
  private Object variables;

  private Consumer<String> correlationKeyExpressionConsumer;
  private Consumer<String> messageNameExpressionConsumer;

  private String expectedCorrelationKey;
  private String expectedMessageName;

  private Consumer<String> correlationKeyConsumer;
  private Consumer<String> messageNameConsumer;

  public ReceiveTaskHandler(String elementId) {
    if (elementId == null) {
      throw new IllegalArgumentException("element ID is null");
    }

    element = new MessageEventElement();
    element.id = elementId;

    correlate();
  }

  public ReceiveTaskHandler(MessageEventElement element) {
    if (element == null) {
      throw new IllegalArgumentException("element is null");
    }
    if (element.id == null) {
      throw new IllegalArgumentException("element ID is null");
    }

    this.element = element;

    correlate();
  }

  void apply(TestCaseInstance instance, long flowScopeKey) {
    if (verifier != null) {
      var processInstanceKey = instance.getProcessInstanceKey(flowScopeKey);
      verifier.accept(new ProcessInstanceAssert(processInstanceKey, BpmnAssert.getRecordStream()));
    }

    if (correlationKeyExpressionConsumer != null) {
      correlationKeyExpressionConsumer.accept(element.correlationKey);
    }
    if (messageNameExpressionConsumer != null) {
      messageNameExpressionConsumer.accept(element.messageName);
    }

    var messageSubscription = instance.getMessageSubscription(flowScopeKey, element.id);

    if (expectedCorrelationKey != null && !expectedCorrelationKey.equals(messageSubscription.correlationKey)) {
      var message = "expected message event %s to have correlation key '%s', but was '%s'";
      throw new AssertionError(String.format(message, element.id, expectedCorrelationKey, messageSubscription.correlationKey));
    }
    if (correlationKeyConsumer != null) {
      correlationKeyConsumer.accept(messageSubscription.correlationKey);
    }

    if (expectedMessageName != null && !expectedMessageName.equals(messageSubscription.messageName)) {
      var message = "expected message event %s to have message name '%s', but was '%s'";
      throw new AssertionError(String.format(message, element.id, expectedMessageName, messageSubscription.messageName));
    }
    if (messageNameConsumer != null) {
      messageNameConsumer.accept(messageSubscription.messageName);
    }

    if (action != null) {
      action.correlate(instance.getClient(), messageSubscription.messageName, messageSubscription.correlationKey);
    }
  }

  /**
   * Correlates the message, when the process instance is waiting at the corresponding element, using specified variables.
   *
   * @see #withVariable(String, Object)
   * @see #withVariables(Object)
   * @see #withVariableMap(Map)
   */
  public void correlate() {
    action = this::correlate;
  }

  /**
   * Customizes the handler, using the given {@link Consumer} function. This method can be used to apply a common customization needed for different test
   * cases.
   *
   * <pre>
   * tc.handleReceiveTask().customize(this::prepare);
   * </pre>
   *
   * @param customizer A function that accepts a {@link ReceiveTaskHandler}.
   * @return The handler.
   */
  public ReceiveTaskHandler customize(Consumer<ReceiveTaskHandler> customizer) {
    if (customizer != null) {
      customizer.accept(this);
    }
    return this;
  }

  /**
   * Correlates the message using a custom action, when the process instance is waiting at the corresponding element.
   *
   * @param action A specific action that implements the {@link Correlation} or accepts a {@link ZeebeClient}, the message name and the correlation key.
   * @see ZeebeClient#newPublishMessageCommand()
   */
  public void execute(Correlation action) {
    if (action == null) {
      throw new IllegalArgumentException("action is null");
    }
    this.action = action;
  }

  /**
   * Verifies the receive task's waiting state.
   *
   * @param verifier Verifier that accepts an {@link ProcessInstanceAssert} instance.
   * @return The handler.
   */
  public ReceiveTaskHandler verify(Consumer<ProcessInstanceAssert> verifier) {
    this.verifier = verifier;
    return this;
  }

  /**
   * Verifies that the receive task has a specific correlation key.
   *
   * @param expectedCorrelationKey The expected correlation key.
   * @return The handler.
   */
  public ReceiveTaskHandler verifyCorrelationKey(String expectedCorrelationKey) {
    this.expectedCorrelationKey = expectedCorrelationKey;
    return this;
  }

  /**
   * Verifies that the receive task has a specific correlation key, using a consumer.
   *
   * @param correlationKeyConsumer A consumer asserting the correlation key.
   * @return The handler.
   */
  public ReceiveTaskHandler verifyCorrelationKey(Consumer<String> correlationKeyConsumer) {
    this.correlationKeyConsumer = correlationKeyConsumer;
    return this;
  }

  /**
   * Verifies that the receive task has a specific correlation key FEEL expression (see "Message" section), using a consumer function.
   *
   * @param correlationKeyExpressionConsumer A consumer asserting the correlation key expression.
   * @return The handler.
   */
  public ReceiveTaskHandler verifyCorrelationKeyExpression(Consumer<String> correlationKeyExpressionConsumer) {
    this.correlationKeyExpressionConsumer = correlationKeyExpressionConsumer;
    return this;
  }

  /**
   * Verifies that the receive task has a specific message name.
   *
   * @param expectedMessageName The expected message name.
   * @return The handler.
   */
  public ReceiveTaskHandler verifyMessageName(String expectedMessageName) {
    this.expectedMessageName = expectedMessageName;
    return this;
  }

  /**
   * Verifies that the receive task has a specific message name, using a consumer.
   *
   * @param messageNameConsumer A consumer asserting the message name.
   * @return The handler.
   */
  public ReceiveTaskHandler verifyMessageName(Consumer<String> messageNameConsumer) {
    this.messageNameConsumer = messageNameConsumer;
    return this;
  }

  /**
   * Verifies that the receive task has a specific message name FEEL expression (see "Message" section), using a consumer function.
   *
   * @param messageNameExpressionConsumer A consumer asserting the message name expression.
   * @return The handler.
   */
  public ReceiveTaskHandler verifyMessageNameExpression(Consumer<String> messageNameExpressionConsumer) {
    this.messageNameExpressionConsumer = messageNameExpressionConsumer;
    return this;
  }

  /**
   * Applies no action at the wait state. This is required when waiting for events (e.g. message, signal or timer events) that are attached as boundary events
   * on the element itself or on the surrounding scope (e.g. embedded subprocess).
   */
  public void waitForBoundaryEvent() {
    action = null;
  }

  /**
   * Sets a variable that is used to correlate the message.
   *
   * @param name  The name of the variable.
   * @param value The variable's value.
   * @return The handler.
   * @see #correlate()
   */
  public ReceiveTaskHandler withVariable(String name, Object value) {
    if (variables != null) {
      throw new IllegalStateException("either use an object (POJO) as variables or a variable map");
    }
    variableMap.put(name, value);
    return this;
  }

  /**
   * Sets an object as variables that is used to correlate the message.
   *
   * @param variables The variables as POJO.
   * @return The handler.
   * @see #correlate()
   */
  public ReceiveTaskHandler withVariables(Object variables) {
    if (!variableMap.isEmpty()) {
      throw new IllegalStateException("either use an object (POJO) as variables or a variable map");
    }
    this.variables = variables;
    return this;
  }

  /**
   * Sets variables that are used to correlate the message.
   *
   * @param variableMap A map of variables.
   * @return The handler.
   * @see #correlate()
   */
  public ReceiveTaskHandler withVariableMap(Map<String, Object> variableMap) {
    if (variables != null) {
      throw new IllegalStateException("either use an object (POJO) as variables or a variable map");
    }
    this.variableMap.putAll(variableMap);
    return this;
  }

  void correlate(ZeebeClient client, String messageName, String correlationKey) {
    var publishMessageCommandStep3 = client.newPublishMessageCommand()
        .messageName(messageName)
        .correlationKey(correlationKey);

    if (variables != null) {
      publishMessageCommandStep3.variables(variables).send().join();
    } else {
      publishMessageCommandStep3.variables(variableMap).send().join();
    }
  }
}
