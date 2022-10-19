package org.camunda.community.bpmndt.api;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement.CallableElementBinding;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class CallActivityTest {

  @Rule
  public TestCase tc = new TestCase().withTenantId("tenant-x");

  private CallActivityHandler handler;

  private TestCaseExecutor executor;

  @Before
  public void setUp() {
    handler = new CallActivityHandler(tc.instance, "callActivity");

    executor = tc.createExecutor()
        .withBusinessKey("simpleKey")
        .withVariable("super", true)
        .withBean("callActivityMapping", new CallActivityMapping());
  }

  @Test
  public void testExecute() {
    executor.execute();
  }

  @Test
  public void testVerify() {
    handler.verify((pi, callActivity) -> {
      assertThat(pi, notNullValue());
      pi.isNotEnded();
      pi.variables().containsEntry("super", true);

      assertThat(callActivity.getBinding(), is(CallableElementBinding.DEPLOYMENT));
      assertThat(callActivity.getBusinessKey(), equalTo("simpleKey"));
      assertThat(callActivity.getDefinitionKey(), equalTo("simple"));
      assertThat(callActivity.getDefinitionTenantId(), equalTo("tenant-x"));
      assertThat(callActivity.getVersion(), nullValue());
      assertThat(callActivity.getVersion(), nullValue());
      assertThat(callActivity.getVersionTag(), nullValue());
    }).verifyInput(variables -> {
      assertThat(variables.getVariable("a"), equalTo("b"));
      assertThat(variables.getVariable("x"), equalTo("y"));
    }).verifyOutput(variables -> {
      assertThat(variables.getVariable("a"), equalTo("y"));
      assertThat(variables.getVariable("x"), equalTo("b"));
    });

    executor.execute();
  }

  /**
   * Tests that the call activity is not ended, when it should wait for a boundary event.
   */
  @Test
  public void testWaitForBoundaryEvent() {
    assertThat(handler.isWaitingForBoundaryEvent(), is(false));
    handler.waitForBoundaryEvent();
    assertThat(handler.isWaitingForBoundaryEvent(), is(true));

    AssertionError e = assertThrows(AssertionError.class, () -> {
      executor.execute();
    });

    // has passed start event, but not call activity
    assertThat(e.getMessage(), containsString("it passed [startEvent]"));
  }

  private class TestCase extends AbstractJUnit4TestCase<TestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi, notNullValue());

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("callActivity");

      ProcessEngineTests.execute(ProcessEngineTests.job());

      piAssert.hasPassed("callActivity", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.simple("simpleCallActivity.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "simpleCallActivity";
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

  private static class CallActivityMapping implements DelegateVariableMapping {

    @Override
    public void mapInputVariables(DelegateExecution superExecution, VariableMap subVariables) {
      assertThat(superExecution.hasVariable("super"), is(true));
      assertThat(subVariables.isEmpty(), is(true));

      subVariables.put("a", "b");
      subVariables.put("x", "y");
    }

    @Override
    public void mapOutputVariables(DelegateExecution superExecution, VariableScope subInstance) {
      assertThat(superExecution.hasVariable("super"), is(true));
      assertThat(subInstance.hasVariable("super"), is(false));

      Object a = subInstance.getVariable("a");
      Object x = subInstance.getVariable("x");

      superExecution.setVariable("a", x);
      superExecution.setVariable("x", a);
    }
  }
}
