package org.camunda.community.bpmndt.strategy;

import org.camunda.community.bpmndt.model.BpmnElement;

import com.squareup.javapoet.MethodSpec;

public class SignalBoundaryEventStrategy extends SignalEventStrategy {

  public SignalBoundaryEventStrategy(BpmnElement element) {
    super(element);
  }

  @Override
  public void applyHandler(MethodSpec.Builder methodBuilder) {
    // nothing to do here
  }
}
