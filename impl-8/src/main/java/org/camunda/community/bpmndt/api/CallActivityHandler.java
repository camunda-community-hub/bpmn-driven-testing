package org.camunda.community.bpmndt.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.camunda.community.bpmndt.api.TestCaseInstanceElement.CallActivityElement;
import org.camunda.community.bpmndt.api.TestCaseInstanceMemo.ProcessInstanceMemo;

import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;

/**
 * Fluent API to handle call activities. The called process must be simulated.
 *
 * @see TestCaseExecutor#simulateProcess(String)
 * @see TestCaseExecutor#simulateVersionedProcess(String, String)
 */
public class CallActivityHandler {

  // see src/main/resources/simulate-sub-process.bpmn
  private static final String DO_ESCALATION_CODE = "DO_ESCALATION";
  private static final String DO_ERROR_CODE = "DO_ERROR";
  private static final String ESCALATION_CODE = "bpmndtEscalationCode";
  private static final String ERROR_CODE = "bpmndtErrorCode";
  private static final String SIMULATE_ELEMENT_ID = "simulate";

  private final CallActivityElement element;

  private final Map<String, Object> variableMap = new HashMap<>();

  private String errorCode;
  private String escalationCode;
  private Consumer<ProcessInstanceAssert> verifier;
  private Consumer<ProcessInstanceAssert> inputVerifier;
  private Consumer<ProcessInstanceAssert> outputVerifier;
  private Object variables;
  private boolean waitForBoundaryEvent;

  private Consumer<CallActivityBindingType> bindingTypeConsumer;
  private Consumer<String> processIdExpressionConsumer;
  private Consumer<String> versionTagConsumer;

  private CallActivityBindingType expectedBindingType;
  private String expectedProcessId;
  private Boolean expectedPropagateAllChildVariables;
  private Boolean expectedPropagateAllParentVariables;
  private String expectedVersionTag;

  private Consumer<String> processIdConsumer;

  public CallActivityHandler(String elementId) {
    if (elementId == null) {
      throw new IllegalArgumentException("element ID is null");
    }

    element = new CallActivityElement();
    element.id = elementId;
  }

  public CallActivityHandler(CallActivityElement element) {
    if (element == null) {
      throw new IllegalArgumentException("element is null");
    }
    if (element.id == null) {
      throw new IllegalArgumentException("element ID is null");
    }

    this.element = element;
  }

  void apply(TestCaseInstance instance, long flowScopeKey) {
    var processInstanceKey = instance.getProcessInstanceKey(flowScopeKey);

    if (verifier != null) {
      verifier.accept(new ProcessInstanceAssert(processInstanceKey, BpmnAssert.getRecordStream()));
    }

    if (bindingTypeConsumer != null) {
      bindingTypeConsumer.accept(element.bindingType);
    }
    if (expectedBindingType != null && expectedBindingType != element.bindingType) {
      var message = "expected call activity %s to have binding type %s, but was %s";
      throw new AssertionError(String.format(message, element.id, expectedBindingType, element.bindingType));
    }

    if (processIdExpressionConsumer != null) {
      processIdExpressionConsumer.accept(element.processId);
    }

    if (versionTagConsumer != null) {
      versionTagConsumer.accept(element.versionTag);
    }
    if (expectedVersionTag != null && !expectedVersionTag.equals(element.versionTag)) {
      var message = "expected call activity %s to have version tag '%s', but was '%s'";
      throw new AssertionError(String.format(message, element.id, expectedVersionTag, element.versionTag));
    }

    var calledProcessInstance = getCalledProcessInstance(instance, flowScopeKey);
    var job = instance.getJob(calledProcessInstance.key, SIMULATE_ELEMENT_ID);

    if (expectedProcessId != null && !expectedProcessId.equals(calledProcessInstance.bpmnProcessId)) {
      var message = "expected call activity %s to call process '%s', but was '%s'";
      throw new AssertionError(String.format(message, element.id, expectedProcessId, calledProcessInstance.bpmnProcessId));
    }
    if (processIdConsumer != null) {
      processIdConsumer.accept(calledProcessInstance.bpmnProcessId);
    }

    if (expectedPropagateAllChildVariables != null && expectedPropagateAllChildVariables != element.propagateAllChildVariables) {
      var message = "expected call activity %s %sto propagate all child variables";
      throw new AssertionError(String.format(message, element.id, element.propagateAllChildVariables ? "not " : ""));
    }
    if (expectedPropagateAllParentVariables != null && expectedPropagateAllParentVariables != element.propagateAllParentVariables) {
      var message = "expected call activity %s %sto propagate all parent variables";
      throw new AssertionError(String.format(message, element.id, element.propagateAllParentVariables ? "not " : ""));
    }

    if (inputVerifier != null) {
      var processInstanceAssert = new ProcessInstanceAssert(calledProcessInstance.key, BpmnAssert.getRecordStream());
      inputVerifier.accept(processInstanceAssert);
    }

    if (waitForBoundaryEvent) {
      return;
    }

    if (errorCode != null || escalationCode != null) {
      if (variables != null) {
        instance.getClient().newSetVariablesCommand(calledProcessInstance.key).variables(variables).send().join();
      } else {
        instance.getClient().newSetVariablesCommand(calledProcessInstance.key).variables(variableMap).send().join();
      }

      if (errorCode != null) {
        // end called process instance with error end event
        instance.getClient().newSetVariablesCommand(calledProcessInstance.key).variables(Map.of(ERROR_CODE, errorCode)).send().join();
        instance.getClient().newThrowErrorCommand(job.key).errorCode(DO_ERROR_CODE).send().join();
      } else {
        // end called process instance with escalation end event
        instance.getClient().newSetVariablesCommand(calledProcessInstance.key).variables(Map.of(ESCALATION_CODE, escalationCode)).send().join();
        instance.getClient().newThrowErrorCommand(job.key).errorCode(DO_ESCALATION_CODE).send().join();
      }

      instance.hasTerminated(flowScopeKey, element.id);
    } else {
      // end called process instance
      if (variables != null) {
        instance.getClient().newCompleteCommand(job.key).variables(variables).send().join();
      } else {
        instance.getClient().newCompleteCommand(job.key).variables(variableMap).send().join();
      }

      instance.hasPassed(flowScopeKey, element.id);
    }

    if (outputVerifier != null) {
      var processInstanceAssert = new ProcessInstanceAssert(processInstanceKey, BpmnAssert.getRecordStream());
      outputVerifier.accept(processInstanceAssert);
    }
  }

  /**
   * Customizes the handler, using the given {@link Consumer} function. This method can be used to apply a common customization needed for different test
   * cases.
   *
   * <pre>
   * tc.handleCallActivity().customize(this::prepare);
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
   * Sets a variable to simulate the results of a called sub process execution.
   *
   * @param name  The name of the variable.
   * @param value The variable's value.
   * @return The handler.
   */
  public CallActivityHandler simulateVariable(String name, Object value) {
    if (variables != null) {
      throw new IllegalStateException("either use an object (POJO) as variables or a variable map");
    }
    variableMap.put(name, value);
    return this;
  }

  /**
   * Sets an object as variables to simulate the results of a called sub process execution.
   *
   * @param variables The variables as POJO.
   * @return The handler.
   */
  public CallActivityHandler simulateVariables(Object variables) {
    if (!variableMap.isEmpty()) {
      throw new IllegalStateException("either use an object (POJO) as variables or a variable map");
    }
    this.variables = variables;
    return this;
  }

  /**
   * Sets variables to simulate the results of a called sub process execution.
   *
   * @param variableMap A map of variables.
   * @return The handler.
   */
  public CallActivityHandler simulateVariableMap(Map<String, Object> variableMap) {
    if (variables != null) {
      throw new IllegalStateException("either use an object (POJO) as variables or a variable map");
    }
    this.variableMap.putAll(variableMap);
    return this;
  }

  /**
   * Verifies the call activity's waiting state.
   *
   * @param verifier Verifier that accepts an {@link ProcessInstanceAssert} instance.
   * @return The handler.
   */
  public CallActivityHandler verify(Consumer<ProcessInstanceAssert> verifier) {
    this.verifier = verifier;
    return this;
  }

  /**
   * Verifies the call activity's binding type.
   *
   * @param expectedBindingType The expected binding type.
   * @return The handler.
   */
  public CallActivityHandler verifyBindingType(CallActivityBindingType expectedBindingType) {
    this.expectedBindingType = expectedBindingType;
    return this;
  }

  /**
   * Verifies the call activity's binding type, using a consumer.
   *
   * @param bindingTypeConsumer A consumer asserting the binding type.
   * @return The handler.
   */
  public CallActivityHandler verifyBindingType(Consumer<CallActivityBindingType> bindingTypeConsumer) {
    this.bindingTypeConsumer = bindingTypeConsumer;
    return this;
  }

  /**
   * Verifies the call activity's input propagation, which parent variables have been propagated to the child process instance.
   *
   * @param inputVerifier Verifier that accepts an {@link ProcessInstanceAssert} instance, which is the child process instance, started by the call activity.
   * @return The handler.
   */
  public CallActivityHandler verifyInput(Consumer<ProcessInstanceAssert> inputVerifier) {
    this.inputVerifier = inputVerifier;
    return this;
  }

  /**
   * Verifies the call activity's output propagation - which child variables have been propagated to the parent process instance.
   * <p>
   * <b>Please note</b>: An application specific job worker may have already advanced the process instance and updated some variables.
   *
   * @param outputVerifier Verifier that accepts an {@link ProcessInstanceAssert} instance, which is the parent process instance, containing the call activity.
   * @return The handler.
   */
  public CallActivityHandler verifyOutput(Consumer<ProcessInstanceAssert> outputVerifier) {
    this.outputVerifier = outputVerifier;
    return this;
  }

  /**
   * Verifies that the call activity called a process with a specific ID.
   *
   * @param expectedProcessId The expected process ID.
   * @return The handler.
   */
  public CallActivityHandler verifyProcessId(String expectedProcessId) {
    this.expectedProcessId = expectedProcessId;
    return this;
  }

  /**
   * Verifies that the call activity called a process with a specific ID, using a consumer.
   *
   * @param processIdConsumer A consumer asserting the process ID.
   * @return The handler.
   */
  public CallActivityHandler verifyProcessId(Consumer<String> processIdConsumer) {
    this.processIdConsumer = processIdConsumer;
    return this;
  }

  /**
   * Verifies that the call activity has a specific process ID FEEL expression (see "Called element" section), using a consumer function.
   *
   * @param processIdExpressionConsumer A consumer asserting the process ID expression.
   * @return The handler.
   */
  public CallActivityHandler verifyProcessIdExpression(Consumer<String> processIdExpressionConsumer) {
    this.processIdExpressionConsumer = processIdExpressionConsumer;
    return this;
  }

  /**
   * Verifies if the call activity has the propagation of all child variables enabled (see "Output propagation" section).
   *
   * @param expectedPropagateAllChildVariables The expected value.
   * @return The handler.
   */
  public CallActivityHandler verifyPropagateAllChildVariables(Boolean expectedPropagateAllChildVariables) {
    this.expectedPropagateAllChildVariables = expectedPropagateAllChildVariables;
    return this;
  }

  /**
   * Verifies if the call activity has the propagation of all parent variables enabled (see "Input propagation" section).
   *
   * @param expectedPropagateAllParentVariables The expected value.
   * @return The handler.
   */
  public CallActivityHandler verifyPropagateAllParentVariables(Boolean expectedPropagateAllParentVariables) {
    this.expectedPropagateAllParentVariables = expectedPropagateAllParentVariables;
    return this;
  }

  /**
   * Verifies the call activity's version tag.
   *
   * @param expectedVersionTag The expected version tag, when binding type is "version tag".
   * @return The handler.
   */
  public CallActivityHandler verifyVersionTag(String expectedVersionTag) {
    this.expectedVersionTag = expectedVersionTag;
    return this;
  }

  /**
   * Verifies the call activity's version tag, using a consumer.
   *
   * @param versionTagConsumer A consumer asserting the version tag, when binding type is "version tag".
   * @return The handler.
   */
  public CallActivityHandler verifyVersionTag(Consumer<String> versionTagConsumer) {
    this.versionTagConsumer = versionTagConsumer;
    return this;
  }

  /**
   * Applies no action at the wait state. This is required when waiting for events (e.g. message, signal or timer events) that are attached as boundary events
   * on the element itself or on the surrounding scope (e.g. embedded subprocess).
   */
  public void waitForBoundaryEvent() {
    waitForBoundaryEvent = true;
  }

  /**
   * Sets the escalation code for ending the called process instance with an escalation end event, propagating the given code.
   *
   * @param escalationCode A specific escalation code or {@code null}.
   * @return The handler.
   */
  public CallActivityHandler withEscalationCode(String escalationCode) {
    this.escalationCode = escalationCode;
    return this;
  }

  /**
   * Sets the error code for ending the called process instance with an error end event, propagating the given code.
   *
   * @param errorCode A specific error code or {@code null}.
   * @return The handler.
   */
  public CallActivityHandler withErrorCode(String errorCode) {
    this.errorCode = errorCode;
    return this;
  }

  private ProcessInstanceMemo getCalledProcessInstance(TestCaseInstance instance, long flowScopeKey) {
    return instance.select(memo -> {
      var callActivity = memo.elements.stream().filter(e ->
          e.flowScopeKey == flowScopeKey && Objects.equals(e.id, element.id)
      ).findFirst();

      if (callActivity.isEmpty()) {
        var message = String.format("call activity %s of flow scope %d could not be found", element.id, flowScopeKey);
        throw instance.createException(message, flowScopeKey);
      }

      var callActivityKey = callActivity.get().key;

      var calledProcessInstance = memo.processInstances.stream().filter(
          processInstance -> processInstance.parentElementInstanceKey == callActivityKey
      ).findFirst();

      if (calledProcessInstance.isEmpty()) {
        var message = String.format("call activity %s of flow scope %d has not called a process", element.id, flowScopeKey);
        throw instance.createException(message, flowScopeKey);
      }

      return calledProcessInstance.get();
    });
  }
}
