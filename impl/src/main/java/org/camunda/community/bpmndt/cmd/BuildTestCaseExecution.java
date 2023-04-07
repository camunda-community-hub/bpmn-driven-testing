package org.camunda.community.bpmndt.cmd;

import java.util.List;
import java.util.function.BiConsumer;

import org.camunda.community.bpmndt.GeneratorStrategy;
import org.camunda.community.bpmndt.TestCaseContext;
import org.camunda.community.bpmndt.model.TestCaseActivity;
import org.camunda.community.bpmndt.model.TestCaseActivityType;

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

      TestCaseActivity activity = strategy.getActivity();

      if (i != 0) {
        builder.addCode("\n");
      }

      builder.addCode("// $L: $L\n", activity.getTypeName(), activity.getId());

      if (strategy.shouldHandleBefore()) {
        strategy.applyHandlerBefore(builder);
      }

      strategy.applyHandler(builder);

      if (strategy.shouldHandleAfter()) {
        strategy.applyHandlerAfter(builder);
      }

      if (activity.hasPrevious(TestCaseActivityType.EVENT_BASED_GATEWAY)) {
        // assert that event based gateway has been passed
        ctx.getStrategy(activity.getPrevious().getId()).hasPassed(builder);
      }

      if (activity.getType() == TestCaseActivityType.EVENT_BASED_GATEWAY) {
        strategy.isWaitingAt(builder);
      } else if (activity.getType() == TestCaseActivityType.LINK_THROW) {
        // since there is no activity for an intermediate link throw event
        // a process instance will not pass and will never wait at such an activity
      } else if ((activity.hasNext()) || activity.isProcessEnd()) {
        strategy.hasPassed(builder);
      } else if (activity.getType() == TestCaseActivityType.SCOPE) {
        strategy.hasPassed(builder);
      } else {
        // assert that process instance is waiting at the test case's last activity
        // which is not the process end
        // see BpmndtParseListener#instrumentEndActivity
        strategy.isWaitingAt(builder);
      }
    }
  }
}
