package org.camunda.community.bpmndt.strategy;

import java.lang.reflect.Type;

import org.camunda.community.bpmndt.TestCaseActivity;
import org.camunda.community.bpmndt.api.CallActivityHandler;

import com.squareup.javapoet.MethodSpec;

public class CallActivityStrategy extends DefaultHandlerStrategy {

  @Override
  public Type getHandlerType() {
    return CallActivityHandler.class;
  }

  @Override
  public void initHandler(MethodSpec.Builder methodBuilder) {
    methodBuilder.addCode("\n// $L: $L\n", activity.getTypeName(), activity.getId());
    methodBuilder.addStatement("$L = new $T(instance, $S)", activity.getLiteral(), getHandlerType(), activity.getId());

    if (!activity.hasNext()) {
      return;
    }

    TestCaseActivity next = activity.getNext();

    if (!next.getType().isBoundaryEvent()) {
      return;
    }

    switch (next.getType()) {
      case ERROR_BOUNDARY:
        methodBuilder.addStatement("$L.simulateBpmnError($S, null)", activity.getLiteral(), next.getEventCode());
        break;
      case ESCALATION_BOUNDARY:
        methodBuilder.addStatement("$L.simulateEscalation($S)", activity.getLiteral(), next.getEventCode());
        break;
      default:
        methodBuilder.addStatement("$L.waitForBoundaryEvent()", activity.getLiteral());
        break;
    }
  }
}
