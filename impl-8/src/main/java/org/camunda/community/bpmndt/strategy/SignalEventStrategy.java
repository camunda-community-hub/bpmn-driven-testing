package org.camunda.community.bpmndt.strategy;

import org.camunda.community.bpmndt.api.TestCaseInstanceElement.SignalEventElement;
import org.camunda.community.bpmndt.model.BpmnElement;
import org.camunda.community.bpmndt.model.BpmnElementType;
import org.camunda.community.bpmndt.model.BpmnEventSupport;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import io.camunda.zeebe.model.bpmn.instance.BoundaryEvent;
import io.camunda.zeebe.model.bpmn.instance.IntermediateCatchEvent;
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
    methodBuilder.addStatement("$LElement.id = $S", literal, element.getId());

    SignalEventDefinition signalEventDefinition = null;
    if (element.getType() == BpmnElementType.SIGNAL_BOUNDARY) {
      var event = element.getFlowNode(BoundaryEvent.class);
      var eventSupport = new BpmnEventSupport(event);

      signalEventDefinition = eventSupport.getSignalDefinition();
    } else if (element.getType() == BpmnElementType.SIGNAL_CATCH) {
      var event = element.getFlowNode(IntermediateCatchEvent.class);
      var eventSupport = new BpmnEventSupport(event);

      signalEventDefinition = eventSupport.getSignalDefinition();
    }

    if (signalEventDefinition != null) {
      var signal = signalEventDefinition.getSignal();
      if (signal != null && signal.getName() != null) {
        methodBuilder.addStatement("$LElement.signalName = $S", literal, signal.getName());
      }
    }
  }
}
