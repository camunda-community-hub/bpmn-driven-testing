package org.camunda.community.bpmndt8.test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Paths to BPMN files within the integration tests.
 */
public class TestPaths {

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
