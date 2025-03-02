package org.camunda.community.bpmndt.strategy;

import org.camunda.community.bpmndt.api.CallActivityBindingType;
import org.camunda.community.bpmndt.api.TestCaseInstanceElement.CallActivityElement;
import org.camunda.community.bpmndt.model.BpmnElement;
import org.camunda.community.bpmndt.model.BpmnElementType;
import org.camunda.community.bpmndt.model.BpmnEventSupport;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import io.camunda.zeebe.model.bpmn.instance.BoundaryEvent;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeBindingType;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeCalledElement;

public class CallActivityStrategy extends DefaultHandlerStrategy {

  public CallActivityStrategy(BpmnElement element) {
    super(element);
  }

  @Override
  public TypeName getHandlerType() {
    return CALL_ACTIVITY;
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

    var event = next.getFlowNode(BoundaryEvent.class);
    var eventSupport = new BpmnEventSupport(event);

    if (next.getType() == BpmnElementType.ERROR_BOUNDARY) {
      var errorCode = eventSupport.getErrorCode();
      methodBuilder.addStatement("$L.withErrorCode($S)", literal, errorCode);
    } else if (next.getType() == BpmnElementType.ESCALATION_BOUNDARY) {
      var escalationCode = eventSupport.getEscalationCode();
      methodBuilder.addStatement("$L.withEscalationCode($S)", literal, escalationCode);
    } else {
      methodBuilder.addStatement("$L.waitForBoundaryEvent()", literal);
    }
  }

  @Override
  public void initHandlerElement(MethodSpec.Builder methodBuilder) {
    methodBuilder.addCode("\n// $L: $L\n", element.getTypeName(), element.getId());
    methodBuilder.addStatement("$T $LElement = new $T()", CallActivityElement.class, literal, CallActivityElement.class);
    methodBuilder.addStatement("$LElement.id = $S", literal, element.getId());

    var extensionElements = element.getFlowNode().getExtensionElements();
    if (extensionElements == null) {
      return;
    }

    var calledElement = (ZeebeCalledElement) extensionElements.getUniqueChildElementByType(ZeebeCalledElement.class);
    if (calledElement != null) {
      var bindingType = mapBindingType(calledElement.getBindingType());
      if (bindingType != null) {
        methodBuilder.addStatement("$LElement.bindingType = $T.$L", literal, CallActivityBindingType.class, bindingType.name());
      }

      if (calledElement.getProcessId() != null) {
        methodBuilder.addStatement("$LElement.processId = $S", literal, calledElement.getProcessId());
      }

      if (calledElement.getVersionTag() != null) {
        methodBuilder.addStatement("$LElement.versionTag = $S", literal, calledElement.getVersionTag());
      }

      methodBuilder.addStatement("$LElement.propagateAllChildVariables = $L", literal, calledElement.isPropagateAllChildVariablesEnabled());
      methodBuilder.addStatement("$LElement.propagateAllParentVariables = $L", literal, calledElement.isPropagateAllParentVariablesEnabled());
    }
  }

  private CallActivityBindingType mapBindingType(ZeebeBindingType source) {
    if (source == null) {
      return CallActivityBindingType.LATEST;
    }
    switch (source) {
      case deployment:
        return CallActivityBindingType.DEPLOYMENT;
      case latest:
        return CallActivityBindingType.LATEST;
      case versionTag:
        return CallActivityBindingType.VERSION_TAG;
      default:
        return null;
    }
  }
}
