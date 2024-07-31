package org.camunda.community.bpmndt.strategy;

import org.camunda.community.bpmndt.model.TestCaseActivity;

import com.squareup.javapoet.CodeBlock;
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
  public CodeBlock initHandlerStatement() {
    Object[] args = {getHandlerType(), activity.getId(), activity.getEventName()};
    return CodeBlock.of("new $T(getProcessEngine(), $S, $S)", args);
  }
}
