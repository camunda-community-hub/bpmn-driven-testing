package org.camunda.community.bpmndt.model;

import java.util.List;

import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.SubProcess;

/**
 * Scope (embedded sub process) of one or more test case activities.
 */
public interface TestCaseActivityScope {

  /**
   * Gets the scope's activities.
   * 
   * @return A list of activities that have the scope as parent.
   */
  List<TestCaseActivity> getActivities();

  /**
   * Gets the underlying flow node.
   * 
   * @return The scope's flow node.
   */
  FlowNode getFlowNode();

  /**
   * Gets the underlying flow node of the given sub process type.
   * 
   * @param <T> A sub process type..
   * 
   * @param subProcessType A specific sub process type.
   * 
   * @return The typed flow node.
   */
  <T extends SubProcess> T getFlowNode(Class<T> subProcessType);

  /**
   * Returns the ID of the underlying flow node.
   * 
   * @return The scope ID.
   */
  String getId();

  /**
   * Returns the name of the underlying flow node.
   * 
   * @return The scope name.
   */
  String getName();

  /**
   * Determines how deep the scope is nested within the BPMN process. Level {@code 0}, means that the
   * scope is not nested in another scope (embedded sub process).
   * 
   * @return The scope's nesting level.
   */
  int getNestingLevel();

  /**
   * Return the parent scope, if the scope is not a direct child of the related BPMN process element.
   * 
   * @return The parent test case activity scope.
   * 
   * @throws IllegalStateException If the scope has no parent.
   * 
   * @see #hasParent()
   */
  TestCaseActivityScope getParent();

  /**
   * Returns the name of the flow node element type.
   * 
   * @return The type name.
   */
  String getTypeName();

  /**
   * Determines if the scope has a parent or if it is a direct child of the BPMN process element.
   * 
   * @return {@code true}, if the scope has a parent scope. Otherwise {@code false}.
   */
  boolean hasParent();

  /**
   * Determines if the scope is a multi instance.
   * 
   * @return {@code true}, if the scope is a multi instance. Otherwise {@code false}.
   */
  boolean isMultiInstance();

  /**
   * Determines if the scope is a parallel multi instance.
   * 
   * @return {@code true}, if the scope is a parallel multi instance. Otherwise {@code false}.
   * 
   * @throws IllegalStateException If the scope is not a multi instance.
   */
  boolean isMultiInstanceParallel();

  /**
   * Determines if the scope is a sequential multi instance.
   * 
   * @return {@code true}, if the scope is a sequential multi instance. Otherwise {@code false}.
   * 
   * @throws IllegalStateException If the scope is not a multi instance.
   */
  boolean isMultiInstanceSequential();
}
