package org.camunda.community.bpmndt.strategy;

import org.camunda.community.bpmndt.model.TestCaseActivity;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

public class EventStrategy extends DefaultHandlerStrategy {

  public EventStrategy(TestCaseActivity activity) {
    super(activity);
  }

  @Override
  public TypeName getHandlerType() {
    return EVENT;
  }

  @Override
  public void initHandler(MethodSpec.Builder methodBuilder) {
    methodBuilder.addCode("\n// $L: $L\n", activity.getTypeName(), activity.getId());
    methodBuilder.addCode("$L = ", literal);
    methodBuilder.addStatement(initHandlerStatement());
  }

  @Override
  public CodeBlock initHandlerStatement() {
    Object[] args = {getHandlerType(), activity.getId(), activity.getEventName()};
    return CodeBlock.of("new $T(getProcessEngine(), $S, $S)", args);
  }
}
