package org.camunda.community.bpmndt.strategy;

import java.lang.reflect.Type;

import org.camunda.community.bpmndt.api.EventHandler;

import com.squareup.javapoet.MethodSpec;

public class EventStrategy extends DefaultHandlerStrategy {

  @Override
  public Type getHandlerType() {
    return EventHandler.class;
  }

  @Override
  public void initHandler(MethodSpec.Builder methodBuilder) {
    methodBuilder.addCode("\n// $L: $L\n", activity.getTypeName(), activity.getId());
    Object[] args = {activity.getLiteral(), getHandlerType(), activity.getId(), activity.getEventName()};
    methodBuilder.addStatement("$L = new $T(getProcessEngine(), $S, $S)", args);
  }
}
