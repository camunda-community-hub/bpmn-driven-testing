package org.camunda.community.bpmndt;

import org.camunda.bpm.model.bpmn.instance.FlowNode;

public class TestCaseActivity {

  private final FlowNode flowNode;
  private final String literal;

  private TestCaseActivityType type;

  private TestCaseActivity prev;
  private TestCaseActivity next;

  private GeneratorStrategy strategy;

  private String attachedTo;
  private String eventCode;
  private String eventName;

  public TestCaseActivity(FlowNode flowNode) {
    this.flowNode = flowNode;

    literal = BpmnSupport.toLiteral(flowNode.getId());

    // initially OTHER, until it is set differently
    type = TestCaseActivityType.OTHER;
  }

  public <T extends FlowNode> T as(Class<T> type) {
    return type.cast(flowNode);
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

  /**
   * Returns the next test activity.
   * 
   * @return The next activity or {@code null}, if this is the last activity.
   */
  public TestCaseActivity getNext() {
    return next;
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

  public TestCaseActivityType getType() {
    return type;
  }

  public String getTypeName() {
    return flowNode.getElementType().getTypeName();
  }

  public boolean hasNext() {
    return next != null;
  }

  public boolean hasPrev() {
    return prev != null;
  }

  public boolean isAsyncAfter() {
    return flowNode.isCamundaAsyncAfter();
  }

  public boolean isAsyncBefore() {
    return flowNode.isCamundaAsyncBefore();
  }

  public boolean isAttachedTo(TestCaseActivity activity) {
    return activity.getId().equals(attachedTo);
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

  public void setPrev(TestCaseActivity prev) {
    this.prev = prev;
  }

  public void setStrategy(GeneratorStrategy strategy) {
    this.strategy = strategy;
  }

  public void setType(TestCaseActivityType type) {
    this.type = type;
  }
}
