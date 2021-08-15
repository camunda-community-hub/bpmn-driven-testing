package org.camunda.community.bpmndt.api;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement.CallableElementBinding;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.bpm.engine.variable.VariableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class CallActivityTest {

  @Rule
  public TestCase tc = new TestCase();

  private CallActivityHandler handler;

  @Before
  public void setUp() {
    handler = new CallActivityHandler(tc.instance, "callActivity");
  }

  @Test
  public void testExecute() {
    tc.createExecutor().withMock("callActivityMapping", new CallActivityMapping()).execute();
  }

  @Test
  public void testVerify() {
    handler.verify((pi, callActivity) -> {
      assertThat(callActivity.getBinding(), is(CallableElementBinding.DEPLOYMENT));
      assertThat(callActivity.getBusinessKey(), equalTo("simpleKey"));
      assertThat(callActivity.getDefinitionKey(), equalTo("simple"));
      assertThat(callActivity.getDefinitionTenantId(), nullValue());
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

    tc.createExecutor().withBusinessKey("simpleKey").withMock("callActivityMapping", new CallActivityMapping()).execute();
  }

  private class TestCase extends AbstractJUnit4TestRule {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi, notNullValue());

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent", "callActivity", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(Paths.get("./src/test/it/simple/src/main/resources/simpleCallActivity.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    protected String getProcessDefinitionKey() {
      return "simpleCallActivity";
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

  private class CallActivityMapping implements DelegateVariableMapping {

    @Override
    public void mapInputVariables(DelegateExecution superExecution, VariableMap subVariables) {
      subVariables.put("a", "b");
      subVariables.put("x", "y");
    }

    @Override
    public void mapOutputVariables(DelegateExecution superExecution, VariableScope subInstance) {
      Object a = subInstance.getVariable("a");
      Object x = subInstance.getVariable("x");

      superExecution.setVariable("a", x);
      superExecution.setVariable("x", a);
    }
  }
}
