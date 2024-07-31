package org.camunda.community.bpmndt.strategy;

import org.camunda.community.bpmndt.api.TestCaseInstanceElement.MessageEventElement;
import org.camunda.community.bpmndt.model.BpmnElement;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import io.camunda.zeebe.model.bpmn.instance.ReceiveTask;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeSubscription;

public class ReceiveTaskStrategy extends DefaultHandlerStrategy {

  public ReceiveTaskStrategy(BpmnElement element) {
    super(element);
  }

  @Override
  public TypeName getHandlerType() {
    return RECEIVE_TASK;
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

    methodBuilder.addStatement("$L.waitForBoundaryEvent()", literal);
  }

  @Override
  public void initHandlerElement(MethodSpec.Builder methodBuilder) {
    methodBuilder.addCode("\n// $L: $L\n", element.getTypeName(), element.getId());
    methodBuilder.addStatement("$T $LElement = new $T()", MessageEventElement.class, literal, MessageEventElement.class);
    methodBuilder.addStatement("$LElement.id = $S", literal, element.getId());

    var message = element.getFlowNode(ReceiveTask.class).getMessage();

    if (message != null) {
      var extensionElements = message.getExtensionElements();
      if (extensionElements != null) {
        var subscription = (ZeebeSubscription) extensionElements.getUniqueChildElementByType(ZeebeSubscription.class);
        if (subscription != null && subscription.getCorrelationKey() != null) {
          methodBuilder.addStatement("$LElement.correlationKey = $S", literal, subscription.getCorrelationKey());
        }
      }

      if (message.getName() != null) {
        methodBuilder.addStatement("$LElement.messageName = $S", literal, message.getName());
      }
    }
  }
}
