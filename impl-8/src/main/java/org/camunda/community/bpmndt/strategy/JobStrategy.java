package org.camunda.community.bpmndt.strategy;

import org.camunda.community.bpmndt.api.TestCaseInstanceElement.JobElement;
import org.camunda.community.bpmndt.model.BpmnElement;
import org.camunda.community.bpmndt.model.BpmnElementType;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeTaskDefinition;

public class JobStrategy extends DefaultHandlerStrategy {

  public JobStrategy(BpmnElement element) {
    super(element);
  }

  @Override
  public TypeName getHandlerType() {
    return JOB;
  }

  @Override
  public void initHandler(MethodSpec.Builder methodBuilder) {
    super.initHandler(methodBuilder);

    if (!element.hasNext()) {
      return;
    }

    var next = element.getNext();
    if (!next.getType().isBoundaryEvent()) {
      return;
    }

    if (next.getType() != BpmnElementType.ERROR_BOUNDARY && next.getType() != BpmnElementType.ESCALATION_BOUNDARY) {
      methodBuilder.addStatement("$L.waitForBoundaryEvent()", literal);
    }
  }

  @Override
  public void initHandlerElement(MethodSpec.Builder methodBuilder) {
    methodBuilder.addCode("\n// $L: $L\n", element.getTypeName(), element.getId());
    methodBuilder.addStatement("$T $LElement = new $T()", JobElement.class, literal, JobElement.class);
    methodBuilder.addStatement("$LElement.id = $S", literal, element.getId());

    var extensionElements = element.getFlowNode().getExtensionElements();
    if (extensionElements == null) {
      return;
    }

    var taskDefinition = (ZeebeTaskDefinition) extensionElements.getUniqueChildElementByType(ZeebeTaskDefinition.class);
    if (taskDefinition != null) {
      if (taskDefinition.getRetries() != null) {
        methodBuilder.addStatement("$LElement.retries = $S", literal, taskDefinition.getRetries());
      }
      if (taskDefinition.getType() != null) {
        methodBuilder.addStatement("$LElement.type = $S", literal, taskDefinition.getType());
      }
    }
  }
}
