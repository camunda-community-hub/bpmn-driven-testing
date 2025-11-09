package org.camunda.community.bpmndt.api;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.helper.BpmnExceptionHandler;
import org.camunda.bpm.engine.impl.bpmn.helper.EscalationHandler;
import org.camunda.bpm.engine.impl.core.model.CallableElement;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
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

  private final AbstractTestCase<?> testCase;
  private final String activityId;

  private BiConsumer<ProcessInstanceAssert, CallActivityDefinition> verifier;
  private Consumer<VariableScope> inputVerifier;
  private Consumer<VariableScope> outputVerifier;

  private String errorCode;
  private String errorMessage;
  private String escalationCode;

  private boolean waitForBoundaryEvent;

  /**
   * Test case, used to execute the called sub process.
   */
  protected AbstractTestCase<?> subTestCase;

  public CallActivityHandler(AbstractTestCase<?> testCase, String activityId) {
    this.testCase = testCase;
    this.activityId = activityId;

    testCase.instance.registerCallActivityHandler(activityId, this);
  }

  protected void apply(ProcessInstance pi) {
    if (subTestCase == null) {
      return;
    }

    HistoryService historyService = testCase.getProcessEngine().getHistoryService();

    // find sub process instances via history service
    // because the process instance may already be finished
    List<HistoricProcessInstance> subHpis = historyService.createHistoricProcessInstanceQuery()
        .superProcessInstanceId(pi.getId())
        .processDefinitionId(subTestCase.instance.getProcessDefinitionId())
        .orderByProcessInstanceStartTime().asc()
        .list();

    if (subHpis.isEmpty()) {
      throw new AssertionError(String.format("No historic process instance found for call activity %s", activityId));
    }

    HistoricProcessInstance subHpi = subHpis.get(subHpis.size() - 1);

    // wrap historic process instance
    ExecutionEntity executionEntity = new ExecutionEntity();
    executionEntity.setBusinessKey(subHpi.getBusinessKey());
    executionEntity.setId(subHpi.getId());
    executionEntity.setProcessDefinitionId(subHpi.getProcessDefinitionId());
    executionEntity.setProcessDefinitionKey(subHpi.getProcessDefinitionKey());
    executionEntity.setProcessInstanceId(subHpi.getId());
    executionEntity.setRootProcessInstanceId(subHpi.getRootProcessInstanceId());
    executionEntity.setTenantId(subHpi.getTenantId());

    subTestCase.instance.setProcessInstance(executionEntity);
    subTestCase.execute(executionEntity);
  }

  /**
   * Customizes the handler, using the given {@link Consumer} function. This method can be used to apply a common customization needed for different test
   * cases.
   *
   * <pre>
   * tc.handleCallActivity().customize(this::prepareCallActivity);
   * </pre>
   *
   * @param customizer A function that accepts a {@link CallActivityHandler}.
   * @return The handler.
   */
  public CallActivityHandler customize(Consumer<CallActivityHandler> customizer) {
    if (customizer != null) {
      customizer.accept(this);
    }
    return this;
  }

  /**
   * Executes the given test case, using the sub process instance, started by the call activity. If this method is called, the call activity is not simulated.
   *
   * <pre>
   * tc.handleCallActivity().executeTestCase(new TC_subProcess(), it -> {
   *   // customize behavior and verify sub process instance
   * });
   * </pre>
   *
   * @param subTestCase A specific test case, generated for the called process, that starts with a non start event.
   * @param customizer  Customizer function that accept the initialized test case.
   */
  public <T extends AbstractTestCase<?>> void executeTestCase(T subTestCase, Consumer<T> customizer) {
    if (subTestCase == null) {
      throw new IllegalArgumentException("Test case is null");
    }

    subTestCase.executedWithinCallActivity = true;
    subTestCase.beforeEach();

    if (customizer != null) {
      customizer.accept(subTestCase);
    }

    testCase.addSubTestCase(subTestCase);

    this.subTestCase = subTestCase;
  }

  private String getDefinitionTenantId(ProcessInstance pi, ActivityExecution execution, CallableElement callableElement) {
    final String methodName = "getDefinitionTenantId";

    Optional<Method> foundMethod = Stream.of(CallableElement.class.getMethods())
        .filter(method -> method.getName().equals(methodName))
        .findFirst();

    if (foundMethod.isEmpty()) {
      throw new RuntimeException(String.format("Class '%s' misses required method '%s'", CallableElement.class.getName(), methodName));
    }

    Method method = foundMethod.get();

    try {
      if (method.getParameterCount() == 1) {
        // Camunda BPM version <= 7.16.0
        return (String) method.invoke(callableElement, execution);
      } else {
        // Camunda BPM version >= 7.17.0
        return (String) method.invoke(callableElement, execution, pi.getTenantId());
      }
    } catch (Exception e) {
      throw new RuntimeException(String.format("Method '%s' could not be invoked", methodName), e);
    }
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
   * Simulates the execution of a call activity.
   * <br>
   * 1. Gets the call activity definition and verifies it.
   * <br>
   * 2. Performs and verifies the variable mapping between super execution and the simulated sub instance.
   * <br>
   * 3. Create sub execution and propagate possible error or escalation events.
   *
   * @param pi        The current process instance.
   * @param execution The current execution.
   * @param behavior  The call activity's original behavior.
   * @return {@code true}, if the execution should leave (continue). {@code false}, if the execution should wait.
   * @throws Exception Exception If the occurrence of an error end event is simulated and the error propagation fails.
   */
  protected boolean simulate(ProcessInstance pi, ActivityExecution execution, CallActivityBehavior behavior) throws Exception {
    verify(pi, execution, behavior);

    CallableElement callableElement = behavior.getCallableElement();

    // input
    VariableMap subVariables = callableElement.getInputVariables(execution);

    DelegateVariableMapping variableMapping = (DelegateVariableMapping) behavior.resolveDelegateClass(execution);
    if (variableMapping != null) {
      variableMapping.mapInputVariables(execution, subVariables);
    }

    VariableScope subInstance = new CallActivityVariableScope(subVariables);

    verifyInput(subInstance);

    ActivityExecution subExecution = execution.createExecution();
    subExecution.setVariables(subVariables);

    // output
    VariableMap outputVariables = callableElement.getOutputVariables(subInstance);
    VariableMap outputVariablesLocal = callableElement.getOutputVariablesLocal(subInstance);

    execution.setVariables(outputVariables);
    execution.setVariablesLocal(outputVariablesLocal);

    if (variableMapping != null) {
      variableMapping.mapOutputVariables(execution, subInstance);
    }

    verifyOutput(execution);

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
   * Simulates the occurrence of an error end event within the called sub instance.
   *
   * @param errorCode    The error code of the attached boundary error event.
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
   * Verifies the definition of the call activity and the state before it is executed (actually before the {@code mapInputVariables} method of a possible
   * {@link DelegateVariableMapping} is invoked).
   *
   * @param verifier Verifier that accepts an {@link ProcessInstanceAssert} and an {@link CallActivityDefinition} instance.
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
   * Verifies a call activity definition.
   *
   * @param pi        The current process instance.
   * @param execution The current execution.
   * @param behavior  The call activity's original behavior.
   */
  protected void verify(ProcessInstance pi, ActivityExecution execution, CallActivityBehavior behavior) {
    CallableElement callableElement = behavior.getCallableElement();

    CallActivityDefinition callActivityDefinition = new CallActivityDefinition();
    callActivityDefinition.binding = callableElement.getBinding();
    callActivityDefinition.businessKey = callableElement.getBusinessKey(execution);
    callActivityDefinition.definitionKey = callableElement.getDefinitionKey(execution);
    callActivityDefinition.definitionTenantId = getDefinitionTenantId(pi, execution, callableElement);
    callActivityDefinition.inputs = !callableElement.getInputs().isEmpty();
    callActivityDefinition.outputs = !callableElement.getOutputs().isEmpty() || !callableElement.getOutputsLocal().isEmpty();
    callActivityDefinition.version = callableElement.getVersion(execution);
    callActivityDefinition.versionTag = callableElement.getVersionTag(execution);

    verify(pi, callActivityDefinition);
  }

  /**
   * Verifies the state after the {@code mapInputVariables} method of a possible {@link DelegateVariableMapping} was invoked.<br> Please note: This method can
   * also be used to simulate the behavior of a called process. The variables set, will be available when the {@code mapOutputVariables} method of a possible
   * {@link DelegateVariableMapping} is invoked.
   *
   * @param inputVerifier Verifier that accepts the {@link VariableScope} of the sub process instance.
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

  protected void verifyInput(VariableMap subVariables) {
    VariableScope subInstance = new CallActivityVariableScope(subVariables);
    verifyInput(subInstance);
  }

  /**
   * Verifies the state after the {@code mapOutputVariables} method of a possible {@link DelegateVariableMapping} was invoked.
   *
   * @param outputVerifier Verifier that accepts the {@link VariableScope} of the super execution.
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
   * Lets the call activity become a wait state. This is required to wait for events (e.g. message, signal or timer events) that are attached as boundary events
   * on the activity itself or on the surrounding scope (e.g. embedded subprocess).
   */
  public void waitForBoundaryEvent() {
    waitForBoundaryEvent = true;
  }

  /**
   * Sets the error message, which is used when the next activity is an error boundary event.
   *
   * @param errorMessage An error message or {@code null}.
   * @return The handler.
   */
  public CallActivityHandler withErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }

  /**
   * Internal class that provides a given variable map as {@link VariableScope}. This is required for the {@code mapOutputVariables} method of
   * {@link DelegateVariableMapping}, which can be used to map variables between super execution and sub instance.
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
      variableNames.forEach(variables::remove);
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
