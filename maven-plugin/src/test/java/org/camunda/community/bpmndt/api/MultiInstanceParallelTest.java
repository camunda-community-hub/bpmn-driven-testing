package org.camunda.community.bpmndt.api;

import static org.hamcrest.CoreMatchers.containsString;
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

public class MultiInstanceParallelTest {

  @Rule
  public TestCase tc = new TestCase();

  private MultiInstanceParallelHandler handler;

  @Before
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
      assertThat(e.getMessage(), containsString("to be sequential, but was parallel"));
    }
  }

  private class TestCase extends AbstractJUnit4TestCase {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi, notNullValue());

      instance.apply(handler);

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent", "multiInstanceManualTask#multiInstanceBody", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(Paths.get("./src/test/it/advanced-multi-instance/src/main/resources/parallel.bpmn"));
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

  private class MultiInstanceParallelHandler extends MultiInstanceHandler<MultiInstanceParallelHandler, Void> {
    public MultiInstanceParallelHandler(TestCaseInstance instance, String activityId) {
      super(instance, activityId);
    }

    @Override
    protected boolean isSequential() {
      return false;
    }
  }
}
