package org.camunda.community.bpmndt.platform8.cmd;

import java.util.List;
import java.util.function.BiConsumer;

import org.camunda.community.bpmndt.model.platform8.BpmnElement;
import org.camunda.community.bpmndt.model.platform8.BpmnElementType;
import org.camunda.community.bpmndt.platform8.GeneratorStrategy;
import org.camunda.community.bpmndt.platform8.TestCaseContext;

import com.squareup.javapoet.MethodSpec;

public class BuildTestCaseExecution implements BiConsumer<List<GeneratorStrategy>, MethodSpec.Builder> {

  private final TestCaseContext ctx;

  public BuildTestCaseExecution(TestCaseContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public void accept(List<GeneratorStrategy> strategies, MethodSpec.Builder builder) {
    for (int i = 0; i < strategies.size(); i++) {
      GeneratorStrategy strategy = strategies.get(i);

      BpmnElement element = strategy.getElement();

      if (i != 0) {
        builder.addCode("\n");
      }

      builder.addCode("// $L: $L\n", element.getTypeName(), element.getId());

      strategy.applyHandler(builder);

      if (element.getType() == BpmnElementType.LINK_THROW) {
        // since there is no activity for an intermediate link throw event
        // a process instance will not pass and will never wait at such an element
        continue;
      }

      if (element.hasPrevious(BpmnElementType.EVENT_BASED_GATEWAY)) {
        // assert that event based gateway has been passed
        ctx.getStrategy(element.getPrevious().getId()).hasPassed(builder);
      }

      if (element.getType() == BpmnElementType.EVENT_BASED_GATEWAY) {
        strategy.isWaitingAt(builder);
      } else if ((element.hasNext()) || element.isProcessEnd()) {
        strategy.hasPassed(builder);
      } else {
        // assert that process instance is waiting at the test case's last element
        // which is not the process end
        strategy.isWaitingAt(builder);
      }
    }
  }
}
