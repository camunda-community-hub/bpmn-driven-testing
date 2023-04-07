package org.camunda.community.bpmndt.strategy;

import org.camunda.community.bpmndt.model.TestCaseActivity;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

public class UserTaskStrategy extends DefaultHandlerStrategy {

  public UserTaskStrategy(TestCaseActivity activity) {
    super(activity);
  }

  @Override
  public TypeName getHandlerType() {
    return USER_TASK;
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
        methodBuilder.addStatement("$L.handleBpmnError($S, null)", literal, next.getEventCode());
        break;
      case ESCALATION_BOUNDARY:
        methodBuilder.addStatement("$L.handleEscalation($S)", literal, next.getEventCode());
        break;
      default:
        methodBuilder.addStatement("$L.waitForBoundaryEvent()", literal);
        break;
    }
  }
}
