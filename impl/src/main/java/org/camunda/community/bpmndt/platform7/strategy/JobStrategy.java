package org.camunda.community.bpmndt.platform7.strategy;

import org.camunda.community.bpmndt.model.platform7.TestCaseActivity;

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
