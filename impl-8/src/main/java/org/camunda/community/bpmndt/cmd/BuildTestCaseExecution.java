package org.camunda.community.bpmndt.cmd;

import java.util.List;
import java.util.function.BiConsumer;

import org.camunda.community.bpmndt.GeneratorStrategy;
import org.camunda.community.bpmndt.model.BpmnElementType;

import com.squareup.javapoet.MethodSpec;

public class BuildTestCaseExecution implements BiConsumer<List<GeneratorStrategy>, MethodSpec.Builder> {

  @Override
  public void accept(List<GeneratorStrategy> strategies, MethodSpec.Builder builder) {
    for (int i = 0; i < strategies.size(); i++) {
      var strategy = strategies.get(i);

      var element = strategy.getElement();

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
