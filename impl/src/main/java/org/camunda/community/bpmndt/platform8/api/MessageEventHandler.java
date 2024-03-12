package org.camunda.community.bpmndt.platform8.api;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceElement.MessageEventElement;
import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceMemo.MessageSubscriptionMemo;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.PublishMessageCommandStep1.PublishMessageCommandStep3;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;

public class MessageEventHandler {

  private final MessageEventElement element;

  private final Map<String, Object> variableMap = new HashMap<>();

  private Consumer<ProcessInstanceAssert> verifier;
  private BiConsumer<ZeebeClient, String> action;
  private Object variables;

  private Consumer<String> correlationKeyExpressionConsumer;

  private String expectedCorrelationKey;
  private String expectedMessageName;

  private Consumer<String> correlationKeyConsumer;
  private Consumer<String> messageNameConsumer;

  public MessageEventHandler(MessageEventElement element) {
    this.element = element;

    correlate();
  }

  void apply(TestCaseInstance instance, ProcessInstanceEvent processInstanceEvent) {
    if (verifier != null) {
      verifier.accept(BpmnAssert.assertThat(processInstanceEvent));
    }

    if (correlationKeyExpressionConsumer != null) {
      correlationKeyExpressionConsumer.accept(element.getCorrelationKey());
    }

    MessageSubscriptionMemo messageSubscription = instance.getMessageSubscription(processInstanceEvent, element.getId());

    if (expectedCorrelationKey != null && !expectedCorrelationKey.equals(messageSubscription.correlationKey)) {
      String message = "expected message event %s to have correlation key '%s', but was '%s'";
      throw new AssertionError(String.format(message, element.getId(), expectedCorrelationKey, messageSubscription.correlationKey));
    }
    if (correlationKeyConsumer != null) {
      correlationKeyConsumer.accept(messageSubscription.correlationKey);
    }

    if (expectedMessageName != null && !expectedMessageName.equals(messageSubscription.correlationKey)) {
      String message = "expected message event %s to have message name '%s', but was '%s'";
      throw new AssertionError(String.format(message, element.getId(), expectedMessageName, messageSubscription.messageName));
    }
    if (messageNameConsumer != null) {
      messageNameConsumer.accept(messageSubscription.messageName);
    }

    action.accept(instance.client, messageSubscription.correlationKey);

    instance.hasPassed(processInstanceEvent, element.getId());
  }

  public void correlate() {
    action = this::correlate;
  }

  public void correlate(BiConsumer<ZeebeClient, String> action) {
    if (action == null) {
      throw new IllegalArgumentException("action is null");
    }
    this.action = action;
  }

  void correlate(ZeebeClient client, String correlationKey) {
    PublishMessageCommandStep3 publishMessageCommandStep3 = client.newPublishMessageCommand()
        .messageName(element.getMessageName())
        .correlationKey(element.getCorrelationKey());

    if (variables != null) {
      publishMessageCommandStep3.variables(variables).send();
    } else {
      publishMessageCommandStep3.variables(variableMap).send();
    }
  }

  /**
   * Customizes the handler, using the given {@link Consumer} function. This method can be used to apply a common customization needed for different test
   * cases.
   *
   * <pre>
   * tc.handleMessageCatchEvent().customize(this::prepare);
   * </pre>
   *
   * @param customizer A function that accepts a {@link MessageEventHandler}.
   * @return The handler.
   */
  public MessageEventHandler customize(Consumer<MessageEventHandler> customizer) {
    if (customizer != null) {
      customizer.accept(this);
    }
    return this;
  }

  /**
   * Verifies the message event's waiting state.
   *
   * @param verifier Verifier that accepts an {@link ProcessInstanceAssert} instance.
   * @return The handler.
   */
  public MessageEventHandler verify(Consumer<ProcessInstanceAssert> verifier) {
    this.verifier = verifier;
    return this;
  }

  /**
   * Sets a variable that is used to correlate the message.
   *
   * @param name  The name of the variable.
   * @param value The variable's value.
   * @return The handler.
   * @see #correlate()
   */
  public MessageEventHandler withVariable(String name, Object value) {
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
  public MessageEventHandler withVariables(Object variables) {
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
  public MessageEventHandler withVariableMap(Map<String, Object> variableMap) {
    if (variables != null) {
      throw new IllegalStateException("either use an object (POJO) as variables or a variable map");
    }
    this.variableMap.putAll(variableMap);
    return this;
  }
}
