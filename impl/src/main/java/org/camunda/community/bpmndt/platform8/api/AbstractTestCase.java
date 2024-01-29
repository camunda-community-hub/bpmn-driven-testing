package org.camunda.community.bpmndt.platform8.api;

import java.io.InputStream;

import io.camunda.zeebe.process.test.api.ZeebeTestEngine;

/**
 * Abstract superclass for test cases.
 */
public abstract class AbstractTestCase {

  /**
   * The related test class - must be provided by the concrete implementation.
   */
  Class<?> testClass;
  /**
   * The test method, which will be executed - must be provided by the concrete implementation.
   */
  String testMethodName;

  /**
   * Creates a new executor, used to specify variables that are considered during test case execution. After the specification,
   * {@link TestCaseExecutor#execute()} is called to create a new process instance and execute the test case.
   *
   * @param engine The Zeebe test engine to use.
   * @return The newly created executor.
   */
  public TestCaseExecutor createExecutor(ZeebeTestEngine engine) {
    return new TestCaseExecutor(this, engine);
  }

  /**
   * Returns the ID of the BPMN process that is tested.
   *
   * @return The BPMN process ID.
   */
  public abstract String getBpmnProcessId();

  /**
   * Returns the ID of the test case's end activity.
   *
   * @return The end activity ID.
   */
  public abstract String getEnd();

  /**
   * Returns the ID of the test case's start activity.
   *
   * @return The start activity ID.
   */
  public abstract String getStart();

  /**
   * Executes the test case.
   *
   * @param instance           The test case instance to use.
   * @param processInstanceKey The key of the process instance, created especially for the test case.
   */
  protected abstract void execute(TestCaseInstance instance, long processInstanceKey);

  /**
   * Returns an input stream that provides the BPMN resource with the process definition to be tested - either this method or {@link #getBpmnResourceName()}
   * must be overridden!
   *
   * @return The BPMN resource as stream.
   */
  protected InputStream getBpmnResource() {
    return null;
  }

  /**
   * Returns the name of the BPMN resource, that provides the process definition to be tested - either this method or {@link #getBpmnResource()} must be
   * overridden!
   *
   * @return The BPMN resource name, within {@code src/main/resources}.
   */
  protected String getBpmnResourceName() {
    return null;
  }

  /**
   * Determines if the test case's end activity ends the process or not. This is the case if the activity is an end event and if the activity's parent scope is
   * the process. This method returns {@code true}, if not overridden.
   *
   * @return {@code true}, if the test case's end activity ends the process. Otherwise {@code false}.
   */
  protected boolean isProcessEnd() {
    return true;
  }

  /**
   * Determines if the test case's start activity starts the process or not. This is the case if the activity is a start event and if the activity's parent
   * scope is the process. This method returns {@code true}, if not overridden.
   *
   * @return {@code true}, if the test case's start activity starts the process. Otherwise {@code false}.
   */
  protected boolean isProcessStart() {
    return true;
  }
}
