package org.camunda.community.bpmndt.strategy;

import org.camunda.community.bpmndt.model.TestCaseActivity;

import com.squareup.javapoet.TypeName;

public class JobStrategy extends DefaultHandlerStrategy {

  public JobStrategy(TestCaseActivity activity) {
    super(activity);
  }

  @Override
  public TypeName getHandlerType() {
    return JOB;
  }
}
