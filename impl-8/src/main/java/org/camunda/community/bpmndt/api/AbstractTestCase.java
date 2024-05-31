package org.camunda.community.bpmndt.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.stream.Collectors;

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

  private String simulateSubProcessResource;

  /**
   * Performs the setup for a test case execution by initializing all element handlers. This method must be invoked before each test!
   */
  protected void beforeEach() {
    // empty default implementation
  }

  /**
   * Creates a new executor, used to specify variables that are considered during test case execution. After the specification,
   * {@link TestCaseExecutor#execute()} is called to create a new process instance and execute the test case.
   *
   * @param engine The Zeebe test engine to use.
   * @return The newly created executor.
   */
  public TestCaseExecutor createExecutor(ZeebeTestEngine engine) {
    if (simulateSubProcessResource == null) {
      simulateSubProcessResource = getSimulateSubProcessResource();
    }

    return new TestCaseExecutor(this, engine, simulateSubProcessResource);
  }

  /**
   * Executes the test case.
   *
   * @param instance           The test case instance to use.
   * @param processInstanceKey The key of the related process instance, created during the start of the test case.
   */
  protected abstract void execute(TestCaseInstance instance, long processInstanceKey);

  /**
   * Returns the ID of the BPMN process that is tested.
   *
   * @return The BPMN process ID.
   */
  public abstract String getBpmnProcessId();

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
   * Returns the ID of the test case's end activity.
   *
   * @return The end activity ID.
   */
  public abstract String getEnd();

  /**
   * Returns the BPMN resource used to simulate arbitrary sub processes. This method must be overridden, since the default implementation relies on a classpath
   * resource, which is not available when a concrete test case is executed.
   *
   * @return The simulate sub process BPMN as string.
   */
  protected String getSimulateSubProcessResource() {
    try (InputStream resource = this.getClass().getResourceAsStream("/simulate-sub-process.bpmn")) {
      return new BufferedReader(new InputStreamReader(Objects.requireNonNull(resource))).lines().collect(Collectors.joining("\n"));
    } catch (IOException e) {
      throw new RuntimeException("failed to read simulate sub process BPMN resource", e);
    }
  }

  /**
   * Returns the ID of the test case's start element.
   *
   * @return The start activity ID.
   */
  public abstract String getStart();

  /**
   * Determines if the test case's start element is a message start event or not. This method returns {@code false}, if not overridden.
   *
   * @return {@code true}, if the test case's start element is a message start event. Otherwise {@code false}.
   */
  protected boolean isMessageStart() {
    return false;
  }

  /**
   * Determines if the test case's start element starts the process or not. This is the case if the BPMN element is a start event and if the element's parent
   * scope is the process. This method returns {@code true}, if not overridden.
   *
   * @return {@code true}, if the test case's start element starts the process. Otherwise {@code false}.
   */
  protected boolean isProcessStart() {
    return true;
  }

  /**
   * Determines if the test case's start element is a signal start event or not. This method returns {@code false}, if not overridden.
   *
   * @return {@code true}, if the test case's start element is a signal start event. Otherwise {@code false}.
   */
  protected boolean isSignalStart() {
    return false;
  }

  /**
   * Determines if the test case's start element is a timer start event or not. This method returns {@code false}, if not overridden.
   *
   * @return {@code true}, if the test case's start element is a timer start event. Otherwise {@code false}.
   */
  protected boolean isTimerStart() {
    return false;
  }
}
