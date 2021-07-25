package org.camunda.bpm.extension.bpmndt.impl;

/**
 * Constants used for the BPMN test case generation.
 */
public final class GeneratorConstants {

  // Type names
  public static final String SPRING_CONFIGURATION = "BpmndtConfiguration";

  // Method names
  public static final String EXECUTE = "execute";
  public static final String GET_BPMN_RESOURCE_NAME = "getBpmnResourceName";
  public static final String GET_END = "getEnd";
  public static final String GET_PROCESS_DEFINITION_KEY = "getProcessDefinitionKey";
  public static final String GET_PROCESS_ENGINE_PLUGINS = "getProcessEnginePlugins";
  public static final String GET_START = "getStart";
  public static final String STARTING = "starting";

  private GeneratorConstants() {
    // hidden constructor
  }
}

