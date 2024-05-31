package org.camunda.community.bpmndt.model;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;

/**
 * Test cases of a BPMN model.
 */
public interface TestCases {

  /**
   * Reads test cases from the given input stream. A runtime exception is thrown, when the BPMN model could not be parsed.
   *
   * @param stream A stream to read from.
   * @return The test cases.
   */
  static TestCases of(InputStream stream) {
    return TestCasesImpl.of(stream);
  }

  /**
   * Gets the test cases of the given BPMN model instance.
   *
   * @param modelInstance A model instance with or without "bpmndt:testCases" extension elements.
   * @return The test cases.
   */
  static TestCases of(BpmnModelInstance modelInstance) {
    return TestCasesImpl.of(modelInstance);
  }

  /**
   * Reads test cases from the BPMN file with the given path. A runtime exception is thrown, when the file could not be found or read or the BPMN model could
   * not be parsed.
   *
   * @param bpmnFile A readable BPMN file.
   * @return The test cases.
   */
  static TestCases of(Path bpmnFile) {
    return TestCasesImpl.of(bpmnFile);
  }

  /**
   * Gets all test cases of all processes that are defined in the BPMN model.
   *
   * @return A list, containing all test cases.
   */
  List<TestCase> get();

  /**
   * Gets the test cases of the process with the given ID.
   *
   * @param processId A specific process ID.
   * @return The test cases of the process or an empty list, if the process has no test cases or does not exist.
   */
  List<TestCase> get(String processId);

  /**
   * Returns the underlying BPMN model instance.
   *
   * @return The BPMN model instance.
   */
  BpmnModelInstance getModelInstance();

  /**
   * Gets the IDs of all processes that are defined in the BPMN model.
   *
   * @return A set, containing all process IDs.
   */
  Set<String> getProcessIds();

  /**
   * Determines if the BPMN model does not define test cases.
   *
   * @return {@code true}, if no test cases are defined. Otherwise {@code false}.
   */
  boolean isEmpty();
}
