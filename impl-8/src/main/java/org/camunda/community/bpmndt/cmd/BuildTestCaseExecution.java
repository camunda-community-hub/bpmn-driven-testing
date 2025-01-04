package org.camunda.community.bpmndt.cmd;

import java.util.List;
import java.util.function.BiConsumer;

import org.camunda.community.bpmndt.GeneratorStrategy;
import org.camunda.community.bpmndt.model.BpmnElement;
import org.camunda.community.bpmndt.model.BpmnElementType;

import com.squareup.javapoet.MethodSpec;

/**
 * Builds the execution for the top level process instance scope, if {@code parent} is {@code null}, or for a specific scope if {@code parent} is not
 * {@code null} - strategies with a different parent scope are skipped.
 */
class BuildTestCaseExecution implements BiConsumer<List<GeneratorStrategy>, MethodSpec.Builder> {

  private final BpmnElement parent;

  BuildTestCaseExecution() {
    this(null);
  }

  BuildTestCaseExecution(BpmnElement parent) {
    this.parent = parent;
  }

  @Override
  public void accept(List<GeneratorStrategy> strategies, MethodSpec.Builder builder) {
    for (int i = 0; i < strategies.size(); i++) {
      var strategy = strategies.get(i);

      var element = strategy.getElement();
      if ((element.hasParent() && element.getParent() != parent) || (!element.hasParent() && parent != null)) {
        continue; // skip strategy if parent differs
      }

      builder.addCode("\n// $L: $L\n", element.getTypeName(), element.getId());

      strategy.applyHandler(builder);

      if (element.hasPrevious(BpmnElementType.EVENT_BASED_GATEWAY)) {
        // assert that event based gateway has been passed
        strategies.get(i - 1).hasPassed(builder);
      }

      if (element.getType() == BpmnElementType.EVENT_BASED_GATEWAY) {
        strategy.isWaitingAt(builder);
      } else {
        strategy.hasPassed(builder);
      }
    }
  }
}
