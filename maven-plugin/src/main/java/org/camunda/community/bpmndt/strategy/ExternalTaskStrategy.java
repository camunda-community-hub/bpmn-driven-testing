package org.camunda.community.bpmndt.strategy;

import org.camunda.community.bpmndt.TestCaseActivity;
import org.camunda.community.bpmndt.TestCaseActivityType;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

public class ExternalTaskStrategy extends DefaultHandlerStrategy {

  @Override
  public TypeName getHandlerType() {
    return EXTERNAL_TASK;
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

    if (next.getType() == TestCaseActivityType.ERROR_BOUNDARY) {
      methodBuilder.addStatement("$L.handleBpmnError($S, null)", activity.getLiteral(), next.getEventCode());
    } else {
      methodBuilder.addStatement("$L.waitForBoundaryEvent()", activity.getLiteral());
    }
  }

  @Override
  public CodeBlock initHandlerStatement() {
    Object[] args = {getHandlerType(), activity.getId(), activity.getTopicName()};
    return CodeBlock.of("new $T(getProcessEngine(), $S, $S)", args);
  }
}
