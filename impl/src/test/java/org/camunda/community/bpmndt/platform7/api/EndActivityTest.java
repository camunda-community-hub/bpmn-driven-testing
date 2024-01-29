package org.camunda.community.bpmndt.platform7.api;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.community.bpmndt.test.Platform7TestPaths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class EndActivityTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  /**
   * Tests if the execution is waiting after the test case's end activity, when it does not end the process.
   */
  @Test
  public void testExecute() {
    tc.createExecutor().verify(ProcessInstanceAssert::isNotEnded).execute();
  }

  private static class TestCase extends AbstractJUnit5TestCase<TestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi).isNotNull();

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("subProcessEndEvent");
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(Platform7TestPaths.simple("simpleSubProcess.bpmn"));
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
