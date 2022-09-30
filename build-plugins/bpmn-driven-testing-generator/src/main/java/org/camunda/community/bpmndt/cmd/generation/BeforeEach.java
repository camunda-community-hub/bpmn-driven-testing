package org.camunda.community.bpmndt.cmd.generation;

import java.util.function.Function;

import javax.lang.model.element.Modifier;

import org.camunda.community.bpmndt.GeneratorStrategy;
import org.camunda.community.bpmndt.TestCaseActivity;
import org.camunda.community.bpmndt.TestCaseContext;
import org.camunda.community.bpmndt.api.AbstractTestCase;

import com.squareup.javapoet.MethodSpec;

/**
 * Overrides the {@code beforeEach} method of the {@link AbstractTestCase} to initialize the
 * activity handlers (e.g. {@code UserTaskHandler}) that are required for a given test case.
 */
public class BeforeEach implements Function<TestCaseContext, MethodSpec> {

  @Override
  public MethodSpec apply(TestCaseContext ctx) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("beforeEach")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED);

    // call #beforeEach of superclass
    builder.addStatement("super.$L()", "beforeEach");

    // handle possible test case errors
    if (!ctx.isValid()) {
      new HandleTestCaseErrors().accept(ctx, builder);
      return builder.build();
    }

    for (TestCaseActivity activity : ctx.getActivities()) {
      GeneratorStrategy strategy = activity.getStrategy();

      if (strategy.shouldHandleBefore()) {
        strategy.initHandlerBefore(builder);
      }

      strategy.initHandler(builder);

      if (strategy.shouldHandleAfter()) {
        strategy.initHandlerAfter(builder);
      }
    }

    return builder.build();
  }
}
