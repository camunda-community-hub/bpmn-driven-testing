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

public class MultiInstanceUserTaskTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  private MultiInstanceUserTaskHandler handler;

  @BeforeEach
  public void setUp() {
    handler = new MultiInstanceUserTaskHandler(tc.instance, "multiInstanceUserTask");
  }

  @Test
  public void testExecute() {
    handler.verifyLoopCount(3);

    handler.handle().verify((pi, task) -> task.hasName("User task"));

    tc.createExecutor().execute();
  }

  @Test
  public void testExecuteCustomized() {
    handler.customize(it -> {
      it.verifyLoopCount(3);

      it.handle().verify((pi, task) -> task.hasName("User task"));
    });

    tc.createExecutor().execute();
  }

  private class TestCase extends AbstractJUnit5TestCase<TestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi).isNotNull();

      instance.apply(handler);

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent", "multiInstanceUserTask#multiInstanceBody", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.advancedMultiInstance("userTask.bpmn"));
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

  private static class MultiInstanceUserTaskHandler extends MultiInstanceHandler<MultiInstanceUserTaskHandler, UserTaskHandler> {

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
