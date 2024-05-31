package org.camunda.community.bpmndt.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import io.camunda.zeebe.model.bpmn.instance.FlowNode;
import io.camunda.zeebe.model.bpmn.instance.MultiInstanceLoopCharacteristics;
import io.camunda.zeebe.model.bpmn.instance.SubProcess;

class BpmnElementScopeImpl implements BpmnElementScope {

  FlowNode flowNode;
  MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics;

  BpmnElementScope parent;

  private final List<BpmnElement> elements = new LinkedList<>();

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof BpmnElementScope)) {
      return false;
    }

    var scope = (BpmnElementScope) obj;
    return scope.getId().equals(getId());
  }

  @Override
  public List<BpmnElement> getElements() {
    return elements;
  }

  @Override
  public FlowNode getFlowNode() {
    return flowNode;
  }

  @Override
  public <T extends SubProcess> T getFlowNode(Class<T> subProcessType) {
    return subProcessType.cast(flowNode);
  }

  @Override
  public String getId() {
    return flowNode.getId();
  }

  @Override
  public String getName() {
    return flowNode.getName();
  }

  @Override
  public int getNestingLevel() {
    return parent != null ? parent.getNestingLevel() + 1 : 1;
  }

  @Override
  public BpmnElementScope getParent() {
    return parent;
  }

  @Override
  public String getTypeName() {
    return flowNode.getElementType().getTypeName();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }

  @Override
  public boolean hasParent() {
    return parent != null;
  }

  @Override
  public boolean isMultiInstance() {
    return multiInstanceLoopCharacteristics != null;
  }

  @Override
  public boolean isMultiInstanceSequential() {
    if (!isMultiInstance()) {
      throw new IllegalStateException("scope is not a multi instance");
    }
    return multiInstanceLoopCharacteristics.isSequential();
  }

  void addElement(BpmnElementImpl next) {
    elements.add(next);
    next.parent = this;
  }
}
