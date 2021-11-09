package org.camunda.community.bpmndt.api;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.helper.BpmnExceptionHandler;
import org.camunda.bpm.engine.impl.bpmn.helper.EscalationHandler;
import org.camunda.bpm.engine.impl.core.model.CallableElement;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

/**
 * Fluent API to handle call activities.
 */
public class CallActivityHandler {

  private BiConsumer<ProcessInstanceAssert, CallActivityDefinition> verifier;
  private Consumer<VariableScope> inputVerifier;
  private Consumer<VariableScope> outputVerifier;

  private String errorCode;
  private String errorMessage;
  private String escalationCode;

  private boolean waitForBoundaryEvent;

  public CallActivityHandler(TestCaseInstance instance, String activityId) {
    instance.registerCallActivityHandler(activityId, this);
  }

  protected boolean execute(ProcessInstance pi, ActivityExecution execution, CallActivityBehavior behavior) throws Exception {
    CallableElement callableElement = behavior.getCallableElement();

    CallActivityDefinition callActivityDefinition = new CallActivityDefinition();
    callActivityDefinition.setBinding(callableElement.getBinding());
    callActivityDefinition.setBusinessKey(callableElement.getBusinessKey(execution));
    callActivityDefinition.setDefinitionKey(callableElement.getDefinitionKey(execution));
    callActivityDefinition.setDefinitionTenantId(callableElement.getDefinitionTenantId(execution));
    callActivityDefinition.setVersion(callableElement.getVersion(execution));
    callActivityDefinition.setVersionTag(callableElement.getVersionTag(execution));

    verify(pi, callActivityDefinition);

    VariableMap subVariables = Variables.createVariables();

    DelegateVariableMapping variableMapping = (DelegateVariableMapping) behavior.resolveDelegateClass(execution);
    if (variableMapping != null) {
      variableMapping.mapInputVariables(execution, subVariables);
    }

    ActivityExecution subInstance = execution.createExecution();
    subInstance.setVariables(subVariables);

    verifyInput(subInstance);

    if (variableMapping != null) {
      variableMapping.mapOutputVariables(execution, subInstance);
    }

    verifyOutput(execution);

    if (errorCode != null) {
      BpmnExceptionHandler.propagateError(errorCode, errorMessage, null, subInstance);
      return false;
    }

    if (escalationCode != null) {
      EscalationHandler.propagateEscalation(subInstance, escalationCode);
      return false;
    }

    subInstance.remove();

    return !waitForBoundaryEvent;
  }

  /**
   * Determines if the call activity is waiting for a boundary message, signal or timer event.
   * 
   * @return {@code true}, if it is waiting for a boundary event. {@code false}, if not.
   */
  public boolean isWaitingForBoundaryEvent() {
    return waitForBoundaryEvent;
  }

  /**
   * Simulates the occurrence of an error end event within the called sub instance.
   * 
   * @param errorCode The error code of the attached boundary error event.
   * 
   * @param errorMessage An error message or {@code null}.
   */
  public void simulateBpmnError(String errorCode, String errorMessage) {
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;

    escalationCode = null;

    waitForBoundaryEvent = false;
  }

  /**
   * Simulates the occurrence of an escalation end event within the called sub instance.
   * 
   * @param escalationCode The escalation code of the attached boundary escalation event.
   */
  public void simulateEscalation(String escalationCode) {
    this.escalationCode = escalationCode;

    errorCode = null;
    errorMessage = null;

    waitForBoundaryEvent = false;
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
    this.verifier = verifier;
    return this;
  }

  protected void verify(ProcessInstance pi, CallActivityDefinition callActivityDefinition) {
    if (verifier != null) {
      verifier.accept(ProcessEngineTests.assertThat(pi), callActivityDefinition);
    }
  }

  /**
   * Verifies the state after the {@code mapInputVariables} method of a possible
   * {@link DelegateVariableMapping} was invoked.<br>
   * Please note: This method can also be used to simulate the behavior of a called process. The
   * variables set, will be available when the {@code mapOutputVariables} method of a possible
   * {@link DelegateVariableMapping} is invoked.
   * 
   * @param inputVerifier Verifier that accepts the {@link VariableScope} of the sub process instance.
   * 
   * @return The handler.
   */
  public CallActivityHandler verifyInput(Consumer<VariableScope> inputVerifier) {
    this.inputVerifier = inputVerifier;
    return this;
  }

  protected void verifyInput(VariableScope variables) {
    if (inputVerifier != null) {
      inputVerifier.accept(variables);
    }
  }

  /**
   * Verifies the state after the {@code mapOutputVariables} method of a possible
   * {@link DelegateVariableMapping} was invoked.
   * 
   * @param outputVerifier Verifier that accepts the {@link VariableScope} of the super execution.
   * 
   * @return The handler.
   */
  public CallActivityHandler verifyOutput(Consumer<VariableScope> outputVerifier) {
    this.outputVerifier = outputVerifier;
    return this;
  }

  protected void verifyOutput(VariableScope variables) {
    if (outputVerifier != null) {
      outputVerifier.accept(variables);
    }
  }

  /**
   * Lets the call activity become a wait state. This is required to wait for events (e.g. message,
   * signal or timer events) that are attached as boundary events on the activity itself or on the
   * surrounding scope (e.g. embedded subprocess).
   */
  public void waitForBoundaryEvent() {
    waitForBoundaryEvent = true;
  }

  /**
   * Sets the error message, which is used when the next activity is an error boundary event.
   * 
   * @param errorMessage An error message or {@code null}.
   * 
   * @return The handler.
   */
  public CallActivityHandler withErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }
}
