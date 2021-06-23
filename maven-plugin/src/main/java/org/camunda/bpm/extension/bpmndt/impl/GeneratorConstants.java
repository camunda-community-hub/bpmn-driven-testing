package org.camunda.bpm.extension.bpmndt.impl;

/**
 * Constants used for the BPMN test case generation.
 */
public final class GeneratorConstants {

  // Type names
  public static final String TYPE_ABSTRACT_TEST_CASE = "AbstractTestCase";
  public static final String TYPE_BPMNDT_CONFIGURATION = "BpmndtConfiguration";
  public static final String TYPE_BPMNDT_PLUGIN = "BpmndtPlugin";
  public static final String TYPE_CALL_ACTIVITY_PARSE_LISTENER = "CallActivityParseListener";
  public static final String TYPE_CALL_ACTIVITY_RULE = "CallActivityRule";

  // Field and variable names
  public static final String ACTIVITY_ID = "activityId";
  public static final String BUSINESS_KEY = "businessKey";
  public static final String CALL_ACTIVITY_RULE = "callActivityRule";
  public static final String EXTERNAL_TASK = "externalTask";
  public static final String EXTERNAL_TASK_SERVICE = "externalTaskService";
  public static final String EVENT_NAME = "eventName";
  public static final String EVENT_SUBSCRIPTION = "subscription";
  public static final String JOB = "job";
  public static final String PROCESS_ENGINE = "processEngine";
  public static final String PROCESS_ENGINE_CONFIGURATION = "configuration";
  public static final String PROCESS_ENGINE_RULE = "rule";
  public static final String PROCESS_INSTANCE = "pi";
  public static final String PROCESS_INSTANCE_ASSERT = "processInstanceAssert";
  public static final String TASK = "task";
  public static final String TASK_SERVICE = "taskService";
  public static final String TOPIC_NAME = "topicName";
  public static final String VARIABLES = "variables";

  // Method names
  public static final String AFTER = "after";
  public static final String ASSERT_THAT_PI = "assertThatPi";
  public static final String BEFORE = "before";
  public static final String BUILD_PROCESS_ENGINE = "buildProcessEngine";
  public static final String BUILD_PROCESS_ENGINE_CONFIGURATION = "buildProcessEngineConfiguration";
  public static final String FIND_EVENT_SUBSCRIPTION = "findEventSubscription";
  /** Name of the actual test method. */
  public static final String TEST_PATH = "testPath";

  /** Name of the process engine, initialized within the test case setup. */
  public static final String PROCESS_ENGINE_NAME = "bpmndt";

  private GeneratorConstants() {
    // hidden constructor
  }
}

