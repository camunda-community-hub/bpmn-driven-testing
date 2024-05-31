package org.camunda.community.bpmndt.model;

import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent;

/**
 * Activity of a {@link TestCase}.
 */
public interface TestCaseActivity {

  /**
   * Returns the code of the related error or escalation event.
   *
   * @return The event code or {@code null}, if the activity is not an error or escalation event or the code is not specified.
   */
  String getEventCode();

  /**
   * Returns the name of the related message or signal event.
   *
   * @return The event name or {@code null}, if the test activity is not related to a message or signal event.
   */
  String getEventName();

  /**
   * Gets the underlying flow node.
   *
   * @return The activity's flow node.
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
   * @return The activity ID.
   */
  String getId();

  /**
   * Returns the name of the underlying flow node.
   *
   * @return The activity name.
   */
  String getName();

  /**
   * Determines how deep the activity is nested within the BPMN process. Level {@code 0}, means that the activity is not nested in a scope (embedded sub
   * process).
   *
   * @return The activity's nesting level.
   */
  int getNestingLevel();

  /**
   * Returns the next test activity, if this is not the last activity.
   *
   * @return The activity's successors.
   * @throws IllegalStateException If the activity has no successors.
   * @see #hasNext()
   */
  TestCaseActivity getNext();

  /**
   * Return the parent scope, if the underlying flow node is not a direct child of the BPMN process element.
   *
   * @return The parent test case activity scope.
   * @throws IllegalStateException If the activity has no parent.
   * @see #hasParent()
   */
  TestCaseActivityScope getParent();

  /**
   * Returns the previous test activity, if this is not the first activity.
   *
   * @return The activity's predecessor.
   * @throws IllegalStateException If the activity has no predecessor.
   * @see #hasPrevious()
   */
  TestCaseActivity getPrevious();

  /**
   * Gets the topic name of an external task activity.
   *
   * @return The topic name or {@code null}, if the activity is not an external task or the topic name is not specified.
   */
  String getTopicName();

  /**
   * Returns the type or {@link TestCaseActivityType#OTHER}, if there is no explicit type.
   *
   * @return The activity's type.
   */
  TestCaseActivityType getType();

  /**
   * Returns the name of the flow node element type.
   *
   * @return The type name.
   */
  String getTypeName();

  /**
   * Determines if the activity has a parent and the parent is a multi instance.
   *
   * @return {@code true}, if the activity has a multi instance parent scope. Otherwise {@code false}.
   * @see #hasParent()
   */
  boolean hasMultiInstanceParent();

  /**
   * Determines if the activity has a successor.
   *
   * @return {@code true}, if the activity has a successor. Otherwise {@code false}.
   */
  boolean hasNext();

  /**
   * Determines if the activity has a parent or if it is a direct child of the BPMN process element.
   *
   * @return {@code true}, if the activity has a parent scope. Otherwise {@code false}.
   */
  boolean hasParent();

  /**
   * Determines if the activity has a predecessor.
   *
   * @return {@code true}, if the activity has a predecessor. Otherwise {@code false}.
   */
  boolean hasPrevious();

  /**
   * Checks if the activity has a predecessor and the predecessor's type is the given type.
   *
   * @param type A specific test activity type.
   * @return {@code true}, if a previous activity with the given type exists. Otherwise {@code false}.
   */
  boolean hasPrevious(TestCaseActivityType type);

  /**
   * Returns {@code true}, if an asynchronous continuation after the activity is specified.
   *
   * @return {@code true}, if the activity is asynchronous after. Otherwise {@code false}.
   */
  boolean isAsyncAfter();

  /**
   * Returns {@code true}, if an asynchronous continuation before the activity is specified.
   *
   * @return {@code true}, if the activity is asynchronous before. Otherwise {@code false}.
   */
  boolean isAsyncBefore();

  /**
   * Checks if the activity is a boundary event, that is attached to the given activity.
   *
   * @param activity A specific activity.
   * @return {@code true}, if the activity is attached to the given activity. Otherwise {@code false}.
   */
  boolean isAttachedTo(TestCaseActivity activity);

  /**
   * Determines if the activity is a multi instance.
   *
   * @return {@code true}, if the activity is a multi instance. Otherwise {@code false}.
   */
  boolean isMultiInstance();

  /**
   * Determines if the activity is a parallel multi instance.
   *
   * @return {@code true}, if the activity is a parallel multi instance. Otherwise {@code false}.
   * @throws IllegalStateException If the activity is not a multi instance.
   */
  boolean isMultiInstanceParallel();

  /**
   * Determines if the activity is a sequential multi instance.
   *
   * @return {@code true}, if the activity is a sequential multi instance. Otherwise {@code false}.
   * @throws IllegalStateException If the activity is not a multi instance.
   */
  boolean isMultiInstanceSequential();

  /**
   * Returns {@code true}, if the activity is a start activity of the process.
   *
   * @return {@code true}, if the activity starts the process. Otherwise {@code false}.
   */
  boolean isProcessEnd();

  /**
   * Returns {@code true}, if the activity is an end activity of the process.
   *
   * @return {@code true}, if the activity ends the process. Otherwise {@code false}.
   */
  boolean isProcessStart();
}
