package org.camunda.community.bpmndt.platform8.strategy;

import org.camunda.community.bpmndt.model.platform8.BpmnElement;
import org.camunda.community.bpmndt.model.platform8.BpmnElementType;
import org.camunda.community.bpmndt.model.platform8.BpmnEventSupport;
import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceElement.TimerEventElement;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import io.camunda.zeebe.model.bpmn.instance.BoundaryEvent;
import io.camunda.zeebe.model.bpmn.instance.IntermediateCatchEvent;
import io.camunda.zeebe.model.bpmn.instance.TimeDate;
import io.camunda.zeebe.model.bpmn.instance.TimeDuration;
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
    methodBuilder.addStatement("$LElement.setId($S)", literal, element.getId());

    TimerEventDefinition timerEventDefinition = null;
    if (element.getType() == BpmnElementType.TIMER_BOUNDARY) {
      BoundaryEvent event = element.getFlowNode(BoundaryEvent.class);
      BpmnEventSupport eventSupport = new BpmnEventSupport(event);

      timerEventDefinition = eventSupport.getTimerDefinition();
    } else if (element.getType() == BpmnElementType.TIMER_CATCH) {
      IntermediateCatchEvent event = element.getFlowNode(IntermediateCatchEvent.class);
      BpmnEventSupport eventSupport = new BpmnEventSupport(event);

      timerEventDefinition = eventSupport.getTimerDefinition();
    }

    if (timerEventDefinition != null) {
      TimeDate timeDate = timerEventDefinition.getTimeDate();
      if (timeDate != null && timeDate.getTextContent() != null) {
        methodBuilder.addStatement("$LElement.setTimeDate($S)", literal, timeDate.getTextContent());
      }

      TimeDuration timeDuration = timerEventDefinition.getTimeDuration();
      if (timeDuration != null && timeDuration.getTextContent() != null) {
        methodBuilder.addStatement("$LElement.setTimeDuration($S)", literal, timeDuration.getTextContent());
      }
    }
  }
}
