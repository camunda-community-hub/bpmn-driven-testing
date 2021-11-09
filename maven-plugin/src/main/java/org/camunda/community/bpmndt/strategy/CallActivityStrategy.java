package org.camunda.community.bpmndt.strategy;

import org.camunda.community.bpmndt.TestCaseActivity;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

public class CallActivityStrategy extends DefaultHandlerStrategy {

  @Override
  public TypeName getHandlerType() {
    return CALL_ACTIVITY;
  }

  @Override
  public void initHandler(MethodSpec.Builder methodBuilder) {
    methodBuilder.addCode("\n// $L: $L\n", activity.getTypeName(), activity.getId());
    methodBuilder.addCode("$L = ", activity.getLiteral());
    methodBuilder.addStatement(initHandlerStatement());

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

  @Override
  public CodeBlock initHandlerStatement() {
    return CodeBlock.of("new $T(instance, $S)", getHandlerType(), activity.getId());
  }
}
