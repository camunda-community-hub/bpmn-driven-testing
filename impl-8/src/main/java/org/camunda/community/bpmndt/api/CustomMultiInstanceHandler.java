package org.camunda.community.bpmndt.api;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.camunda.community.bpmndt.api.TestCaseInstanceElement.MultiInstanceElement;

import io.camunda.client.api.search.enums.ElementInstanceState;
import io.camunda.client.api.search.response.ElementInstance;
import io.camunda.process.test.api.CamundaAssert;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;
import io.camunda.process.test.api.assertions.ProcessInstanceSelectors;

/**
 * Fluent API to handle multi instances and multi instance scopes, using custom code - see {@link #execute(BiConsumer)}.
 */
public class CustomMultiInstanceHandler {

  private final MultiInstanceElement element;

  private Consumer<ProcessInstanceAssert> verifier;
  private BiConsumer<TestCaseInstance, Long> action;
  private BiConsumer<TestCaseInstance, Long> loopAction;

  private Integer expectedLoopCount;
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

  void apply(TestCaseInstance instance, long flowScopeKey) {
    if (verifier != null) {
      var processInstanceSelector = ProcessInstanceSelectors.byKey(instance.getProcessInstanceKey(flowScopeKey));
      verifier.accept(CamundaAssert.assertThat(processInstanceSelector));
    }

    if (expectedSequential != null && expectedSequential != element.sequential) {
      var message = "expected multi instance %s to be %s, but was %s";
      throw new AssertionError(String.format(message, element.id, getText(expectedSequential), getText(element.sequential)));
    }

    if (action != null) {
      var processInstanceKey = instance.getProcessInstanceKey(flowScopeKey);
      action.accept(instance, processInstanceKey);
    }

    int loopIndex = 0;

    Optional<ElementInstance> next;
    while ((next = next(instance, flowScopeKey, loopIndex)).isPresent()) {
      loopIndex++;

      if (loopAction != null) {
        loopAction.accept(instance, next.get().getElementInstanceKey());
      }
    }

    if (expectedLoopCount != null && expectedLoopCount != loopIndex) {
      throw new AssertionError(String.format("expected multi instance %s to loop %dx, but was %dx", element.id, expectedLoopCount, loopIndex));
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
    if (loopAction != null) {
      throw new IllegalStateException("either use action or loop action");
    }
    this.action = action;
  }

  /**
   * Executes a custom action that handles the multi instance loop. The given consumer is called once per loop.
   * <p>
   * Please note: If the multi instance is <b>NOT</b> a scope (e.g. embedded sub process), the flow scope key, used to apply handler or to check if an element
   * has been passed, must be obtained via {@link TestCaseInstance#getFlowScopeKey(long)}.
   * <pre>
   *   tc.handleUserTask().executeLoop((instance, elementInstanceKey) -> {
   *      var flowScopeKey = instance.getFlowScopeKey(elementInstanceKey);
   *
   *      instance.apply(flowScopeKey, userTaskHandler);
   *      instance.hasPassed(flowScopeKey, "userTask");
   *   }
   * </pre>
   *
   * @param loopAction A specific action that accepts a {@link TestCaseInstance} and the current element instance key.
   */
  public void executeLoop(BiConsumer<TestCaseInstance, Long> loopAction) {
    if (loopAction == null) {
      throw new IllegalArgumentException("loop action is null");
    }
    if (action != null) {
      throw new IllegalStateException("either use action or loop action");
    }
    this.loopAction = loopAction;
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
   * Verifies that the multi instance loop is executed n-times.
   *
   * @param expectedLoopCount The expected loop count at the point of time when the multi instance is left (completed or terminated by a boundary event).
   * @return The handler.
   */
  public CustomMultiInstanceHandler verifyLoopCount(int expectedLoopCount) {
    this.expectedLoopCount = expectedLoopCount;
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

  private Optional<ElementInstance> next(TestCaseInstance instance, long flowScopeKey, int loopIndex) {
    var multiInstance = instance.getElementInstance(flowScopeKey, element.id);

    return instance.await(() -> {
      var elementInstances = instance.getClient().newElementInstanceSearchRequest()
          .filter(filter -> filter.elementInstanceScopeKey(multiInstance.getElementInstanceKey()))
          .sort(sort -> sort.startDate().asc())
          .execute()
          .items();

      if (elementInstances.size() > loopIndex) {
        // multi instance has next element
        return Optional.of(elementInstances.get(loopIndex));
      }

      // fetch multi instance again to get latest state
      var latestMultiInstance = instance.getClient().newElementInstanceGetRequest(multiInstance.getElementInstanceKey()).execute();

      if (latestMultiInstance.getState() == ElementInstanceState.COMPLETED || latestMultiInstance.getState() == ElementInstanceState.TERMINATED) {
        // multi instance is completed or terminated
        return Optional.empty();
      }

      var message = String.format("expected multi instance %s of flow scope %d to be completed or terminated, but was not", element.id, flowScopeKey);
      throw new AssertionError(message);
    });
  }
}
