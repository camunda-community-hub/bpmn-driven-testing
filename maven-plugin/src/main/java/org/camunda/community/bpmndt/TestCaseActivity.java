package org.camunda.community.bpmndt;

import org.camunda.bpm.model.bpmn.instance.FlowNode;

public class TestCaseActivity {

  private final FlowNode flowNode;
  private final String literal;

  private String eventName;
  private TestCaseActivityType type;

  public TestCaseActivity(FlowNode flowNode) {
    this.flowNode = flowNode;

    literal = BpmnSupport.toJavaLiteral(flowNode.getId());

    // initially OTHER, until it is set differently
    type = TestCaseActivityType.OTHER;
  }

  public <T extends FlowNode> T as(Class<T> type) {
    return type.cast(flowNode);
  }

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
   * @see BpmnSupport#convert(String)
   */
  public String getLiteral() {
    return literal;
  }

  public String getLiteralAfter() {
    return String.format("%sAfter", getLiteral());
  }

  public String getLiteralBefore() {
    return String.format("%sBefore", getLiteral());
  }

  public TestCaseActivityType getType() {
    return type;
  }

  public String getTypeName() {
    return flowNode.getElementType().getTypeName();
  }

  public boolean isAsyncAfter() {
    return flowNode.isCamundaAsyncAfter();
  }

  public boolean isAsyncBefore() {
    return flowNode.isCamundaAsyncBefore();
  }

  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  public void setType(TestCaseActivityType type) {
    this.type = type;
  }
}
