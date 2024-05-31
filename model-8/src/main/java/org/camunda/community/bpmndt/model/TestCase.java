package org.camunda.community.bpmndt.model;

import java.util.List;

import io.camunda.zeebe.model.bpmn.instance.Process;

/**
 * Test case of BPMN process, including its elements.
 */
public interface TestCase {

  /**
   * Returns the test case's description.
   *
   * @return The description or {@code null}, if not specified.
   */
  String getDescription();

  /**
   * Gets the test case's elements.
   *
   * @return A list of BPMN elements that are passed during test execution.
   */
  List<BpmnElement> getElements();

  /**
   * Gets the BPMN element IDs of the test case's path.
   *
   * @return A list of BPMN element IDs, starting with the start element and ending with the end element.
   */
  List<String> getElementIds();

  /**
   * Gets the end element, if the test case's path is not empty.
   *
   * @return The end element.
   * @throws IllegalStateException If the path is empty.
   * @see #hasEmptyPath()
   */
  BpmnElement getEndElement();

  /**
   * Gets the BPMN element IDs of the test case's path, which are not valid (do not exist within the related process).
   *
   * @return A list of invalid BPMN element IDs.
   */
  List<String> getInvalidElementIds();

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
   * @see Process#getId()
   */
  String getProcessId();

  /**
   * Returns the name of the underlying BPMN process.
   *
   * @return The process name or {@code null}, if not specified.
   * @see Process#getName()
   */
  String getProcessName();

  /**
   * Gets the start element, if the test case's path is not empty.
   *
   * @return The start element.
   * @throws IllegalStateException If the path is empty.
   * @see #hasEmptyPath()
   */
  BpmnElement getStartElement();

  /**
   * Determines if the test case path consists of no elements.
   *
   * @return {@code true}, if the test case has an empty path. Otherwise {@code false}.
   */
  boolean hasEmptyPath();

  /**
   * Determines if the test case path consists of only one BPMN element.
   *
   * @return {@code true}, if the test case has an incomplete path. Otherwise {@code false}.
   */
  boolean hasIncompletePath();

  /**
   * Determines if the test case path contains invalid BPMN elements.
   *
   * @return {@code true}, if the test case has an invalid path. Otherwise {@code false}.
   * @see #getInvalidElementIds()
   */
  boolean hasInvalidPath();

  /**
   * Determines if the test case is valid - the path is not empty, incomplete nor invalid.
   *
   * @return {@code true}, if the test case is valid. Otherwise {@code false}.
   * @see #hasEmptyPath()
   * @see #hasIncompletePath()
   * @see #hasInvalidPath()
   */
  boolean isValid();
}
