package org.camunda.community.bpmndt.api;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
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
import org.camunda.bpm.engine.variable.value.TypedValue;

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

  /**
   * Simulates the execution of a call activity.
   * 
   * 1. Gets the call activity definition and verifies it.
   * 
   * 2. Performs and verifies the variable mapping between super execution and stubbed sub instance.
   * 
   * 3. Create sub execution and propagate possible error or escalation events.
   * 
   * @param pi The current process instance.
   * 
   * @param execution The current execution.
   * 
   * @param behavior The call activity's original behavior.
   * 
   * @return {@code true}, if the execution should leave (continue). {@code false}, if the execution
   *         should wait.
   * 
   * @throws Exception Exception If the occurrence of an error end event is simulated and the error
   *         propagation fails.
   */
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

    VariableScope subInstance = new CallActivityVariableScope(subVariables);

    verifyInput(subInstance);

    if (variableMapping != null) {
      variableMapping.mapOutputVariables(execution, subInstance);
    }

    verifyOutput(execution);

    ActivityExecution subExecution = execution.createExecution();

    if (errorCode != null) {
      BpmnExceptionHandler.propagateError(errorCode, errorMessage, null, subExecution);
      return false;
    }

    if (escalationCode != null) {
      EscalationHandler.propagateEscalation(subExecution, escalationCode);
      return false;
    }

    subExecution.remove();

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
   * Verifies the definition of the call activity and the state before it is executed (actually before
   * the {@code mapInputVariables} method of a possible {@link DelegateVariableMapping} is invoked).
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

  /**
   * Internal class that provides a given variable map as {@link VariableScope}. This is required for
   * the {@code mapOutputVariables} method of {@link DelegateVariableMapping}, which can be used to
   * map variables between super execution and sub instance.
   */
  private static class CallActivityVariableScope implements VariableScope {

    private final VariableMap variables;

    private CallActivityVariableScope(VariableMap variables) {
      this.variables = variables;
    }

    @Override
    public Object getVariable(String variableName) {
      return variables.get(variableName);
    }

    @Override
    public Object getVariableLocal(String variableName) {
      return null;
    }

    @Override
    public <T extends TypedValue> T getVariableLocalTyped(String variableName) {
      return null;
    }

    @Override
    public <T extends TypedValue> T getVariableLocalTyped(String variableName, boolean deserializeValue) {
      return null;
    }

    @Override
    public Set<String> getVariableNames() {
      return variables.keySet();
    }

    @Override
    public Set<String> getVariableNamesLocal() {
      return Collections.emptySet();
    }

    @Override
    public Map<String, Object> getVariables() {
      return variables;
    }

    @Override
    public String getVariableScopeKey() {
      return "execution";
    }

    @Override
    public Map<String, Object> getVariablesLocal() {
      return Collections.emptyMap();
    }

    @Override
    public VariableMap getVariablesLocalTyped() {
      return Variables.createVariables();
    }

    @Override
    public VariableMap getVariablesLocalTyped(boolean deserializeValues) {
      return Variables.createVariables();
    }

    @Override
    public VariableMap getVariablesTyped() {
      return variables;
    }

    @Override
    public VariableMap getVariablesTyped(boolean deserializeValues) {
      return variables;
    }

    @Override
    public <T extends TypedValue> T getVariableTyped(String variableName) {
      return variables.getValueTyped(variableName);
    }

    @Override
    public <T extends TypedValue> T getVariableTyped(String variableName, boolean deserializeValue) {
      return variables.getValueTyped(variableName);
    }

    @Override
    public boolean hasVariable(String variableName) {
      return variables.containsKey(variableName);
    }

    @Override
    public boolean hasVariableLocal(String variableName) {
      return false;
    }

    @Override
    public boolean hasVariables() {
      return !variables.isEmpty();
    }

    @Override
    public boolean hasVariablesLocal() {
      return false;
    }

    @Override
    public void removeVariable(String variableName) {
      variables.remove(variableName);
    }

    @Override
    public void removeVariableLocal(String variableName) {
      // not supported
    }

    @Override
    public void removeVariables() {
      variables.clear();
    }

    @Override
    public void removeVariables(Collection<String> variableNames) {
      variableNames.stream().forEach(variables::remove);
    }

    @Override
    public void removeVariablesLocal() {
      // not supported
    }

    @Override
    public void removeVariablesLocal(Collection<String> variableNames) {
      // not supported
    }

    @Override
    public void setVariable(String variableName, Object value) {
      variables.putValue(variableName, value);
    }

    @Override
    public void setVariableLocal(String variableName, Object value) {
      // not supported
    }

    @Override
    public void setVariables(Map<String, ? extends Object> variables) {
      this.variables.putAll(variables);
    }

    @Override
    public void setVariablesLocal(Map<String, ? extends Object> variables) {
      // not supported
    }
  }
}
