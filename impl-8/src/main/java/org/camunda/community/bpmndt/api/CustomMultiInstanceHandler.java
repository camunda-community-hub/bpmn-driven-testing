package org.camunda.community.bpmndt.api;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.camunda.community.bpmndt.api.TestCaseInstanceElement.MultiInstanceElement;

import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;

/**
 * Fluent API to handle multi instances and multi instance scopes, using custom code - see {@link #execute(BiConsumer)}.
 */
public class CustomMultiInstanceHandler {

  private final MultiInstanceElement element;

  private Consumer<ProcessInstanceAssert> verifier;
  private BiConsumer<TestCaseInstance, Long> action;

  private Boolean expectedSequential;

  public CustomMultiInstanceHandler(String elementId) {
    if (elementId == null) {
      throw new IllegalArgumentException("element ID is null");
    }

    element = new MultiInstanceElement();
    element.id = elementId;
  }

  public CustomMultiInstanceHandler(MultiInstanceElement element) {
    if (element == null) {
      throw new IllegalArgumentException("element is null");
    }
    if (element.id == null) {
      throw new IllegalArgumentException("element ID is null");
    }

    this.element = element;
  }

  void apply(TestCaseInstance instance, long processInstanceKey) {
    if (verifier != null) {
      verifier.accept(new ProcessInstanceAssert(processInstanceKey, BpmnAssert.getRecordStream()));
    }

    if (expectedSequential != null && expectedSequential != element.sequential) {
      var message = "expected multi instance %s to be %s, but was %s";
      throw new AssertionError(String.format(message, element.id, getText(expectedSequential), getText(element.sequential)));
    }

    if (action != null) {
      action.accept(instance, processInstanceKey);
    }
  }

  /**
   * Customizes the handler, using the given {@link Consumer} function. This method can be used to apply a common customization needed for different test
   * cases.
   *
   * <pre>
   * tc.handleMultiInstance().customize(this::prepare);
   * </pre>
   *
   * @param customizer A function that accepts a {@link CustomMultiInstanceHandler}.
   * @return The handler.
   */
  public CustomMultiInstanceHandler customize(Consumer<CustomMultiInstanceHandler> customizer) {
    if (customizer != null) {
      customizer.accept(this);
    }
    return this;
  }

  /**
   * Executes a custom action that handles the multi instance or the multi instance scope.
   *
   * @param action A specific action that accepts a {@link TestCaseInstance} and the related process instance key.
   */
  public void execute(BiConsumer<TestCaseInstance, Long> action) {
    if (action == null) {
      throw new IllegalArgumentException("action is null");
    }
    this.action = action;
  }

  /**
   * Verifies the multi instance state.
   * <p>
   * <b>Please note</b>: An application specific job worker may have already completed the related job and updated some variables.
   *
   * @param verifier Verifier that accepts an {@link ProcessInstanceAssert} instance.
   * @return The handler.
   */
  public CustomMultiInstanceHandler verify(Consumer<ProcessInstanceAssert> verifier) {
    this.verifier = verifier;
    return this;
  }

  /**
   * Verifies that the multi instance loop execution is done in parallel.
   *
   * @return The handler.
   */
  public CustomMultiInstanceHandler verifyParallel() {
    this.expectedSequential = Boolean.FALSE;
    return this;
  }

  /**
   * Verifies that the multi instance loop is sequentially executed.
   *
   * @return The handler.
   */
  public CustomMultiInstanceHandler verifySequential() {
    this.expectedSequential = Boolean.TRUE;
    return this;
  }

  private String getText(boolean sequential) {
    return sequential ? "sequential" : "parallel";
  }
}
