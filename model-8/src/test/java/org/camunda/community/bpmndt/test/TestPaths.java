package org.camunda.community.bpmndt.test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Paths to BPMN files within the integration tests.
 */
public class TestPaths {

  public static Path advanced() {
    return of("advanced");
  }

  public static Path advanced(String fileName) {
    return advanced().resolve(fileName);
  }

  public static Path advancedMultiInstance() {
    return of("advanced-multi-instance");
  }

  public static Path advancedMultiInstance(String fileName) {
    return advancedMultiInstance().resolve(fileName);
  }

  public static Path of(String projectName) {
    return Paths.get("../integration-tests-8").resolve(projectName).resolve("src/main/resources");
  }

  public static Path of(String projectName, String fileName) {
    return of(projectName).resolve(fileName);
  }

  public static Path simple() {
    return of("simple");
  }

  public static Path simple(String fileName) {
    return simple().resolve(fileName);
  }
}
