package org.camunda.community.bpmndt.strategy;

import java.util.HashMap;

import org.camunda.community.bpmndt.api.TestCaseInstanceElement.OutboundConnectorElement;
import org.camunda.community.bpmndt.model.BpmnElement;
import org.camunda.community.bpmndt.model.BpmnElementType;
import org.camunda.community.bpmndt.model.BpmnEventSupport;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import io.camunda.zeebe.model.bpmn.instance.BoundaryEvent;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeHeader;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeInput;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeIoMapping;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeOutput;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeTaskDefinition;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeTaskHeaders;

public class OutboundConnectorStrategy extends DefaultHandlerStrategy {

  public OutboundConnectorStrategy(BpmnElement element) {
    super(element);
  }

  @Override
  public TypeName getHandlerType() {
    return OUTBOUND_CONNECTOR;
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

    if (next.getType() == BpmnElementType.ERROR_BOUNDARY) {
      var event = next.getFlowNode(BoundaryEvent.class);
      var eventSupport = new BpmnEventSupport(event);

      var errorCode = eventSupport.getErrorCode();
      methodBuilder.addStatement("$L.throwBpmnError($S, $S)", literal, errorCode, "error message, generated by BPMNDT");
    }
  }

  @Override
  public void initHandlerElement(MethodSpec.Builder methodBuilder) {
    methodBuilder.addCode("\n// $L: $L\n", element.getTypeName(), element.getId());
    methodBuilder.addStatement("$T $LElement = new $T()", OutboundConnectorElement.class, literal, OutboundConnectorElement.class);
    methodBuilder.addStatement("$LElement.id = $S", literal, element.getId());

    var extensionElements = element.getFlowNode().getExtensionElements();
    if (extensionElements == null) {
      return;
    }

    var taskDefinition = (ZeebeTaskDefinition) extensionElements.getUniqueChildElementByType(ZeebeTaskDefinition.class);
    if (taskDefinition != null) {
      if (taskDefinition.getType() != null) {
        methodBuilder.addStatement("$LElement.taskDefinitionType = $S", literal, taskDefinition.getType());
      }
    }

    var ioMapping = (ZeebeIoMapping) extensionElements.getUniqueChildElementByType(ZeebeIoMapping.class);
    if (ioMapping != null) {
      var inputs = ioMapping.getInputs();
      if (inputs != null && !inputs.isEmpty()) {
        methodBuilder.addStatement("$LElement.inputs = new $T()", literal, ParameterizedTypeName.get(HashMap.class, String.class, String.class));

        for (ZeebeInput input : inputs) {
          methodBuilder.addStatement("$LElement.inputs.put($S, $S)", literal, input.getTarget(), input.getSource());
        }
      }

      var outputs = ioMapping.getOutputs();
      if (outputs != null && !outputs.isEmpty()) {
        methodBuilder.addStatement("$LElement.outputs = new $T()", literal, ParameterizedTypeName.get(HashMap.class, String.class, String.class));

        for (ZeebeOutput output : outputs) {
          methodBuilder.addStatement("$LElement.outputs.put($S, $S)", literal, output.getTarget(), output.getSource());
        }
      }
    }

    var taskHeaders = (ZeebeTaskHeaders) extensionElements.getUniqueChildElementByType(ZeebeTaskHeaders.class);
    if (taskHeaders != null) {
      var headers = taskHeaders.getHeaders();
      if (headers != null && !headers.isEmpty()) {
        methodBuilder.addStatement("$LElement.taskHeaders = new $T()", literal, ParameterizedTypeName.get(HashMap.class, String.class, String.class));

        for (ZeebeHeader header : headers) {
          methodBuilder.addStatement("$LElement.taskHeaders.put($S, $S)", literal, header.getKey(), header.getValue());
        }
      }
    }
  }
}
