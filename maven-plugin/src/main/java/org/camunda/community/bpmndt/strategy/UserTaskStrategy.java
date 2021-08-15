package org.camunda.community.bpmndt.strategy;

import java.lang.reflect.Type;

import org.camunda.community.bpmndt.TestCaseActivity;
import org.camunda.community.bpmndt.api.UserTaskHandler;

import com.squareup.javapoet.MethodSpec;

public class UserTaskStrategy extends DefaultHandlerStrategy {

  @Override
  public Type getHandlerType() {
    return UserTaskHandler.class;
  }

  @Override
  public void initHandler(MethodSpec.Builder methodBuilder) {
    super.initHandler(methodBuilder);

    if (!activity.hasNext()) {
      return;
    }

    TestCaseActivity next = activity.getNext();

    if (!next.getType().isBoundaryEvent()) {
      return;
    }

    switch (next.getType()) {
      case ERROR_BOUNDARY:
        methodBuilder.addStatement("$L.handleBpmnError($S, null)", activity.getLiteral(), next.getEventCode());
        break;
      case ESCALATION_BOUNDARY:
        methodBuilder.addStatement("$L.handleEscalation($S)", activity.getLiteral(), next.getEventCode());
        break;
      default:
        methodBuilder.addStatement("$L.execute(task -> {})", activity.getLiteral());
        break;
    }
  }
}
