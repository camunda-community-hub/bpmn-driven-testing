package org.camunda.community.bpmndt.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.community.bpmndt.api.JobHandler.Cardinality;

/**
 * Fluent API for multi instance scopes (sub processes or transactions).
 *
 * @param <T> The generated multi instance scope handler type.
 */
public class MultiInstanceScopeHandler<T extends MultiInstanceScopeHandler<?>> {

  private static final String MSG_LOOP_COUNT = "Expected multi instance '%s' to loop %dx, but was %dx";
  private static final String MSG_SEQUENTIAL = "Expected multi instance '%s' to be %s, but was %s";

  protected final AbstractTestCase<?> testCase;
  protected final TestCaseInstance instance;

  private final String activityId;

  /**
   * ID of the multi instance scope.
   */
  private final String scopeId;

  private final Map<Integer, BiFunction<ProcessInstanceAssert, Integer, Boolean>> actions;
  private final Map<Integer, BiConsumer<ProcessInstanceAssert, Integer>> verifiers;

  private Integer loopCount;
  private Boolean sequential;

  public MultiInstanceScopeHandler(AbstractTestCase<?> testCase, String activityId) {
    this.testCase = testCase;
    this.instance = testCase.instance;
    this.activityId = activityId;

    scopeId = String.format("%s#%s", activityId, ActivityTypes.MULTI_INSTANCE_BODY);

    actions = new HashMap<>();
    verifiers = new HashMap<>();
  }

  protected void apply(ProcessInstance pi) {
    if (sequential != null && sequential != isSequential()) {
      throw new AssertionError(String.format(MSG_SEQUENTIAL, activityId, getText(sequential), getText(isSequential())));
    }

    if (loopCount != null && loopCount == 0) {
      // if multi instance loop count is 0,
      // the activity should not have been passed
      ProcessEngineTests.assertThat(pi).hasNotPassed(activityId);
      return;
    }

    int loopIndex = 0;
    do {
      handleAsyncBefore(pi, loopIndex);

      BiFunction<ProcessInstanceAssert, Integer, Boolean> action = actions.get(loopIndex);

      boolean shouldContinue;
      if (action == null) {
        shouldContinue = apply(pi, loopIndex);
      } else {
        shouldContinue = nullSafeBoolean(action.apply(ProcessEngineTests.assertThat(pi), loopIndex));
      }

      loopIndex++;

      if (!shouldContinue) {
        break;
      }
    } while (!isEnded(pi));

    if (loopCount != null && loopCount != loopIndex) {
      throw new AssertionError(String.format(MSG_LOOP_COUNT, activityId, loopCount, loopIndex));
    }
  }

  /**
   * Applies the multi instance loop for the given index. Please note: This method will be overridden by generated multi instance scope handler classes.
   *
   * @param pi        The process instance, used to execute the test case.
   * @param loopIndex The current loop index.
   * @return {@code true}, if the multi instance execution should be continued. Otherwise {@code false}.
   */
  protected boolean apply(ProcessInstance pi, int loopIndex) {
    return true;
  }

  /**
   * Customizes the handler, using the given {@link Consumer} function. This method can be used to apply a common customization needed for different test
   * cases.
   *
   * <pre>
   * tc.handleMultiInstanceScope().customize(this::prepareMultiInstanceScope);
   * </pre>
   *
   * @param customizer A function that accepts a suitable {@link MultiInstanceScopeHandler}.
   * @return The handler.
   */
  @SuppressWarnings("unchecked")
  public T customize(Consumer<MultiInstanceScopeHandler<T>> customizer) {
    if (customizer != null) {
      customizer.accept(this);
    }
    return (T) this;
  }

  /**
   * Executes a custom action, which handles the activities within the scope, for a given loop index.
   *
   * @param loopIndex A specific loop index (>= 0).
   * @param action    A specific action that accepts the related process instance as {@link ProcessInstanceAssert} and the loop index. It returns a boolean
   *                  value that indicates if the multi instance loop should be continued or not (e.g. in case of a boundary event that will be triggered
   *                  afterward).
   */
  @SuppressWarnings("unchecked")
  public T execute(int loopIndex, BiFunction<ProcessInstanceAssert, Integer, Boolean> action) {
    actions.put(loopIndex, action);
    return (T) this;
  }

  protected ProcessEngine getProcessEngine() {
    return instance.getProcessEngine();
  }

  private String getText(boolean sequential) {
    return sequential ? "sequential" : "parallel";
  }

  protected void handleAsyncBefore(ProcessInstance pi, int loopIndex) {
    JobHandler jobHandler = new JobHandler(getProcessEngine(), activityId, isSequential() ? Cardinality.ZERO_TO_ONE : Cardinality.ZERO_TO_N);

    ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

    piAssert.isWaitingAt(activityId);

    BiConsumer<ProcessInstanceAssert, Integer> verifier = verifiers.get(loopIndex);
    if (verifier != null) {
      verifier.accept(piAssert, loopIndex);
    }

    instance.apply(jobHandler);
  }

  /**
   * Checks if the multi instance scope is ended or not.
   *
   * @param pi The related process instance.
   * @return {@code true}, if the multi instance scope is ended. Otherwise {@code false}.
   */
  protected boolean isEnded(ProcessInstance pi) {
    HistoryService historyService = getProcessEngine().getHistoryService();

    List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
        .processInstanceId(pi.getId())
        .activityId(scopeId)
        .orderByHistoricActivityInstanceStartTime().desc() // last activity instance first
        .list();

    if (historicActivityInstances.isEmpty()) {
      throw new AssertionError(String.format("No historic activity instance found for multi instance scope '%s'", scopeId));
    }

    return historicActivityInstances.get(0).getEndTime() != null;
  }

  /**
   * Determines if the multi instance loop is sequentially executed or not. Please note: If the multi instance loop is defined as parallel, this method will be
   * overridden by generated multi instance handler classes.
   *
   * @return {@code true}, if execution is done sequentially. {@code false}, if execution is done in parallel.
   */
  protected boolean isSequential() {
    return true;
  }

  private boolean nullSafeBoolean(Boolean value) {
    return value != null && value;
  }

  /**
   * Registers the given call activity handler at the test case instance, so that it will be executed when the custom call activity behavior is applied.<br>
   * Since there can be multiple handlers (one for each loop index), it is necessary to register the correct one before the next multi instance loop is
   * executed.
   *
   * @param activityId The ID of the call activity.
   * @param handler    The call activity handler to be executed next.
   */
  protected void registerCallActivityHandler(String activityId, CallActivityHandler handler) {
    instance.registerCallActivityHandler(activityId, handler);
  }

  /**
   * Verifies the state before the multi instance loop with the given index is executed.
   *
   * @param loopIndex A specific loop index (>= 0).
   * @param verifier  Verifier that accepts an {@link ProcessInstanceAssert} and the loop index.
   * @return The handler.
   */
  @SuppressWarnings("unchecked")
  public T verify(int loopIndex, BiConsumer<ProcessInstanceAssert, Integer> verifier) {
    verifiers.put(loopIndex, verifier);
    return (T) this;
  }

  /**
   * Verifies that the multi instance loop is executed n-times.
   *
   * @param loopCount The expected loop count at the point of time when the multi instance scope is left (finished or terminated by a boundary event).
   * @return The handler.
   */
  @SuppressWarnings("unchecked")
  public T verifyLoopCount(int loopCount) {
    this.loopCount = loopCount;
    return (T) this;
  }

  /**
   * Verifies that the multi instance loop execution is done in parallel.
   *
   * @return The handler.
   */
  @SuppressWarnings("unchecked")
  public T verifyParallel() {
    this.sequential = Boolean.FALSE;
    return (T) this;
  }

  /**
   * Verifies that the multi instance loop is sequentially executed.
   *
   * @return The handler.
   */
  @SuppressWarnings("unchecked")
  public T verifySequential() {
    this.sequential = Boolean.TRUE;
    return (T) this;
  }
}
