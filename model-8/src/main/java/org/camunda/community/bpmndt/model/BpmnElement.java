package org.camunda.community.bpmndt.model;

import io.camunda.zeebe.model.bpmn.instance.FlowNode;
import io.camunda.zeebe.model.bpmn.instance.IntermediateCatchEvent;

/**
 * Element of a {@link TestCase}.
 */
public interface BpmnElement {

  /**
   * Gets the underlying flow node.
   *
   * @return The element's flow node.
   */
  FlowNode getFlowNode();

  /**
   * Gets the underlying flow node of the given type.
   *
   * @param <T>          A flow node type - e.g. {@link IntermediateCatchEvent}.
   * @param flowNodeType A specific flow node type.
   * @return The typed flow node.
   */
  <T extends FlowNode> T getFlowNode(Class<T> flowNodeType);

  /**
   * Returns the ID of the underlying flow node.
   *
   * @return The element ID.
   */
  String getId();

  /**
   * Returns the name of the underlying flow node.
   *
   * @return The element name.
   */
  String getName();

  /**
   * Determines how deep the BPMN element is nested within the BPMN process. Level {@code 0}, means that the element is not nested in a scope (embedded sub
   * process).
   *
   * @return The element's nesting level.
   */
  int getNestingLevel();

  /**
   * Returns the next BPMN element, if this is not the last element.
   *
   * @return The element's successors.
   * @throws IllegalStateException If the element has no successors.
   * @see #hasNext()
   */
  BpmnElement getNext();

  /**
   * Return the parent BPMN element scope, if the underlying flow node is not a direct child of the BPMN process element.
   *
   * @return The parent scope.
   * @throws IllegalStateException If the element has no parent.
   * @see #hasParent()
   */
  BpmnElementScope getParent();

  /**
   * Returns the previous BPMN element, if this is not the first element.
   *
   * @return The element's predecessor.
   * @throws IllegalStateException If the element has no predecessor.
   * @see #hasPrevious()
   */
  BpmnElement getPrevious();

  /**
   * Returns the type or {@link BpmnElementType#OTHER}, if there is no explicit type.
   *
   * @return The element's type.
   */
  BpmnElementType getType();

  /**
   * Returns the name of the flow node element type.
   *
   * @return The type name.
   */
  String getTypeName();

  /**
   * Determines if the BPMN element has a parent and the parent is a multi instance.
   *
   * @return {@code true}, if the element has a multi instance parent scope. Otherwise {@code false}.
   * @see #hasParent()
   */
  boolean hasMultiInstanceParent();

  /**
   * Determines if the BPMN element has a successor.
   *
   * @return {@code true}, if the element has a successor. Otherwise {@code false}.
   */
  boolean hasNext();

  /**
   * Determines if the BPMN element has a parent or if it is a direct child of the BPMN process element.
   *
   * @return {@code true}, if the element has a parent scope. Otherwise {@code false}.
   */
  boolean hasParent();

  /**
   * Determines if the BPMN element has a predecessor.
   *
   * @return {@code true}, if the element has a predecessor. Otherwise {@code false}.
   */
  boolean hasPrevious();

  /**
   * Checks if the BPMN element has a predecessor and the predecessor's type is the given type.
   *
   * @param type A specific element type.
   * @return {@code true}, if a previous element with the given type exists. Otherwise {@code false}.
   */
  boolean hasPrevious(BpmnElementType type);

  /**
   * Checks if the BPMN element is a boundary event, that is attached to the given element.
   *
   * @param element A specific element.
   * @return {@code true}, if the element is attached to the given element. Otherwise {@code false}.
   */
  boolean isAttachedTo(BpmnElement element);

  /**
   * Determines if the BPMN element is a multi instance.
   *
   * @return {@code true}, if the element is a multi instance. Otherwise {@code false}.
   */
  boolean isMultiInstance();

  /**
   * Determines if the BPMN element is a sequential multi instance.
   *
   * @return {@code true}, if the element is a sequential multi instance. Otherwise {@code false}.
   * @throws IllegalStateException If the element is not a multi instance.
   */
  boolean isMultiInstanceSequential();

  /**
   * Returns {@code true}, if the BPMN element is a none start element of the process.
   *
   * @return {@code true}, if the BPMN element is a none start element of the process. Otherwise {@code false}.
   */
  boolean isProcessStart();
}
