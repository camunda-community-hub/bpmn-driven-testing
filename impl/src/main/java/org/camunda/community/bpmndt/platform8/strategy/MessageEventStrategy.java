package org.camunda.community.bpmndt.platform8.strategy;

import org.camunda.community.bpmndt.model.platform8.BpmnElement;
import org.camunda.community.bpmndt.model.platform8.BpmnElementType;
import org.camunda.community.bpmndt.model.platform8.BpmnEventSupport;
import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceElement.MessageEventElement;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import io.camunda.zeebe.model.bpmn.instance.BoundaryEvent;
import io.camunda.zeebe.model.bpmn.instance.IntermediateCatchEvent;
import io.camunda.zeebe.model.bpmn.instance.Message;
import io.camunda.zeebe.model.bpmn.instance.MessageEventDefinition;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeSubscription;

public class MessageEventStrategy extends DefaultHandlerStrategy {

  public MessageEventStrategy(BpmnElement element) {
    super(element);
  }

  @Override
  public TypeName getHandlerType() {
    return MESSAGE_EVENT;
  }

  @Override
  public void initHandlerElement(MethodSpec.Builder methodBuilder) {
    methodBuilder.addCode("\n// $L: $L\n", element.getTypeName(), element.getId());
    methodBuilder.addStatement("$T $LElement = new $T()", MessageEventElement.class, literal, MessageEventElement.class);
    methodBuilder.addStatement("$LElement.setId($S)", literal, element.getId());

    MessageEventDefinition messageEventDefinition = null;
    if (element.getType() == BpmnElementType.MESSAGE_BOUNDARY) {
      BoundaryEvent event = element.getFlowNode(BoundaryEvent.class);
      BpmnEventSupport eventSupport = new BpmnEventSupport(event);

      messageEventDefinition = eventSupport.getMessageDefinition();
    } else if (element.getType() == BpmnElementType.MESSAGE_CATCH) {
      IntermediateCatchEvent event = element.getFlowNode(IntermediateCatchEvent.class);
      BpmnEventSupport eventSupport = new BpmnEventSupport(event);

      messageEventDefinition = eventSupport.getMessageDefinition();
    }

    if (messageEventDefinition != null) {
      Message message = messageEventDefinition.getMessage();

      ZeebeSubscription subscription = (ZeebeSubscription) message.getExtensionElements().getUniqueChildElementByType(ZeebeSubscription.class);
      if (subscription != null && subscription.getCorrelationKey() != null) {
        methodBuilder.addStatement("$LElement.setCorrelationKey($S)", literal, subscription.getCorrelationKey());
      }

      if (message != null && message.getName() != null) {
        methodBuilder.addStatement("$LElement.setMessageName($S)", literal, message.getName());
      }
    }
  }
}
