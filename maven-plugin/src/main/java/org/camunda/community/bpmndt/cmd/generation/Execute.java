package org.camunda.community.bpmndt.cmd.generation;

import java.util.function.Function;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.runtime.ProcessInstance;
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

    for (TestCaseActivity activity : ctx.getActivities()) {
      builder.addCode("\n// $L: $L\n", activity.getTypeName(), activity.getId());

      if (activity.isAsyncBefore()) {
        builder.addStatement("assertThat(pi).isWaitingAt($S)", activity.getId());
        builder.addStatement("instance.apply($L)", activity.getLiteralBefore());
      }

      switch (activity.getType()) {
        case CALL_ACTIVITY:
          // nothing to do here, since a CallActivity has no waite state
          break;
        case EXTERNAL_TASK:
        case MESSAGE_CATCH_EVENT:
        case SIGNAL_CATCH_EVENT:
        case TIMER_CATCH_EVENT:
        case USER_TASK:
          builder.addStatement("assertThat(pi).isWaitingAt($S)", activity.getId());
          builder.addStatement("instance.apply($L)", activity.getLiteral());
          break;
        default:
          // other activities are not handled
          break;
      }

      if (activity.isAsyncAfter()) {
        builder.addStatement("assertThat(pi).isWaitingAt($S)", activity.getId());
        builder.addStatement("instance.apply($L)", activity.getLiteralAfter());
      }

      builder.addStatement("assertThat(pi).hasPassed($S)", activity.getId());
    }

    return builder.build();
  }
}
