package org.camunda.community.bpmndt.api;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.camunda.community.bpmndt.api.TestCaseInstanceElement.SignalEventElement;

import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaAssert;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;
import io.camunda.process.test.api.assertions.ProcessInstanceSelectors;

/**
 * Fluent API to handle signal catch events. Please note: a signal is broadcasted by default.
 */
public class SignalEventHandler {

  private final SignalEventElement element;

  private Consumer<ProcessInstanceAssert> verifier;
  private BiConsumer<CamundaClient, String> action;

  private Consumer<String> signalNameExpressionConsumer;

  private String expectedSignalName;

  private Consumer<String> signalNameConsumer;

  /**
   * Creates a new handler that by default broadcasts a signal.
   *
   * @param elementId ID of the BPMN signal event.
   */
  public SignalEventHandler(String elementId) {
    this(elementId, null);
  }

  /**
   * Creates a new handler that by default broadcasts a signal.
   *
   * @param elementId  ID of the BPMN signal event.
   * @param attachedTo ID of the BPMN element, the signal event is attached to.
   */
  public SignalEventHandler(String elementId, String attachedTo) {
    if (elementId == null) {
      throw new IllegalArgumentException("element ID is null");
    }

    element = new SignalEventElement();
    element.id = elementId;
    element.attachedTo = attachedTo;

    broadcast();
  }

  public SignalEventHandler(SignalEventElement element) {
    if (element == null) {
      throw new IllegalArgumentException("element is null");
    }
    if (element.id == null) {
      throw new IllegalArgumentException("element ID is null");
    }

    this.element = element;

    broadcast();
  }

  void apply(TestCaseInstance instance, long flowScopeKey) {
    if (verifier != null) {
      var processInstanceSelector = ProcessInstanceSelectors.byKey(instance.getProcessInstanceKey(flowScopeKey));
      verifier.accept(CamundaAssert.assertThat(processInstanceSelector));
    }

    if (signalNameExpressionConsumer != null) {
      signalNameExpressionConsumer.accept(element.signalName);
    }

    var signalName = getSignalName(instance, flowScopeKey);

    if (expectedSignalName != null && !expectedSignalName.equals(signalName)) {
      String message = "expected signal event %s to have signal name '%s', but was '%s'";
      throw new AssertionError(String.format(message, element.id, expectedSignalName, signalName));
    }
    if (signalNameConsumer != null) {
      signalNameConsumer.accept(signalName);
    }

    action.accept(instance.getClient(), signalName);
  }

  /**
   * Broadcasts a signal, when the process instance is waiting at the corresponding element.
   */
  public void broadcast() {
    action = this::broadcast;
  }

  /**
   * Customizes the handler, using the given {@link Consumer} function. This method can be used to apply a common customization needed for different test
   * cases.
   *
   * <pre>
   * tc.handleSignalCatchEvent().customize(this::prepare);
   * </pre>
   *
   * @param customizer A function that accepts a {@link SignalEventHandler}.
   * @return The handler.
   */
  public SignalEventHandler customize(Consumer<SignalEventHandler> customizer) {
    if (customizer != null) {
      customizer.accept(this);
    }
    return this;
  }

  /**
   * Broadcasts a signal using a custom action, when the process instance is waiting at the corresponding element.
   *
   * @param action A specific action that accepts a {@link CamundaClient} and the signal name.
   * @see CamundaClient#newBroadcastSignalCommand()
   */
  public void execute(BiConsumer<CamundaClient, String> action) {
    if (action == null) {
      throw new IllegalArgumentException("action is null");
    }
    this.action = action;
  }

  /**
   * Verifies the signal event's waiting state.
   *
   * @param verifier Verifier that accepts an {@link ProcessInstanceAssert} instance.
   * @return The handler.
   */
  public SignalEventHandler verify(Consumer<ProcessInstanceAssert> verifier) {
    this.verifier = verifier;
    return this;
  }

  /**
   * Verifies that the signal event has a specific signal name.
   *
   * @param expectedSignalName The expected signal name.
   * @return The handler.
   */
  public SignalEventHandler verifySignalName(String expectedSignalName) {
    this.expectedSignalName = expectedSignalName;
    return this;
  }

  /**
   * Verifies that the signal event has a specific signal name, using a consumer.
   *
   * @param signalNameConsumer A consumer asserting the signal name.
   * @return The handler.
   */
  public SignalEventHandler verifySignalName(Consumer<String> signalNameConsumer) {
    this.signalNameConsumer = signalNameConsumer;
    return this;
  }

  /**
   * Verifies that the signal event has a specific signal name FEEL expression (see "Signal" section), using a consumer function.
   *
   * @param signalNameExpressionConsumer A consumer asserting the signal name expression.
   * @return The handler.
   */
  public SignalEventHandler verifySignalNameExpression(Consumer<String> signalNameExpressionConsumer) {
    this.signalNameExpressionConsumer = signalNameExpressionConsumer;
    return this;
  }

  void broadcast(CamundaClient client, String signalName) {
    client.newBroadcastSignalCommand().signalName(signalName).send().join();
  }

  private String getSignalName(TestCaseInstance instance, long flowScopeKey) {
    throw new UnsupportedOperationException();
  }
}
