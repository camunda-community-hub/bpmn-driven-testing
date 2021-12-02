package org.camunda.community.bpmndt.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.community.bpmndt.api.JobHandler.Cardinality;

/**
 * Fluent API for multi instance activites (call activities or tasks). This class does not support
 * sub process multi instances like embedded sub processes or transactions.
 *
 * @param <T> The generated multi instance handler type.
 * 
 * @param <U> The activity handler type (e.g. {@code UserTaskHandler}).
 */
public class MultiInstanceHandler<T extends MultiInstanceHandler<?, ?>, U> {

  private static final String MSG_LOOP_COUNT = "Expected multi instance '%s' to loop %dx, but was %dx";
  private static final String MSG_SEQUENTIAL = "Expected multi instance '%s' to be %s, but was %s";

  protected final TestCaseInstance instance;

  private final String activityId;

  /** ID of the multi instance scope. */
  private final String scopeId;

  private final Map<Integer, JobHandler> handlersBefore;
  private final Map<Integer, U> handlers;
  private final Map<Integer, JobHandler> handlersAfter;

  private Integer loopCount;
  private Boolean sequential;

  public MultiInstanceHandler(TestCaseInstance instance, String activityId) {
    this.instance = instance;
    this.activityId = activityId;

    scopeId = String.format("%s#%s", activityId, ActivityTypes.MULTI_INSTANCE_BODY);

    handlersBefore = new HashMap<>();
    handlers = new HashMap<>();
    handlersAfter = new HashMap<>();
  }

  protected void apply(ProcessInstance pi) {
    if (sequential != null && sequential != isSequential()) {
      throw new AssertionError(String.format(MSG_SEQUENTIAL, activityId, getText(sequential), getText(isSequential())));
    }

    int loopIndex = 0;
    while (!isEnded(pi)) {
      boolean shouldContinue = apply(pi, loopIndex);

      loopIndex++;

      if (!shouldContinue) {
        break;
      }
    }

    if (loopCount != null && loopCount != loopIndex) {
      throw new AssertionError(String.format(MSG_LOOP_COUNT, activityId, loopCount, loopIndex));
    }
  }

  /**
   * Applies the multi instance loop for the given index. Please note: This method will be overriden
   * by generated multi instance handler classes in case of call activities and or wait states
   * (external or user tasks).
   * 
   * @param pi The process instance, used to execute the test case.
   * 
   * @param loopIndex The current loop index.
   * 
   * @return {@code true}, if the multi instance execution should be continued. Otherwise
   *         {@code false}.
   */
  protected boolean apply(ProcessInstance pi, int loopIndex) {
    handleBefore(loopIndex).apply(pi);
    handleAfter(loopIndex).apply(pi);

    return true;
  }

  /**
   * Creates a new activity handler. Please note: This method will be overridden by generated multi
   * instance handler classes.
   * 
   * @param loopIndex The current loop index.
   * 
   * @return The newly created handler.
   */
  protected U createHandler(int loopIndex) {
    return null;
  }

  /**
   * Creates a new job handler for the asynchronous continuation after the activity.
   * 
   * @param loopIndex The current loop index.
   * 
   * @return The newly created job handler.
   */
  private JobHandler createJobHandlerAfter(int loopIndex) {
    return new JobHandler(getProcessEngine(), activityId, isSequential() ? Cardinality.ZERO_TO_ONE : Cardinality.ZERO_TO_N);
  }

  /**
   * Creates a new job handler for the asynchronous continuation before the activity.
   * 
   * @param loopIndex The current loop index.
   * 
   * @return The newly created job handler.
   */
  private JobHandler createJobHandlerBefore(int loopIndex) {
    return new JobHandler(getProcessEngine(), activityId, isSequential() ? Cardinality.ONE : Cardinality.ONE_TO_N);
  }

  protected U getHandler(int loopIndex) {
    return handlers.getOrDefault(loopIndex, handleDefault());
  }

  protected JobHandler getHandlerAfter(int loopIndex) {
    return handlersAfter.getOrDefault(loopIndex, handleAfterDefault());
  }

  protected JobHandler getHandlerBefore(int loopIndex) {
    return handlersBefore.getOrDefault(loopIndex, handleBeforeDefault());
  }

  protected ProcessEngine getProcessEngine() {
    return instance.getProcessEngine();
  }

  private String getText(boolean sequential) {
    return sequential ? "sequential" : "parallel";
  }

  /**
   * Returns the activity handler, which is applied on the multi instance loop with the given index.
   * 
   * @param loopIndex A specific loop index.
   * 
   * @return The handler for the given loop index.
   */
  public U handle(int loopIndex) {
    return handlers.computeIfAbsent(loopIndex, this::createHandler);
  }

  /**
   * Returns the job handler for the asynchronous continuation after the activity, which is applied on
   * the multi instance loop with the given index. This handler can be used to verify the process
   * variables after the activity was executed the nth time.
   * 
   * @param loopIndex A specific loop index.
   * 
   * @return The async after job handler for the given loop index.
   */
  public JobHandler handleAfter(int loopIndex) {
    return handlersAfter.computeIfAbsent(loopIndex, this::createJobHandlerAfter);
  }

  /**
   * Returns the default job handler for the asynchronous continuation after the activity.
   * 
   * @return The default async after job handler.
   */
  public JobHandler handleAfterDefault() {
    return handleAfter(-1);
  }

  /**
   * Returns the job handler for the asynchronous continuation before the activity, which is applied
   * on the multi instance loop with the given index. This handler can be used to verify the process
   * variables before the activity is executed the nth time.
   * 
   * @param loopIndex A specific loop index.
   * 
   * @return The async before job handler for the given loop index.
   */
  public JobHandler handleBefore(int loopIndex) {
    return handlersBefore.computeIfAbsent(loopIndex, this::createJobHandlerBefore);
  }

  /**
   * Returns the default job handler for the asynchronous continuation before the activity.
   * 
   * @return The default async before job handler.
   */
  public JobHandler handleBeforeDefault() {
    return handleBefore(-1);
  }

  /**
   * Returns the default activity handler.
   * 
   * @return The default handler.
   */
  public U handleDefault() {
    return handle(-1);
  }

  /**
   * Checks if the multi instance scope is ended or not.
   * 
   * @param pi The related process instance.
   * 
   * @return {@code true}, if the multi instance scope is ended. Otherwise {@code false}.
   */
  protected boolean isEnded(ProcessInstance pi) {
    HistoryService historyService = getProcessEngine().getHistoryService();

    List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
        .processInstanceId(pi.getId())
        .activityId(scopeId)
        .list();

    if (historicActivityInstances.isEmpty()) {
      throw new AssertionError(String.format("No historic activity instance found for multi instance scope '%s'", scopeId));
    }

    // in case of call activities another execution is created (see CallActivityHandler#execute)
    // when a BPMN error or escalation is simulated
    // or the call activity is waiting for a boundary event (e.g. a timer event)
    // then this execution will not be removed
    // this causes an additional historic activity instance for the scope ID
    // just check the latest instance, both will have ended
    return historicActivityInstances.get(0).getEndTime() != null;
  }

  /**
   * Determines if the multi instance loop is sequentially executed or not. Please note: If the multi
   * instance loop is defined as parallel, this method will be overridden by generated multi instance
   * handler classes.
   * 
   * @return {@code true}, if execution is done sequentially. {@code false}, if execution is done in
   *         parallel.
   */
  protected boolean isSequential() {
    return true;
  }

  /**
   * Registers the given call activity handler at the test case instance, so that it will be executed
   * when the custom call activity behavior is applied.<br>
   * Since there can be multiple handlers (one for each loop index), it is necessary to register the
   * correct one before the next multi instance loop is executed.
   * 
   * @param handler The handler to be executed next.
   */
  protected void registerCallActivityHandler(CallActivityHandler handler) {
    instance.registerCallActivityHandler(activityId, handler);
  }

  /**
   * Verifies that the multi instance loop is executed n-times.
   * 
   * @param loopCount The expected loop count at the point of time when the multi instance scope is
   *        left (finished or terminated by a boundary event).
   * 
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
