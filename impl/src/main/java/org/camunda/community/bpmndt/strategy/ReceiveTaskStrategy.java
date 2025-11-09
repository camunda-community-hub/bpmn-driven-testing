package org.camunda.community.bpmndt.strategy;

import org.camunda.community.bpmndt.model.TestCaseActivity;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

public class ReceiveTaskStrategy extends DefaultHandlerStrategy {

  public ReceiveTaskStrategy(TestCaseActivity activity) {
    super(activity);
  }

  @Override
  public TypeName getHandlerType() {
    return RECEIVE_TASK;
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

    methodBuilder.addStatement("$L.waitForBoundaryEvent()", literal);
  }

  @Override
  public CodeBlock initHandlerStatement(boolean isTestCase) {
    Object[] args = {getHandlerType(), activity.getId(), activity.getEventName()};
    return CodeBlock.of("new $T(getProcessEngine(), $S, $S)", args);
  }
}
