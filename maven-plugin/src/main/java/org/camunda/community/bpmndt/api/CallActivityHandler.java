package org.camunda.community.bpmndt.api;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;

/**
 * Fluent API to handle call activities.
 */
public class CallActivityHandler {

  private final TestCaseInstance instance;
  private final String activityId;

  public CallActivityHandler(TestCaseInstance instance, String activityId) {
    this.instance = instance;
    this.activityId = activityId;
  }

  /**
   * Verifies the state before the call activity is executed (actually before the
   * {@code mapInputVariables} method of a possible {@link DelegateVariableMapping} is invoked).
   * 
   * @param verifier Verifier that accepts an {@link ProcessInstanceAssert} and an
   *        {@link CallActivityDefinition} instance.
   * 
   * @return The handler.
   */
  public CallActivityHandler verify(BiConsumer<ProcessInstanceAssert, CallActivityDefinition> verifier) {
    instance.verifyCallActivity(activityId, verifier);
    return this;
  }

  /**
   * Verifies the state after the {@code mapInputVariables} method of a possible
   * {@link DelegateVariableMapping} was invoked.<br>
   * Please note: This method can also be used to simulate the behavior of a called process. The
   * variables set, will be available when the {@code mapOutputVariables} method of a possible
   * {@link DelegateVariableMapping} is invoked.
   * 
   * @param verifier Verifier that accepts the {@link VariableScope} of the sub process instance.
   * 
   * @return The handler.
   */
  public CallActivityHandler verifyInput(Consumer<VariableScope> verifier) {
    instance.verifyCallActivityInput(activityId, verifier);
    return this;
  }

  /**
   * Verifies the state after the {@code mapOutputVariables} method of a possible
   * {@link DelegateVariableMapping} was invoked.
   * 
   * @param verifier Verifier that accepts the {@link VariableScope} of the super execution.
   * 
   * @return The handler.
   */
  public CallActivityHandler verifyOutput(Consumer<VariableScope> verifier) {
    instance.verifyCallActivityOutput(activityId, verifier);
    return this;
  }
}
