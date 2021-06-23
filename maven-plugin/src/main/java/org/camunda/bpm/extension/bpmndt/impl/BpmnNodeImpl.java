package org.camunda.bpm.extension.bpmndt.impl;

import java.util.Optional;

import org.camunda.bpm.extension.bpmndt.BpmnNode;
import org.camunda.bpm.extension.bpmndt.BpmnSupport;
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants;
import org.camunda.bpm.model.bpmn.instance.EventDefinition;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.TimerEventDefinition;

class BpmnNodeImpl implements BpmnNode {

  private final FlowNode flowNode;

  BpmnNodeImpl(FlowNode flowNode) {
    this.flowNode = flowNode;
  }

  @Override
  public <T extends FlowNode> T as(Class<T> type) {
    return type.cast(flowNode);
  }

  @Override
  public String getId() {
    return flowNode.getId();
  }

  @Override
  public String getLiteral() {
    return BpmnSupport.convert(flowNode.getId());
  }

  @Override
  public String getType() {
    return flowNode.getElementType().getTypeName();
  }

  protected boolean is(String typeName) {
    return flowNode.getElementType().getTypeName().equals(typeName);
  }

  @Override
  public boolean isAsyncAfter() {
    return flowNode.isCamundaAsyncAfter();
  }

  @Override
  public boolean isAsyncBefore() {
    return flowNode.isCamundaAsyncBefore();
  }

  @Override
  public boolean isCallActivity() {
    return is(BpmnModelConstants.BPMN_ELEMENT_CALL_ACTIVITY);
  }

  @Override
  public boolean isExternalTask() {
    if (is(BpmnModelConstants.BPMN_ELEMENT_SERVICE_TASK)) {
      ServiceTask serviceTask = (ServiceTask) flowNode;
      return "external".equals(serviceTask.getCamundaType());
    } else {
      return false;
    }
  }

  @Override
  public boolean isIntermediateCatchEvent() {
    return is(BpmnModelConstants.BPMN_ELEMENT_INTERMEDIATE_CATCH_EVENT);
  }

  @Override
  public boolean isJob() {
    if (flowNode.isCamundaAsyncBefore() || flowNode.isCamundaAsyncAfter()) {
      return true;
    }

    if (!isIntermediateCatchEvent()) {
      return false;
    }

    IntermediateCatchEvent event = (IntermediateCatchEvent) flowNode;

    Optional<EventDefinition> eventDefinition = event.getEventDefinitions().stream().findFirst();
    if (eventDefinition.isPresent() && eventDefinition.get() instanceof TimerEventDefinition) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean isUserTask() {
    return is(BpmnModelConstants.BPMN_ELEMENT_USER_TASK);
  }
}
