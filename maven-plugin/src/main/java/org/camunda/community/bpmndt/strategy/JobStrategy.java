package org.camunda.community.bpmndt.strategy;

import java.lang.reflect.Type;

import org.camunda.community.bpmndt.api.JobHandler;

public class JobStrategy extends DefaultHandlerStrategy {

  @Override
  public Type getHandlerType() {
    return JobHandler.class;
  }
}
