package org.camunda.community.bpmndt.cmd.generation;

import java.util.function.Function;

import javax.lang.model.element.Modifier;

import org.camunda.community.bpmndt.GeneratorStrategy;
import org.camunda.community.bpmndt.TestCaseActivity;
import org.camunda.community.bpmndt.TestCaseActivityType;
import org.camunda.community.bpmndt.TestCaseContext;
import org.junit.runner.Description;

import com.squareup.javapoet.MethodSpec;

public class Starting implements Function<TestCaseContext, MethodSpec> {

  @Override
  public MethodSpec apply(TestCaseContext ctx) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("starting")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .addParameter(Description.class, "description");

    // call #starting of superclass
    builder.addStatement("super.$L(description)", "starting");

    // handle possible test case errors
    if (!ctx.isValid()) {
      new HandleTestCaseErrors().accept(ctx, builder);
      return builder.build();
    }

    for (TestCaseActivity activity : ctx.getActivities()) {
      GeneratorStrategy strategy = activity.getStrategy();

      if (activity.isAsyncBefore() || (activity.getType() == TestCaseActivityType.CALL_ACTIVITY && !activity.isMultiInstance())) {
        // call activities have asyncBefore enabled by default
        // see BpmndtParseListener#parseCallActivity
        strategy.initHandlerBefore(builder);
      }

      strategy.initHandler(builder);

      if (activity.isAsyncAfter()) {
        strategy.initHandlerAfter(builder);
      }
    }

    return builder.build();
  }
}
