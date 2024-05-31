package org.camunda.community.bpmndt;

import java.nio.file.Path;

public class GeneratorContext {

  private Path basePath;
  private Path mainResourcePath;
  private String packageName;
  private Path testSourcePath;

  public Path getBasePath() {
    return basePath;
  }

  public Path getMainResourcePath() {
    return mainResourcePath;
  }

  /**
   * Gets the configured base package name or the default value.
   *
   * @return The base package name.
   */
  public String getPackageName() {
    return packageName;
  }

  public Path getTestSourcePath() {
    return testSourcePath;
  }

  public void setBasePath(Path basePath) {
    this.basePath = basePath;
  }

  public void setMainResourcePath(Path mainResourcePath) {
    this.mainResourcePath = mainResourcePath;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public void setTestSourcePath(Path testSourcePath) {
    this.testSourcePath = testSourcePath;
  }
}
