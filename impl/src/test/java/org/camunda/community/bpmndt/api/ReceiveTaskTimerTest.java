package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ReceiveTaskTimerTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  private ReceiveTaskHandler handler;
  private JobHandler timerBoundaryHandler;

  @BeforeEach
  public void setUp() {
    handler = new ReceiveTaskHandler(tc.getProcessEngine(), "receiveTask", "advanced");
    handler.waitForBoundaryEvent();

    timerBoundaryHandler = new JobHandler(tc.getProcessEngine(), "timerBoundaryEvent");
  }

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }

  private class TestCase extends AbstractJUnit5TestCase<TestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi).isNotNull();

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("receiveTask");

      instance.apply(handler);
      instance.apply(timerBoundaryHandler);

      piAssert.hasPassed("receiveTask", "timerBoundaryEvent", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.advanced("receiveTaskTimer.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "receiveTaskTimer";
    }

    @Override
    public String getStart() {
      return "startEvent";
    }

    @Override
    public String getEnd() {
      return "endEvent";
    }
  }
}
