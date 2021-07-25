package org.camunda.community.bpmndt.api;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class UserTaskHandlerTest {

  @Rule
  public TestCase tc = new TestCase();

  private UserTaskHandler handler;

  @Before
  public void setUp() {
    handler = new UserTaskHandler(tc.getProcessEngine(), "userTask");
  }

  @Test
  public void testDefaults() {
    tc.createExecutor().execute();
  }

  @Test
  public void testComplete() {
    handler.complete(task -> {
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

  private class TestCase extends AbstractJUnit4TestRule {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi, notNullValue());

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("userTask");

      instance.apply(handler);

      piAssert.hasPassed("userTask", "endEvent").isEnded();
    }

    @Override
    protected String getBpmnResourceName() {
      return "bpmn/simpleUserTask.bpmn";
    }

    @Override
    protected String getProcessDefinitionKey() {
      return "simpleUserTask";
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
