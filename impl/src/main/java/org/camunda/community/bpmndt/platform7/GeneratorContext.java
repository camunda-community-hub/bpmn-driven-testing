package org.camunda.community.bpmndt.platform7;

import java.util.Collections;
import java.util.List;

import org.camunda.community.bpmndt.GeneratorContextBase;

public class GeneratorContext extends GeneratorContextBase {

  private List<String> processEnginePluginNames;
  private boolean springEnabled;

  /**
   * Returns the class names of process engine plugins, which should be registered at the process engine that executes the generated test cases.
   *
   * @return A list of process engine class names.
   */
  public List<String> getProcessEnginePluginNames() {
    return processEnginePluginNames != null ? processEnginePluginNames : Collections.emptyList();
  }

  public boolean isSpringEnabled() {
    return springEnabled;
  }

  public void setProcessEnginePluginNames(List<String> processEnginePluginNames) {
    this.processEnginePluginNames = processEnginePluginNames;
  }

  public void setSpringEnabled(boolean springEnabled) {
    this.springEnabled = springEnabled;
  }
}
