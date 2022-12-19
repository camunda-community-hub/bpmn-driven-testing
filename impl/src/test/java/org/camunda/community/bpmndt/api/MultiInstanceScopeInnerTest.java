package org.camunda.community.bpmndt.api;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

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

public class MultiInstanceScopeInnerTest {

  @RegisterExtension
  public TestCase tc = new TestCase();

  private MultiInstanceScopeHandler<?> handler;

  @BeforeEach
  public void setUp() {
    handler = new Handler(tc.instance, "subProcess");
  }

  @Test
  public void testExecute() {
    handler.verifyLoopCount(2).verifySequential();

    tc.createExecutor().execute();
  }

  private class TestCase extends AbstractJUnit5TestCase<TestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi).isNotNull();

      instance.apply(handler);

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.isWaitingAt("subProcess#multiInstanceBody");
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.advancedMultiInstance("scopeInner.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "scopeInner";
    }

    @Override
    public String getStart() {
      return "subProcess#multiInstanceBody";
    }

    @Override
    public String getEnd() {
      return "subProcess#multiInstanceBody";
    }

    @Override
    protected boolean isProcessEnd() {
      return false;
    }
  }

  private static class Handler extends MultiInstanceScopeHandler<Handler> {

    public Handler(TestCaseInstance instance, String activityId) {
      super(instance, activityId);
    }

    @Override
    protected boolean apply(ProcessInstance pi, int loopIndex) {
      // startEvent: subProcessStartEvent
      assertThat(pi).hasPassed("subProcessStartEvent");

      // endEvent: subProcessEndEvent
      assertThat(pi).hasPassed("subProcessEndEvent");

      return true;
    }
  }
}
