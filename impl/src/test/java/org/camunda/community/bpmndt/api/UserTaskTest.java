package org.camunda.community.bpmndt.api;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class UserTaskTest {

  @Rule
  public TestCase tc = new TestCase();

  private UserTaskHandler handler;

  @Before
  public void setUp() {
    handler = new UserTaskHandler(tc.getProcessEngine(), "userTask");
  }

  @Test
  public void testExecute() {
    assertThat(tc.createExecutor().execute(), notNullValue());
  }

  @Test
  public void testExecuteWithProcessInstance() {
    RuntimeService runtimeService = tc.getProcessEngine().getRuntimeService();

    ProcessInstance pi = runtimeService.startProcessInstanceByKey(tc.getProcessDefinitionKey());

    tc.createExecutor().execute(pi);
  }

  @Test
  public void testExecuteWithProcessInstanceId() {
    RuntimeService runtimeService = tc.getProcessEngine().getRuntimeService();

    ProcessInstance pi = runtimeService.startProcessInstanceByKey(tc.getProcessDefinitionKey());

    tc.createExecutor().execute(pi.getId());
  }

  @Test
  public void testExecuteCustom() {
    handler.execute(task -> {
      assertThat(task, notNullValue());

      tc.getProcessEngine().getTaskService().complete(task.getId());
    });

    tc.createExecutor().execute();
  }

  @Test
  public void testVerify() {
    handler.verify((pi, task) -> {
      assertThat(pi, notNullValue());
      assertThat(task, notNullValue());

      pi.variables().containsEntry("test", 123);

      task.hasName("User task");
    });

    tc.createExecutor().withVariable("test", 123).execute();
  }

  @Test
  public void testWithVariables() {
    handler.withVariable("a", "b").withVariableTyped("x", Variables.stringValue("y")).complete();

    tc.createExecutor().verify(pi -> {
      pi.variables().containsEntry("a", "b");
      pi.variables().containsEntry("x", "y");
    }).execute();
  }

  /**
   * Tests that the user task is not completed, when it should wait for a boundary event.
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
    assertThat(e.getMessage(), containsString("to have passed activities [userTask, endEvent]"));
  }

  private class TestCase extends AbstractJUnit4TestCase<TestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi, notNullValue());

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("userTask");

      instance.apply(handler);

      piAssert.hasPassed("userTask", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.simple("simpleUserTask.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "simpleUserTask";
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
