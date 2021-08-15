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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class JobTest {

  @Rule
  public TestCase tc = new TestCase();

  private JobHandler handler;

  @Before
  public void setUp() {
    handler = new JobHandler(tc.getProcessEngine(), "timerCatchEvent");
  }

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }

  @Test
  public void testVerify() {
    handler.verify((pi, job) -> {
      assertThat(pi, notNullValue());
      assertThat(job, notNullValue());
    });

    tc.createExecutor().execute();
  }

  private class TestCase extends AbstractJUnit4TestRule {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi, notNullValue());

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("timerCatchEvent");

      instance.apply(handler);

      piAssert.hasPassed("timerCatchEvent", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(Paths.get("./src/test/it/simple/src/main/resources/simpleTimerCatchEvent.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    protected String getProcessDefinitionKey() {
      return "simpleTimerCatchEvent";
    }

    @Override
    protected String getStart() {
      return "startEvent";
    }

    @Override
    protected String getEnd() {
      return "endEvent";
    }
  }
}
