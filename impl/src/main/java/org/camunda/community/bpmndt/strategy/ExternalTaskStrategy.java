package org.camunda.community.bpmndt.strategy;

import org.camunda.community.bpmndt.model.TestCaseActivity;
import org.camunda.community.bpmndt.model.TestCaseActivityType;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

public class ExternalTaskStrategy extends DefaultHandlerStrategy {

  public ExternalTaskStrategy(TestCaseActivity activity) {
    super(activity);
  }

  @Override
  public TypeName getHandlerType() {
    return EXTERNAL_TASK;
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

    if (next.getType() == TestCaseActivityType.ERROR_BOUNDARY) {
      methodBuilder.addStatement("$L.handleBpmnError($S, null)", literal, next.getEventCode());
    } else {
      methodBuilder.addStatement("$L.waitForBoundaryEvent()", literal);
    }
  }

  @Override
  public CodeBlock initHandlerStatement(boolean isTestCase) {
    return CodeBlock.of("new ExternalTaskHandler<>(getProcessEngine(), $S, $S)", activity.getId(), activity.getTopicName());
  }
}
