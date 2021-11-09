package org.camunda.community.bpmndt.strategy;

import com.squareup.javapoet.TypeName;

public class JobStrategy extends DefaultHandlerStrategy {

  @Override
  public TypeName getHandlerType() {
    return JOB;
  }
}
