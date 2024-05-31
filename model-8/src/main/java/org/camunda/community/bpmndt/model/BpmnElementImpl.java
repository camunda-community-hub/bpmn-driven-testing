package org.camunda.community.bpmndt.model;

import java.util.Objects;

import io.camunda.zeebe.model.bpmn.instance.FlowNode;
import io.camunda.zeebe.model.bpmn.instance.MultiInstanceLoopCharacteristics;

class BpmnElementImpl implements BpmnElement {

  FlowNode flowNode;
  MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics;

  BpmnElementScope parent;
  BpmnElement prev;
  BpmnElement next;
  BpmnElementType type;

  String attachedTo;

  boolean processStart;

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof BpmnElement)) {
      return false;
    }

    var element = (BpmnElement) obj;
    return element.getId().equals(getId());
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
    return parent != null ? parent.getNestingLevel() : 0;
  }

  @Override
  public BpmnElement getNext() {
    if (!hasNext()) {
      throw new IllegalStateException("element has no successors");
    }
    return next;
  }

  @Override
  public BpmnElementScope getParent() {
    if (!hasParent()) {
      throw new IllegalStateException("element has no parent");
    }
    return parent;
  }

  @Override
  public BpmnElement getPrevious() {
    if (!hasPrevious()) {
      throw new IllegalStateException("element has no predecessor");
    }
    return prev;
  }

  @Override
  public BpmnElementType getType() {
    return type;
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
    return hasParent() && parent.isMultiInstance();
  }

  @Override
  public boolean hasNext() {
    return next != null;
  }

  @Override
  public boolean hasParent() {
    return parent != null;
  }

  @Override
  public boolean hasPrevious() {
    return prev != null;
  }

  @Override
  public boolean hasPrevious(BpmnElementType type) {
    return hasPrevious() && getPrevious().getType() == type;
  }

  @Override
  public boolean isAttachedTo(BpmnElement element) {
    return element.getId().equals(attachedTo);
  }

  @Override
  public boolean isMultiInstance() {
    return multiInstanceLoopCharacteristics != null;
  }

  @Override
  public boolean isMultiInstanceSequential() {
    if (!isMultiInstance()) {
      throw new IllegalStateException("element is not a multi instance");
    }
    return multiInstanceLoopCharacteristics.isSequential();
  }

  @Override
  public boolean isProcessStart() {
    return processStart;
  }
}
