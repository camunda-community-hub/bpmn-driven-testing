package org.camunda.community.bpmndt;

/**
 * Please note: This constants cannot be used within the API classes nor within the generated test
 * code, since this class will not be available in the test classpath of the target project.
 */
public final class Constants {

  public static final String BPMN_EXTENSION = ".bpmn";
  public static final String JAVA_EXTENSION = ".java";

  public static final String NS = "http://camunda.org/schema/extension/bpmn-driven-testing";

  public static final String ELEMENT_DESCRIPTION = "description";
  public static final String ELEMENT_NAME = "name";
  public static final String ELEMENT_NODE = "node";
  public static final String ELEMENT_PATH = "path";
  public static final String ELEMENT_TEST_CASE = "testCase";
  public static final String ELEMENT_TEST_CASES = "testCases";

  private Constants() {
  }
}
