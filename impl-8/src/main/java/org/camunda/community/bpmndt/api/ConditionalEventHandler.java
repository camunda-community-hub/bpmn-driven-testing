package org.camunda.community.bpmndt.api;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.camunda.community.bpmndt.api.TestCaseInstanceElement.ConditionalEventElement;

import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaAssert;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;
import io.camunda.process.test.api.assertions.ProcessInstanceSelectors;

/**
 * Fluent API to handle conditional catch and boundary events.
 */
public class ConditionalEventHandler {

  private final ConditionalEventElement element;

  private final Map<String, Object> variableMap = new HashMap<>();

  private Consumer<ProcessInstanceAssert> verifier;
  private BiConsumer<CamundaClient, Long> action;
  private Object variables;

  private Consumer<String> conditionExpressionConsumer;

  /**
   * Creates a new handler.
   *
   * @param elementId ID of the BPMN conditional event.
   */
  public ConditionalEventHandler(String elementId) {
    this(elementId, null);
  }

  /**
   * Creates a new handler.
   *
   * @param elementId  ID of the BPMN conditional event.
   * @param attachedTo ID of the BPMN element, the conditional event is attached to.
   */
  public ConditionalEventHandler(String elementId, String attachedTo) {
    if (elementId == null) {
      throw new IllegalArgumentException("element ID is null");
    }

    element = new ConditionalEventElement();
    element.id = elementId;
    element.attachedTo = attachedTo;
  }

  public ConditionalEventHandler(ConditionalEventElement element) {
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
      var processInstanceSelector = ProcessInstanceSelectors.byKey(processInstanceKey);
      verifier.accept(CamundaAssert.assertThat(processInstanceSelector));
    }

    if (conditionExpressionConsumer != null) {
      conditionExpressionConsumer.accept(element.condition);
    }

    var client = instance.getClient();
    if (action != null) {
      action.accept(client, processInstanceKey);
    } else if (variables != null) {
      client.newSetVariablesCommand(processInstanceKey).variables(variables).send().join();
    } else if (!variableMap.isEmpty()) {
      client.newSetVariablesCommand(processInstanceKey).variables(variableMap).send().join();
    }
  }

  /**
   * Customizes the handler, using the given {@link Consumer} function. This method can be used to apply a common customization needed for different test
   * cases.
   *
   * <pre>
   * tc.handleConditionalCatchEvent().customize(this::prepare);
   * </pre>
   *
   * @param customizer A function that accepts a {@link ConditionalEventHandler}.
   * @return The handler.
   */
  public ConditionalEventHandler customize(Consumer<ConditionalEventHandler> customizer) {
    if (customizer != null) {
      customizer.accept(this);
    }
    return this;
  }

  /**
   * Sets variables to trigger the conditional event (condition becomes {@code true}) using a custom action, when the process instance is waiting at the
   * corresponding element.
   *
   * @param action A specific action that accepts a {@link CamundaClient} and the process instance key.
   * @see CamundaClient#newSetVariablesCommand(long)
   */
  public void execute(BiConsumer<CamundaClient, Long> action) {
    if (action == null) {
      throw new IllegalArgumentException("action is null");
    }
    this.action = action;
  }

  /**
   * Verifies the conditional event's waiting state.
   *
   * @param verifier Verifier that accepts an {@link ProcessInstanceAssert} instance.
   * @return The handler.
   */
  public ConditionalEventHandler verify(Consumer<ProcessInstanceAssert> verifier) {
    this.verifier = verifier;
    return this;
  }

  /**
   * Verifies that the conditional event has a specific condition FEEL expression (see "Condition" section), using a consumer function.
   *
   * @param conditionExpressionConsumer A consumer asserting the signal name expression.
   * @return The handler.
   */
  public ConditionalEventHandler verifyConditionExpression(Consumer<String> conditionExpressionConsumer) {
    this.conditionExpressionConsumer = conditionExpressionConsumer;
    return this;
  }

  /**
   * Sets a variable that is used to trigger the conditional event.
   *
   * @param name  The name of the variable.
   * @param value The variable's value.
   * @return The handler.
   * @see CamundaClient#newSetVariablesCommand(long)
   */
  public ConditionalEventHandler withVariable(String name, Object value) {
    if (variables != null) {
      throw new IllegalStateException("either use an object (POJO) as variables or a variable map");
    }
    variableMap.put(name, value);
    return this;
  }

  /**
   * Sets an object as variables that is used to trigger the conditional event.
   *
   * @param variables The variables as POJO.
   * @return The handler.
   * @see CamundaClient#newSetVariablesCommand(long)
   */
  public ConditionalEventHandler withVariables(Object variables) {
    if (!variableMap.isEmpty()) {
      throw new IllegalStateException("either use an object (POJO) as variables or a variable map");
    }
    this.variables = variables;
    return this;
  }

  /**
   * Sets variables that are used to trigger the conditional event.
   *
   * @param variableMap A map of variables.
   * @return The handler.
   * @see CamundaClient#newSetVariablesCommand(long)
   */
  public ConditionalEventHandler withVariableMap(Map<String, Object> variableMap) {
    if (variables != null) {
      throw new IllegalStateException("either use an object (POJO) as variables or a variable map");
    }
    this.variableMap.putAll(variableMap);
    return this;
  }
}
