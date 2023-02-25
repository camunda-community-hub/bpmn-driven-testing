package org.camunda.community.bpmndt.cmd;

import java.util.List;
import java.util.function.BiConsumer;

import org.camunda.community.bpmndt.GeneratorStrategy;
import org.camunda.community.bpmndt.TestCaseActivity;
import org.camunda.community.bpmndt.TestCaseActivityType;

import com.squareup.javapoet.MethodSpec;

public class BuildTestCaseExecution implements BiConsumer<List<TestCaseActivity>, MethodSpec.Builder> {

  @Override
  public void accept(List<TestCaseActivity> activities, MethodSpec.Builder builder) {
    for (int i = 0; i < activities.size(); i++) {
      TestCaseActivity activity = activities.get(i);

      if (i != 0) {
        builder.addCode("\n");
      }

      builder.addCode("// $L: $L\n", activity.getTypeName(), activity.getId());

      GeneratorStrategy strategy = activity.getStrategy();

      if (strategy.shouldHandleBefore()) {
        strategy.applyHandlerBefore(builder);
      }

      strategy.applyHandler(builder);

      if (strategy.shouldHandleAfter()) {
        strategy.applyHandlerAfter(builder);
      }

      if (activity.hasPrev(TestCaseActivityType.EVENT_BASED_GATEWAY)) {
        // assert that event based gateway has been passed
        activity.getPrev().getStrategy().hasPassed(builder);
      }

      if (activity.getType() == TestCaseActivityType.EVENT_BASED_GATEWAY) {
        activity.getStrategy().isWaitingAt(builder);
      } else if (activity.getType() == TestCaseActivityType.LINK_THROW) {
        // since there is no activity for an intermediate link throw event
        // a process instance will not pass and will never wait at such an activity
      } else if ((activity.hasNext()) || activity.isProcessEnd()) {
        activity.getStrategy().hasPassed(builder);
      } else if (activity.getParent() != null) {
        activity.getStrategy().hasPassed(builder);
      } else {
        // assert that process instance is waiting at the test case's last activity
        // which is not the process end
        // see BpmndtParseListener#instrumentEndActivity
        activity.getStrategy().isWaitingAt(builder);
      }
    }
  }
}
