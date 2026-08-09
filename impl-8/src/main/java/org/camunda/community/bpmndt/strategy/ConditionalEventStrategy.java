package org.camunda.community.bpmndt.strategy;

import org.camunda.community.bpmndt.api.TestCaseInstanceElement.ConditionalEventElement;
import org.camunda.community.bpmndt.model.BpmnElement;
import org.camunda.community.bpmndt.model.BpmnElementType;
import org.camunda.community.bpmndt.model.BpmnEventSupport;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import io.camunda.zeebe.model.bpmn.instance.BoundaryEvent;
import io.camunda.zeebe.model.bpmn.instance.ConditionalEventDefinition;
import io.camunda.zeebe.model.bpmn.instance.IntermediateCatchEvent;

public class ConditionalEventStrategy extends DefaultHandlerStrategy {

  public ConditionalEventStrategy(BpmnElement element) {
    super(element);
  }

  @Override
  public TypeName getHandlerType() {
    return CONDITIONAL_EVENT;
  }

  @Override
  public void initHandlerElement(MethodSpec.Builder methodBuilder) {
    methodBuilder.addCode("\n// $L: $L\n", element.getTypeName(), element.getId());
    methodBuilder.addStatement("$T $LElement = new $T()", ConditionalEventElement.class, literal, ConditionalEventElement.class);
    methodBuilder.addStatement("$LElement.id = $S", literal, element.getId());

    ConditionalEventDefinition conditionalEventDefinition = null;
    if (element.getType() == BpmnElementType.CONDITIONAL_BOUNDARY) {
      var event = element.getFlowNode(BoundaryEvent.class);
      var eventSupport = new BpmnEventSupport(event);

      conditionalEventDefinition = eventSupport.getConditionalDefinition();

      methodBuilder.addStatement("$LElement.attachedTo = $S", literal, event.getAttachedTo().getId());
    } else if (element.getType() == BpmnElementType.CONDITIONAL_CATCH) {
      var event = element.getFlowNode(IntermediateCatchEvent.class);
      var eventSupport = new BpmnEventSupport(event);

      conditionalEventDefinition = eventSupport.getConditionalDefinition();

      if (element.hasPrevious(BpmnElementType.EVENT_BASED_GATEWAY)) {
        methodBuilder.addStatement("$LElement.attachedTo = $S", literal, element.getPrevious().getId());
      }
    }

    if (conditionalEventDefinition != null) {
      var condition = conditionalEventDefinition.getCondition();
      if (condition != null) {
        methodBuilder.addStatement("$LElement.condition = $S", literal, condition.getRawTextContent());
      }
    }
  }
}
