package org.camunda.community.bpmndt.api;

import static org.camunda.community.bpmndt.api.TestCaseInstance.PROCESS_ENGINE_NAME;

import org.camunda.bpm.engine.ProcessEngine;

/**
 * Abstract superclass for JUnit 4 and Spring based test cases.
 */
public abstract class AbstractJUnit4SpringBasedTestRule extends AbstractJUnit4TestRule {

  /**
   * If this method is invoked, the current Spring application context does not provide a BPMN Driven
   * Testing conform process engine.
   */
  @Override
  protected ProcessEngine buildProcessEngine() {
    String message = String.format("Spring application context must provide a process engine with name '%s'", PROCESS_ENGINE_NAME);
    throw new UnsupportedOperationException(message);
  }
}
