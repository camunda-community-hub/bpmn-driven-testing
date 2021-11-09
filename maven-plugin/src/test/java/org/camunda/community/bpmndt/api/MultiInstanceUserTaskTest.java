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

public class MultiInstanceUserTaskTest {

  @Rule
  public TestCase tc = new TestCase();

  private MultiInstanceUserTaskHandler handler;

  @Before
  public void setUp() {
    handler = new MultiInstanceUserTaskHandler(tc.instance, "multiInstanceUserTask");
  }

  @Test
  public void testExecute() {
    handler.verifyLoopCount(3);

    handler.handleDefault().verify((pi, task) -> {
      task.hasName("User task");
    });

    tc.createExecutor().execute();
  }

  private class TestCase extends AbstractJUnit4TestRule {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi, notNullValue());

      handler.apply(pi);

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent", "multiInstanceUserTask#multiInstanceBody", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(Paths.get("./src/test/it/advanced-multi-instance/src/main/resources/userTask.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "userTask";
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

  private class MultiInstanceUserTaskHandler extends MultiInstanceHandler<MultiInstanceUserTaskHandler, UserTaskHandler> {
    public MultiInstanceUserTaskHandler(TestCaseInstance instance, String activityId) {
      super(instance, activityId);
    }

    @Override
    protected UserTaskHandler createHandler(int loopIndex) {
      return new UserTaskHandler(getProcessEngine(), "multiInstanceUserTask");
    }

    @Override
    protected boolean apply(ProcessInstance pi, int loopIndex) {
      instance.apply(getHandlerBefore(loopIndex));
      instance.apply(getHandler(loopIndex));
      instance.apply(getHandlerAfter(loopIndex));

      return true;
    }
  }
}
