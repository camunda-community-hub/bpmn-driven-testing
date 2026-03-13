package org.camunda.community.bpmndt.strategy;

import org.camunda.community.bpmndt.api.TestCaseInstanceElement.MessageEventElement;
import org.camunda.community.bpmndt.model.BpmnElement;
import org.camunda.community.bpmndt.model.BpmnElementType;
import org.camunda.community.bpmndt.model.BpmnEventSupport;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import io.camunda.zeebe.model.bpmn.instance.BoundaryEvent;
import io.camunda.zeebe.model.bpmn.instance.IntermediateCatchEvent;
import io.camunda.zeebe.model.bpmn.instance.Message;
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
    methodBuilder.addStatement("$LElement.id = $S", literal, element.getId());

    Message message = null;
    if (element.getType() == BpmnElementType.MESSAGE_BOUNDARY) {
      var event = element.getFlowNode(BoundaryEvent.class);
      var eventSupport = new BpmnEventSupport(event);

      var messageEventDefinition = eventSupport.getMessageDefinition();
      if (messageEventDefinition != null) {
        message = messageEventDefinition.getMessage();
      }

      methodBuilder.addStatement("$LElement.attachedTo = $S", literal, event.getAttachedTo().getId());
    } else if (element.getType() == BpmnElementType.MESSAGE_CATCH) {
      var event = element.getFlowNode(IntermediateCatchEvent.class);
      var eventSupport = new BpmnEventSupport(event);

      var messageEventDefinition = eventSupport.getMessageDefinition();
      if (messageEventDefinition != null) {
        message = messageEventDefinition.getMessage();
      }

      if (element.hasPrevious(BpmnElementType.EVENT_BASED_GATEWAY)) {
        methodBuilder.addStatement("$LElement.attachedTo = $S", literal, element.getPrevious().getId());
      }
    }

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
