package org.camunda.community.bpmndt.strategy;

import org.camunda.community.bpmndt.api.TestCaseInstanceElement.TimerEventElement;
import org.camunda.community.bpmndt.model.BpmnElement;
import org.camunda.community.bpmndt.model.BpmnElementType;
import org.camunda.community.bpmndt.model.BpmnEventSupport;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import io.camunda.zeebe.model.bpmn.instance.BoundaryEvent;
import io.camunda.zeebe.model.bpmn.instance.IntermediateCatchEvent;
import io.camunda.zeebe.model.bpmn.instance.TimerEventDefinition;

public class TimerEventStrategy extends DefaultHandlerStrategy {

  public TimerEventStrategy(BpmnElement element) {
    super(element);
  }

  @Override
  public TypeName getHandlerType() {
    return TIMER_EVENT;
  }

  @Override
  public void initHandlerElement(MethodSpec.Builder methodBuilder) {
    methodBuilder.addCode("\n// $L: $L\n", element.getTypeName(), element.getId());
    methodBuilder.addStatement("$T $LElement = new $T()", TimerEventElement.class, literal, TimerEventElement.class);
    methodBuilder.addStatement("$LElement.id = $S", literal, element.getId());

    TimerEventDefinition timerEventDefinition = null;
    if (element.getType() == BpmnElementType.TIMER_BOUNDARY) {
      var event = element.getFlowNode(BoundaryEvent.class);
      var eventSupport = new BpmnEventSupport(event);

      timerEventDefinition = eventSupport.getTimerDefinition();
    } else if (element.getType() == BpmnElementType.TIMER_CATCH) {
      var event = element.getFlowNode(IntermediateCatchEvent.class);
      var eventSupport = new BpmnEventSupport(event);

      timerEventDefinition = eventSupport.getTimerDefinition();
    }

    if (timerEventDefinition != null) {
      var timeDate = timerEventDefinition.getTimeDate();
      if (timeDate != null && timeDate.getTextContent() != null) {
        methodBuilder.addStatement("$LElement.timeDate = $S", literal, timeDate.getTextContent());
      }

      var timeDuration = timerEventDefinition.getTimeDuration();
      if (timeDuration != null && timeDuration.getTextContent() != null) {
        methodBuilder.addStatement("$LElement.timeDuration = $S", literal, timeDuration.getTextContent());
      }
    }
  }
}
