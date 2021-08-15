package org.camunda.community.bpmndt.strategy;

import java.lang.reflect.Type;

import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.community.bpmndt.TestCaseActivity;
import org.camunda.community.bpmndt.TestCaseActivityType;
import org.camunda.community.bpmndt.api.ExternalTaskHandler;

import com.squareup.javapoet.MethodSpec;

public class ExternalTaskStrategy extends DefaultHandlerStrategy {

  @Override
  public Type getHandlerType() {
    return ExternalTaskHandler.class;
  }

  @Override
  public void initHandler(MethodSpec.Builder methodBuilder) {
    Object[] args;

    ServiceTask serviceTask = activity.as(ServiceTask.class);

    methodBuilder.addCode("\n// $L: $L\n", activity.getTypeName(), activity.getId());
    args = new Object[] {activity.getLiteral(), getHandlerType(), activity.getId(), serviceTask.getCamundaTopic()};
    methodBuilder.addStatement("$L = new $T(getProcessEngine(), $S, $S)", args);

    if (!activity.hasNext()) {
      return;
    }
    
    TestCaseActivity next = activity.getNext();

    if (!next.getType().isBoundaryEvent()) {
      return;
    }

    if (next.getType() == TestCaseActivityType.ERROR_BOUNDARY) {
      methodBuilder.addStatement("$L.handleBpmnError($S, null)", activity.getLiteral(), next.getEventCode());
    } else {
      methodBuilder.addStatement("$L.execute(topicName -> {})", activity.getLiteral());
    }
  }
}
