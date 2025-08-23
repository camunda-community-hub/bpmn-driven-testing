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

public class MultiInstanceSequentialTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  private MultiInstanceHandler<?, ?> handler;

  @BeforeEach
  public void setUp() {
    handler = new MultiInstanceHandler<>(tc, "multiInstanceManualTask");
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
      assertThat(e.getMessage()).contains("1x, but was 3x");
    }
  }

  @Test
  public void testVerifyParallel() {
    handler.verifyParallel();

    try {
      tc.createExecutor().execute();
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("to be parallel, but was sequential");
    }
  }

  @Test
  public void testVerifyParallelCustomized() {
    handler.customize(it -> it.verifyParallel());

    try {
      tc.createExecutor().execute();
    } catch (AssertionError e) {
      assertThat(e.getMessage()).contains("to be parallel, but was sequential");
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
        return Files.newInputStream(TestPaths.advancedMultiInstance("sequential.bpmn"));
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
