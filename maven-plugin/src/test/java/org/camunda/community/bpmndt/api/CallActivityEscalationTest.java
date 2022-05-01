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

public class CallActivityEscalationTest {

  @Rule
  public TestCase tc = new TestCase();

  private CallActivityHandler handler;

  @Before
  public void setUp() {
    handler = new CallActivityHandler(tc.instance, "callActivity");
  }

  @Test
  public void testExecute() {
    handler.simulateEscalation("callActivityEscalation");

    tc.createExecutor().execute();
  }

  private class TestCase extends AbstractJUnit4TestCase<TestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi, notNullValue());

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("callActivity");

      ProcessEngineTests.execute(ProcessEngineTests.job());

      piAssert.hasPassed("callActivity", "escalationBoundaryEvent", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(Paths.get("./src/test/it/advanced/src/main/resources/callActivityEscalation.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "callActivityEscalation";
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
