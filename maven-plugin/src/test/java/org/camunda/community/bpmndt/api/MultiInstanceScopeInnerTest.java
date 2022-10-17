package org.camunda.community.bpmndt.api;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
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

public class MultiInstanceScopeInnerTest {

  @Rule
  public TestCase tc = new TestCase();

  private MultiInstanceScopeHandler<?> handler;

  @Before
  public void setUp() {
    handler = new Handler(tc.instance, "subProcess");
  }

  @Test
  public void testExecute() {
    handler.verifyLoopCount(2).verifySequential();

    tc.createExecutor().execute();
  }

  private class TestCase extends AbstractJUnit4TestCase<TestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi, notNullValue());

      instance.apply(handler);

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.isWaitingAt("subProcess#multiInstanceBody");
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(Paths.get("./src/test/it/advanced-multi-instance/src/main/resources/scopeInner.bpmn"));
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

    @java.lang.Override
    protected boolean apply(ProcessInstance pi, int loopIndex) {
      // startEvent: subProcessStartEvent
      assertThat(pi).hasPassed("subProcessStartEvent");

      // endEvent: subProcessEndEvent
      assertThat(pi).hasPassed("subProcessEndEvent");

      return true;
    }
  }
}
