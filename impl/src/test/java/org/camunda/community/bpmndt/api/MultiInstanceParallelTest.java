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

public class MultiInstanceParallelTest {

  @RegisterExtension
  public TestCase tc = new TestCase();

  private MultiInstanceParallelHandler handler;

  @BeforeEach
  public void setUp() {
    handler = new MultiInstanceParallelHandler(tc.instance, "multiInstanceManualTask");
  }

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }

  @Test
  public void testVerify() {
    handler.verifyLoopCount(3).verifyParallel();

    tc.createExecutor().execute();
  }

  @Test
  public void testVerifySequential() {
    handler.verifySequential();

    try {
      tc.createExecutor().execute();
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("to be sequential, but was parallel");
    }
  }

  private class TestCase extends AbstractJUnit5TestCase<TestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi).isNotNull();

      instance.apply(handler);

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent", "multiInstanceManualTask#multiInstanceBody", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.advancedMultiInstance("parallel.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "parallel";
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

  private static class MultiInstanceParallelHandler extends MultiInstanceHandler<MultiInstanceParallelHandler, Void> {
    public MultiInstanceParallelHandler(TestCaseInstance instance, String activityId) {
      super(instance, activityId);
    }

    @Override
    protected boolean isSequential() {
      return false;
    }
  }
}
