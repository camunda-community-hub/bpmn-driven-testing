package org.camunda.community.bpmndt;

import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.MultiInstanceLoopCharacteristics;

public class TestCaseActivity {

  private final FlowNode flowNode;
  private final String literal;
  private final MultiInstanceLoopCharacteristics multiInstance;

  private TestCaseActivityScope parent;
  private TestCaseActivityType type;
  private TestCaseActivity prev;
  private TestCaseActivity next;

  private boolean processEnd;

  private GeneratorStrategy strategy;

  private String attachedTo;
  private String eventCode;
  private String eventName;
  private String topicName;

  public TestCaseActivity(FlowNode flowNode, MultiInstanceLoopCharacteristics multiInstance) {
    this.flowNode = flowNode;
    this.multiInstance = multiInstance;

    literal = BpmnSupport.toLiteral(flowNode.getId());

    // initially OTHER, until it is set differently
    type = TestCaseActivityType.OTHER;
  }

  public <T extends FlowNode> T as(Class<T> type) {
    return type.cast(flowNode);
  }

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

  /**
   * Returns the code of the related error or escalation event.
   * 
   * @return The event code or {@code null}, if the test activity is not releated to an error or
   *         escalation event.
   */
  public String getEventCode() {
    return eventCode;
  }

  /**
   * Returns the name of the related message or signal event.
   * 
   * @return The event name or {@code null}, if the test activity is not related to an message or
   *         signal event.
   */
  public String getEventName() {
    return eventName;
  }

  /**
   * Returns the ID of the flow node.
   * 
   * @return The flow node ID.
   */
  public String getId() {
    return flowNode.getId();
  }

  /**
   * Returns the ID of the flow node as Java literal.
   * 
   * @return The ID as Java literal.
   * 
   * @see BpmnSupport#toJavaLiteral(String)
   */
  public String getLiteral() {
    return literal;
  }

  public MultiInstanceLoopCharacteristics getMultiInstance() {
    return multiInstance;
  }

  /**
   * Returns the next test activity.
   * 
   * @return The next activity or {@code null}, if this is the last activity.
   */
  public TestCaseActivity getNext() {
    return next;
  }

  /**
   * Return the parent test case activity scope.
   * 
   * @return The parent or {@code null}, if there is no parent.
   */
  public TestCaseActivityScope getParent() {
    return parent;
  }

  /**
   * Returns the previous test activity.
   * 
   * @return The previous activity or {@code null}, if this is the first activity.
   */
  public TestCaseActivity getPrev() {
    return prev;
  }

  public GeneratorStrategy getStrategy() {
    return strategy;
  }

  public String getTopicName() {
    return topicName;
  }

  public TestCaseActivityType getType() {
    return type;
  }

  public String getTypeName() {
    return flowNode.getElementType().getTypeName();
  }

  @Override
  public int hashCode() {
    return getId().hashCode();
  }

  public boolean hasMultiInstanceParent() {
    return parent != null && parent.isMultiInstance();
  }

  public boolean hasNext() {
    return next != null;
  }

  public boolean hasParent() {
    return parent != null;
  }

  public boolean hasPrev() {
    return prev != null;
  }

  /**
   * Checks if the activity has a predecessor and the predecessor's type is the given type.
   * 
   * @param type A specific test activity type.
   * 
   * @return {@code true}, if a previous activity with the given type exists. Otherwise {@code false}.
   */
  public boolean hasPrev(TestCaseActivityType type) {
    return hasPrev() && getPrev().getType() == type;
  }

  public boolean isAsyncAfter() {
    return type != TestCaseActivityType.EVENT_BASED_GATEWAY && flowNode.isCamundaAsyncAfter();
  }

  public boolean isAsyncBefore() {
    return flowNode.isCamundaAsyncBefore();
  }

  public boolean isAttachedTo(TestCaseActivity activity) {
    return activity.getId().equals(attachedTo);
  }

  public boolean isMultiInstance() {
    return multiInstance != null;
  }

  public boolean isProcessEnd() {
    return processEnd;
  }

  /**
   * Determines if the activity is a scope (embedded sub process or transaction) or an atomic
   * activity.
   * 
   * @return {@code true}, if the activity is a scope. Otherwise {@code false}.
   */
  public boolean isScope() {
    return false;
  }

  public void setAttachedTo(String attachedTo) {
    this.attachedTo = attachedTo;
  }

  public void setEventCode(String eventCode) {
    this.eventCode = eventCode;
  }

  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  public void setNext(TestCaseActivity next) {
    this.next = next;
  }

  public void setParent(TestCaseActivityScope parent) {
    this.parent = parent;
  }

  public void setPrev(TestCaseActivity prev) {
    this.prev = prev;
  }

  public void setProcessEnd(boolean processEnd) {
    this.processEnd = processEnd;
  }

  public void setStrategy(GeneratorStrategy strategy) {
    this.strategy = strategy;
  }

  public void setTopicName(String topicName) {
    this.topicName = topicName;
  }

  public void setType(TestCaseActivityType type) {
    this.type = type;
  }
}
