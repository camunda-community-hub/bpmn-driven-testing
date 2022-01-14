package org.camunda.community.bpmndt;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class GeneratorContext {

  private Path basePath;
  private boolean h2Version2;
  private Path mainResourcePath;
  private String packageName;
  private List<String> processEnginePluginNames;
  private boolean springEnabled;
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

  /**
   * Returns the class names of process engine plugins, which should be registered at the process
   * engine that executes the generated test cases.
   * 
   * @return A list of process engine class names.
   */
  public List<String> getProcessEnginePluginNames() {
    return processEnginePluginNames != null ? processEnginePluginNames : Collections.emptyList();
  }

  public Path getTestSourcePath() {
    return testSourcePath;
  }

  public boolean isH2Version2() {
    return h2Version2;
  }

  public boolean isSpringEnabled() {
    return springEnabled;
  }

  public void setBasePath(Path basePath) {
    this.basePath = basePath;
  }

  public void setH2Version2(boolean h2Version2) {
    this.h2Version2 = h2Version2;
  }

  public void setMainResourcePath(Path mainResourcePath) {
    this.mainResourcePath = mainResourcePath;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public void setProcessEnginePluginNames(List<String> processEnginePluginNames) {
    this.processEnginePluginNames = processEnginePluginNames;
  }

  public void setSpringEnabled(boolean springEnabled) {
    this.springEnabled = springEnabled;
  }

  public void setTestSourcePath(Path testSourcePath) {
    this.testSourcePath = testSourcePath;
  }
}
