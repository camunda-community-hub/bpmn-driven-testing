package org.camunda.community.bpmndt.cmd.generation;

import java.util.function.Function;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.community.bpmndt.GeneratorStrategy;
import org.camunda.community.bpmndt.TestCaseActivity;
import org.camunda.community.bpmndt.TestCaseContext;

import com.squareup.javapoet.MethodSpec;

/**
 * Function that builds the method, which executes the actual test case.
 */
public class Execute implements Function<TestCaseContext, MethodSpec> {

  @Override
  public MethodSpec apply(TestCaseContext ctx) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("execute")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .addParameter(ProcessInstance.class, "pi");

    // handle possible test case errors
    if (!ctx.isValid()) {
      new HandleTestCaseErrors().accept(ctx, builder);
      return builder.build();
    }

    for (int i = 0; i < ctx.getActivities().size(); i++) {
      TestCaseActivity activity = ctx.getActivities().get(i);
      GeneratorStrategy strategy = activity.getStrategy();

      if (i != 0) {
        builder.addCode("\n");
      }

      builder.addCode("// $L: $L\n", activity.getTypeName(), activity.getId());

      if (activity.isAsyncBefore()) {
        strategy.applyHandlerBefore(builder);
      }

      strategy.applyHandler(builder);

      if (activity.isAsyncAfter()) {
        strategy.applyHandlerAfter(builder);
      }

      builder.addStatement("assertThat(pi).hasPassed($S)", getPassed(activity));
    }

    return builder.build();
  }

  protected String getPassed(TestCaseActivity activity) {
    if (activity.isMultiInstance()) {
      return String.format("%s#%s", activity.getId(), ActivityTypes.MULTI_INSTANCE_BODY);
    } else {
      return activity.getId();
    }
  }
}
