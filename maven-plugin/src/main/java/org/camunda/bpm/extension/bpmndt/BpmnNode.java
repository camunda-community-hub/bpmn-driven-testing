package org.camunda.bpm.extension.bpmndt;

import org.camunda.bpm.model.bpmn.instance.FlowNode;

/**
 * BPMN flow node wrapper.
 */
public interface BpmnNode {

  <T extends FlowNode> T as(Class<T> type);

  /**
   * Returns the ID of the wrapped flow node.
   * 
   * @return The flow node ID.
   */
  String getId();

  /**
   * Returns the ID of the flow node as Java literal.
   * 
   * @return The ID as Java literal.
   * 
   * @see BpmnSupport#convert(String)
   */
  String getLiteral();

  String getType();

  boolean isAsyncAfter();

  boolean isAsyncBefore();

  boolean isCallActivity();

  boolean isExternalTask();

  boolean isIntermediateCatchEvent();

  boolean isJob();

  boolean isUserTask();
}
