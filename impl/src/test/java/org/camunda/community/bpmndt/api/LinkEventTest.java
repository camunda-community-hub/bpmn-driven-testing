package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class LinkEventTest {

  @RegisterExtension
  public TestCase tc = new TestCase();

  @Test
  public void testExecute() {
    tc.createExecutor().withVariable("forkA", true).execute();
  }

  private class TestCase extends AbstractJUnit5TestCase<TestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi).isNotNull();

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("forkA").hasNotPassed("linkThrowEventA").isWaitingAt("linkCatchEventA");
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.advanced("linkEvent.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "linkEvent";
    }

    @Override
    public String getStart() {
      return "forkA";
    }

    @Override
    public String getEnd() {
      return "linkCatchEventA";
    }

    @Override
    protected boolean isProcessEnd() {
      return false;
    }
  }
}
