package org.camunda.community.bpmndt.model;

import java.util.Objects;

import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.MultiInstanceLoopCharacteristics;

class TestCaseActivityImpl implements TestCaseActivity {

  protected FlowNode flowNode;
  protected MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics;

  protected TestCaseActivityScope parent;
  protected TestCaseActivity prev;
  protected TestCaseActivity next;
  protected TestCaseActivityType type;

  protected String attachedTo;
  protected String eventCode;
  protected String eventName;
  protected String topicName;

  protected boolean processEnd;
  protected boolean processStart;

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof TestCaseActivity)) {
      return false;
    }

    TestCaseActivity activity = (TestCaseActivity) obj;
    return activity.getId().equals(getId());
  }

  @Override
  public String getEventCode() {
    return eventCode;
  }

  @Override
  public String getEventName() {
    return eventName;
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
  public TestCaseActivity getNext() {
    if (!hasNext()) {
      throw new IllegalStateException("activity has no successors");
    }
    return next;
  }

  @Override
  public TestCaseActivityScope getParent() {
    if (!hasParent()) {
      throw new IllegalStateException("activity has no parent");
    }
    return parent;
  }

  @Override
  public TestCaseActivity getPrevious() {
    if (!hasPrevious()) {
      throw new IllegalStateException("activity has no predecessor");
    }
    return prev;
  }

  @Override
  public String getTopicName() {
    return topicName;
  }

  @Override
  public TestCaseActivityType getType() {
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
  public boolean hasPrevious(TestCaseActivityType type) {
    return hasPrevious() && getPrevious().getType() == type;
  }

  @Override
  public boolean isAsyncAfter() {
    return type != TestCaseActivityType.EVENT_BASED_GATEWAY && flowNode.isCamundaAsyncAfter();
  }

  @Override
  public boolean isAsyncBefore() {
    return flowNode.isCamundaAsyncBefore();
  }

  @Override
  public boolean isAttachedTo(TestCaseActivity activity) {
    return activity.getId().equals(attachedTo);
  }

  @Override
  public boolean isMultiInstance() {
    return multiInstanceLoopCharacteristics != null;
  }

  @Override
  public boolean isMultiInstanceParallel() {
    return !isMultiInstanceSequential();
  }

  @Override
  public boolean isMultiInstanceSequential() {
    if (!isMultiInstance()) {
      throw new IllegalStateException("activity is not a multi instance");
    }
    return multiInstanceLoopCharacteristics.isSequential();
  }

  @Override
  public boolean isProcessEnd() {
    return processEnd;
  }

  @Override
  public boolean isProcessStart() {
    return processStart;
  }
}
