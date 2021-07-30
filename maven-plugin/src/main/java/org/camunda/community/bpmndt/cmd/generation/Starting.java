package org.camunda.community.bpmndt.cmd.generation;

import java.util.function.Function;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.community.bpmndt.TestCaseActivity;
import org.camunda.community.bpmndt.TestCaseContext;
import org.camunda.community.bpmndt.api.CallActivityHandler;
import org.camunda.community.bpmndt.api.ExternalTaskHandler;
import org.camunda.community.bpmndt.api.IntermediateCatchEventHandler;
import org.camunda.community.bpmndt.api.JobHandler;
import org.camunda.community.bpmndt.api.UserTaskHandler;
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
      String id = activity.getId();
      String literal = activity.getLiteral();
      String typeName = activity.getTypeName();

      if (activity.isAsyncBefore()) {
        builder.addCode("\n// $L: $L\n", typeName, id);
        builder.addStatement("$L = new $T(getProcessEngine(), $S)", activity.getLiteralBefore(), JobHandler.class, id);
      }

      switch (activity.getType()) {
        case CALL_ACTIVITY:
          builder.addCode("\n// $L: $L\n", typeName, id);
          builder.addStatement("$L = new $T(instance, $S)", literal, CallActivityHandler.class, id);
          break;
        case EXTERNAL_TASK:
          ServiceTask serviceTask = activity.as(ServiceTask.class);
          String topicName = serviceTask.getCamundaTopic();

          builder.addCode("\n// $L: $L\n", typeName, id);
          builder.addStatement("$L = new $T(getProcessEngine(), $S)", literal, ExternalTaskHandler.class, topicName);
          break;
        case MESSAGE_CATCH_EVENT:
        case SIGNAL_CATCH_EVENT:
          String eventName = activity.getEventName();

          builder.addCode("\n// $L: $L\n", typeName, id);
          builder.addStatement("$L = new $T(getProcessEngine(), $S, $S)", literal, IntermediateCatchEventHandler.class, id, eventName);
          break;
        case TIMER_CATCH_EVENT:
          builder.addCode("\n// $L: $L\n", typeName, id);
          builder.addStatement("$L = new $T(getProcessEngine(), $S)", literal, JobHandler.class, id);
          break;
        case USER_TASK:
          builder.addCode("\n// $L: $L\n", typeName, id);
          builder.addStatement("$L = new $T(getProcessEngine(), $S)", literal, UserTaskHandler.class, id);
          break;
        default:
          // other activities are not handled
          break;
      }

      if (activity.isAsyncAfter()) {
        builder.addCode("\n// $L: $L\n", typeName, id);
        builder.addStatement("$L = new $T(getProcessEngine(), $S)", activity.getLiteralAfter(), JobHandler.class, id);
      }
    }

    return builder.build();
  }
}
