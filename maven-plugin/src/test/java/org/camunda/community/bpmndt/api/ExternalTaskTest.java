package org.camunda.community.bpmndt.api;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ExternalTaskTest {

  @Rule
  public TestCase tc = new TestCase();

  private ExternalTaskHandler handler;

  @Before
  public void setUp() {
    handler = new ExternalTaskHandler(tc.getProcessEngine(), "externalTask", "test-topic");
  }

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }

  @Test
  public void testWithVariables() {
    handler.withVariable("a", "b").withVariableTyped("x", Variables.stringValue("y")).complete();

    tc.createExecutor().verify(pi -> {
      pi.variables().containsEntry("a", "b");
      pi.variables().containsEntry("x", "y");
    }).execute();
  }

  @Test
  public void testVerify() {
    handler.withVariable("a", "b").verify((pi, topicName) -> {
      assertThat(pi, notNullValue());
      assertThat(topicName, equalTo("test-topic"));
    });

    tc.createExecutor().execute();
  }

  /**
   * Tests that the external task is not completed, when it should wait for a boundary event.
   */
  @Test
  public void testWaitForBoundaryEvent() {
    assertThat(handler.isWaitingForBoundaryEvent(), is(false));
    handler.waitForBoundaryEvent();
    assertThat(handler.isWaitingForBoundaryEvent(), is(true));

    AssertionError e = assertThrows(AssertionError.class, () -> {
      tc.createExecutor().execute();
    });

    // has not passed
    assertThat(e.getMessage(), containsString("to have passed activities [externalTask, endEvent]"));
  }

  private class TestCase extends AbstractJUnit4TestCase {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi, notNullValue());

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("externalTask");

      instance.apply(handler);

      piAssert.hasPassed("externalTask", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(Paths.get("./src/test/it/simple/src/main/resources/simpleExternalTask.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "simpleExternalTask";
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
