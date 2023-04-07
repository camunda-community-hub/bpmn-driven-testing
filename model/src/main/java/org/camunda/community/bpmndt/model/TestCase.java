package org.camunda.community.bpmndt.model;

import java.util.List;

import org.camunda.bpm.model.bpmn.instance.Process;

/**
 * Test case of BPMN process, including it's activities.
 */
public interface TestCase {

  /**
   * Gets the test case's activities.
   * 
   * @return A list of activities that are passed during test execution.
   */
  List<TestCaseActivity> getActivities();

  /**
   * Gets the scopes of the test case activities.
   * 
   * @return A list of involved scopes.
   */
  List<TestCaseActivityScope> getActivityScopes();

  /**
   * Returns the test case's description.
   * 
   * @return The description or {@code null}, if not specified.
   */
  String getDescription();

  /**
   * Gets the end activity, if the test case's path is not empty.
   * 
   * @return The end activity.
   * 
   * @throws IllegalStateException If the path is empty.
   * 
   * @see #hasEmptyPath()
   */
  TestCaseActivity getEndActivity();

  /**
   * Gets the flow node IDs of the test case's path.
   * 
   * @return A list of flow node IDs, starting with the start activity and ending with the end
   *         activity.
   */
  List<String> getFlowNodeIds();

  /**
   * Gets the flow node IDs of the test case's path, which are not valid (do not exist within the
   * related process).
   * 
   * @return A list of invalid flow node IDs.
   */
  List<String> getInvalidFlowNodeIds();

  /**
   * Returns the test case's name.
   * 
   * @return The name or {@code null}, if not specified.
   */
  String getName();

  /**
   * Returns the related process.
   * 
   * @return The BPMN process, that belongs to the test case.
   */
  Process getProcess();

  /**
   * Returns the ID of the underlying BPMN process.
   * 
   * @return The process ID (process definition key).
   * 
   * @see Process#getId()
   */
  String getProcessId();

  /**
   * Returns the name of the underlying BPMN process.
   * 
   * @return The process name or {@code null}, if not specified.
   * 
   * @see Process#getName()
   */
  String getProcessName();

  /**
   * Gets the start activity, if the test case's path is not empty.
   * 
   * @return The start activity.
   * 
   * @throws IllegalStateException If the path is empty.
   * 
   * @see #hasEmptyPath()
   */
  TestCaseActivity getStartActivity();

  /**
   * Determines if the test case path consists of no flow nodes.
   * 
   * @return {@code true}, if the test case has an empty path. Otherwise {@code false}.
   */
  boolean hasEmptyPath();

  /**
   * Determines if the test case path consists of only one flow node.
   * 
   * @return {@code true}, if the test case has an incomplete path. Otherwise {@code false}.
   */
  boolean hasIncompletePath();

  /**
   * Determines if the test case path contains invalid flow nodes.
   * 
   * @return {@code true}, if the test case has an invalid path. Otherwise {@code false}.
   * 
   * @see #getInvalidFlowNodeIds()
   */
  boolean hasInvalidPath();

  /**
   * Determines if the test case is valid - the path is not empty, incomplete nor invalid.
   * 
   * @return {@code true}, if the test case is valid. Otherwise {@code false}.
   * 
   * @see #hasEmptyPath()
   * @see #hasIncompletePath()
   * @see #hasInvalidPath()
   */
  boolean isValid();
}
