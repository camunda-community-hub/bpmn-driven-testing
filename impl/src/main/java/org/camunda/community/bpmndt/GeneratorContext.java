package org.camunda.community.bpmndt;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class GeneratorContext {

  private Path basePath;
  private boolean externalTaskClientUsed;
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
   * Returns the class names of process engine plugins, which should be registered at the process engine that executes the generated test cases.
   *
   * @return A list of process engine class names.
   */
  public List<String> getProcessEnginePluginNames() {
    return processEnginePluginNames != null ? processEnginePluginNames : Collections.emptyList();
  }

  public Path getTestSourcePath() {
    return testSourcePath;
  }

  public boolean isExternalTaskClientUsed() {
    return externalTaskClientUsed;
  }

  public boolean isSpringEnabled() {
    return springEnabled;
  }

  public void setBasePath(Path basePath) {
    this.basePath = basePath;
  }

  public void setExternalTaskClientUsed(boolean externalTaskClientUsed) {
    this.externalTaskClientUsed = externalTaskClientUsed;
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
