package org.camunda.community.bpmndt.strategy;

import org.camunda.community.bpmndt.model.TestCaseActivity;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

public class CallActivityStrategy extends DefaultHandlerStrategy {

  public CallActivityStrategy(TestCaseActivity activity) {
    super(activity);
  }

  @Override
  public TypeName getHandlerType() {
    return CALL_ACTIVITY;
  }

  @Override
  public void initHandler(MethodSpec.Builder methodBuilder) {
    methodBuilder.addCode("\n// $L: $L\n", activity.getTypeName(), activity.getId());
    methodBuilder.addCode("$L = ", literal);
    methodBuilder.addStatement(initHandlerStatement(true));

    if (!activity.hasNext()) {
      return;
    }

    TestCaseActivity next = activity.getNext();
    if (!next.getType().isBoundaryEvent()) {
      return;
    }

    switch (next.getType()) {
      case ERROR_BOUNDARY:
        methodBuilder.addStatement("$L.simulateBpmnError($S, null)", literal, next.getEventCode());
        break;
      case ESCALATION_BOUNDARY:
        methodBuilder.addStatement("$L.simulateEscalation($S)", literal, next.getEventCode());
        break;
      default:
        methodBuilder.addStatement("$L.waitForBoundaryEvent()", literal);
        break;
    }
  }

  @Override
  public CodeBlock initHandlerStatement(boolean isTestCase) {
    if (isTestCase) {
      return CodeBlock.of("new $T(this, $S)", getHandlerType(), activity.getId());
    } else {
      return CodeBlock.of("new $T(testCase, $S)", getHandlerType(), activity.getId());
    }
  }
}
