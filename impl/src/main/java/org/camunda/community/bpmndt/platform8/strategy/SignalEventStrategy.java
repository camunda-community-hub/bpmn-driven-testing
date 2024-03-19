package org.camunda.community.bpmndt.platform8.strategy;

import org.camunda.community.bpmndt.model.platform8.BpmnElement;
import org.camunda.community.bpmndt.model.platform8.BpmnElementType;
import org.camunda.community.bpmndt.model.platform8.BpmnEventSupport;
import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceElement.SignalEventElement;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import io.camunda.zeebe.model.bpmn.instance.BoundaryEvent;
import io.camunda.zeebe.model.bpmn.instance.IntermediateCatchEvent;
import io.camunda.zeebe.model.bpmn.instance.Signal;
import io.camunda.zeebe.model.bpmn.instance.SignalEventDefinition;

public class SignalEventStrategy extends DefaultHandlerStrategy {

  public SignalEventStrategy(BpmnElement element) {
    super(element);
  }

  @Override
  public TypeName getHandlerType() {
    return SIGNAL_EVENT;
  }

  @Override
  public void initHandlerElement(MethodSpec.Builder methodBuilder) {
    methodBuilder.addCode("\n// $L: $L\n", element.getTypeName(), element.getId());
    methodBuilder.addStatement("$T $LElement = new $T()", SignalEventElement.class, literal, SignalEventElement.class);
    methodBuilder.addStatement("$LElement.setId($S)", literal, element.getId());

    SignalEventDefinition signalEventDefinition = null;
    if (element.getType() == BpmnElementType.SIGNAL_BOUNDARY) {
      BoundaryEvent event = element.getFlowNode(BoundaryEvent.class);
      BpmnEventSupport eventSupport = new BpmnEventSupport(event);

      signalEventDefinition = eventSupport.getSignalDefinition();
    } else if (element.getType() == BpmnElementType.SIGNAL_CATCH) {
      IntermediateCatchEvent event = element.getFlowNode(IntermediateCatchEvent.class);
      BpmnEventSupport eventSupport = new BpmnEventSupport(event);

      signalEventDefinition = eventSupport.getSignalDefinition();
    }

    if (signalEventDefinition != null) {
      Signal signal = signalEventDefinition.getSignal();

      if (signal != null && signal.getName() != null) {
        methodBuilder.addStatement("$LElement.setSignalName($S)", literal, signal.getName());
      }
    }
  }
}
