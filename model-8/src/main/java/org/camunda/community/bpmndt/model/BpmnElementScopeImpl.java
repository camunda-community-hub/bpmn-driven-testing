package org.camunda.community.bpmndt.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import io.camunda.zeebe.model.bpmn.instance.FlowNode;
import io.camunda.zeebe.model.bpmn.instance.MultiInstanceLoopCharacteristics;

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
  public <T extends FlowNode> T getFlowNode(Class<T> flowNodeType) {
    return flowNodeType.cast(flowNode);
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
  public BpmnElement getNext() {
    throw new UnsupportedOperationException();
  }

  @Override
  public BpmnElementScope getParent() {
    return parent;
  }

  @Override
  public BpmnElement getPrevious() {
    throw new UnsupportedOperationException();
  }

  @Override
  public BpmnElementType getType() {
    return BpmnElementType.SCOPE;
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
  public boolean hasMultiInstanceParent() {
    return hasParent() && getParent().isMultiInstance();
  }

  @Override
  public boolean hasNext() {
    return false;
  }

  @Override
  public boolean hasParent() {
    return parent != null;
  }

  @Override
  public boolean hasPrevious() {
    return false;
  }

  @Override
  public boolean hasPrevious(BpmnElementType type) {
    return false;
  }

  @Override
  public boolean isAttachedTo(BpmnElement element) {
    return false;
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

  @Override
  public boolean isProcessStart() {
    return false;
  }

  void addElement(BpmnElementImpl next) {
    elements.add(next);
    next.parent = this;
  }
}
