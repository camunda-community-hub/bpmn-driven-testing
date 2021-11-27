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

public class MultiInstanceSequentialTest {

  @Rule
  public TestCase tc = new TestCase();

  private MultiInstanceHandler<?, ?> handler;

  @Before
  public void setUp() {
    handler = new MultiInstanceHandler<>(tc.instance, "multiInstanceManualTask");
  }

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }

  @Test
  public void testVerify() {
    handler.verifyLoopCount(3).verifySequential();

    tc.createExecutor().execute();
  }

  @Test
  public void testVerifyLoopCount() {
    handler.verifyLoopCount(1);

    try {
      tc.createExecutor().execute();
    } catch (AssertionError e) {
      assertThat(e.getMessage(), containsString("1x, but was 3x"));
    }
  }

  @Test
  public void testVerifyParallel() {
    handler.verifyParallel();

    try {
      tc.createExecutor().execute();
    } catch (AssertionError e) {
      assertThat(e.getMessage(), containsString("to be parallel, but was sequential"));
    }
  }

  private class TestCase extends AbstractJUnit4TestCase {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi, notNullValue());

      handler.apply(pi);

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent", "multiInstanceManualTask#multiInstanceBody", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(Paths.get("./src/test/it/advanced-multi-instance/src/main/resources/sequential.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "sequential";
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
