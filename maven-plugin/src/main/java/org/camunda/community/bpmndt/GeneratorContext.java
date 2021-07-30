package org.camunda.community.bpmndt;

import java.nio.file.Path;

public class GeneratorContext {

  private Path basePath;
  private Path mainResourcePath;
  private Path testSourcePath;

  private String packageName;
  private boolean springEnabled;

  public Path getBasePath() {
    return basePath;
  }

  public Path getMainResourcePath() {
    return mainResourcePath;
  }

  public String getPackageName() {
    return packageName;
  }

  public Path getTestSourcePath() {
    return testSourcePath;
  }

  public boolean isSpringEnabled() {
    return springEnabled;
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

  public void setSpringEnabled(boolean springEnabled) {
    this.springEnabled = springEnabled;
  }

  public void setTestSourcePath(Path testSourcePath) {
    this.testSourcePath = testSourcePath;
  }
}
