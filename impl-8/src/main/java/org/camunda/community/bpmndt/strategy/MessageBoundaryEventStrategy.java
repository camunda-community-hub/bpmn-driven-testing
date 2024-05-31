package org.camunda.community.bpmndt.strategy;

import org.camunda.community.bpmndt.model.BpmnElement;

import com.squareup.javapoet.MethodSpec;

public class MessageBoundaryEventStrategy extends MessageEventStrategy {

  public MessageBoundaryEventStrategy(BpmnElement element) {
    super(element);
  }

  @Override
  public void applyHandler(MethodSpec.Builder methodBuilder) {
    // nothing to do here
  }
}
