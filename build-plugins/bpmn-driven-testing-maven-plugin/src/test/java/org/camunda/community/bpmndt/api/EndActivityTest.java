package org.camunda.community.bpmndt.api;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.junit.Rule;
import org.junit.Test;

public class EndActivityTest {

  @Rule
  public TestCase tc = new TestCase();

  /**
   * Tests if the execution is waiting after the test case's end activity, when it does not end the
   * process.
   */
  @Test
  public void testExecute() {
    tc.createExecutor().verify(pi -> {
      pi.isNotEnded();
    }).execute();
  }

  private class TestCase extends AbstractJUnit4TestCase<TestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi, notNullValue());

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("subProcessEndEvent");
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(Paths.get("./src/test/it/simple/src/main/resources/simpleSubProcess.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "simpleSubProcess";
    }

    @Override
    public String getStart() {
      return "startEvent";
    }

    @Override
    public String getEnd() {
      return "subProcessEndEvent";
    }

    @Override
    protected boolean isProcessEnd() {
      return false;
    }
  }
}
