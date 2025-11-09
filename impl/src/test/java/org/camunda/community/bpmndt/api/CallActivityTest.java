package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class CallActivityTest {

  @RegisterExtension
  TestCase tc = new TestCase().withTenantId("tenant-x");

  private CallActivityHandler handler;

  private TestCaseExecutor executor;

  @BeforeEach
  public void setUp() {
    handler = new CallActivityHandler(tc, "callActivity");

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
      assertThat(pi).isNotNull();
      pi.isNotEnded();
      pi.variables().containsEntry("super", true);

      assertThat(callActivity.getBinding()).isEqualTo(CallableElementBinding.DEPLOYMENT);
      assertThat(callActivity.getBusinessKey()).isEqualTo("simpleKey");
      assertThat(callActivity.getDefinitionKey()).isEqualTo("simple");
      assertThat(callActivity.getDefinitionTenantId()).isEqualTo("tenant-x");
      assertThat(callActivity.getVersion()).isNull();
      assertThat(callActivity.getVersion()).isNull();
      assertThat(callActivity.getVersionTag()).isNull();
      assertThat(callActivity.hasInputs()).isFalse();
      assertThat(callActivity.hasOutputs()).isFalse();
    }).verifyInput(variables -> {
      assertThat(variables.getVariable("a")).isEqualTo("b");
      assertThat(variables.getVariable("x")).isEqualTo("y");
    }).verifyOutput(variables -> {
      assertThat(variables.getVariable("a")).isEqualTo("y");
      assertThat(variables.getVariable("x")).isEqualTo("b");
    });

    executor.execute();
  }

  /**
   * Tests that the call activity is not ended, when it should wait for a boundary event.
   */
  @Test
  public void testWaitForBoundaryEvent() {
    assertThat(handler.isWaitingForBoundaryEvent()).isFalse();
    handler.waitForBoundaryEvent();
    assertThat(handler.isWaitingForBoundaryEvent()).isTrue();

    AssertionError e = assertThrows(AssertionError.class, () -> executor.execute());

    // has passed start event, but not call activity
    assertThat(e.getMessage()).contains("it passed [startEvent]");
  }

  private static class TestCase extends AbstractJUnit5TestCase<TestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi).isNotNull();

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
      assertThat(superExecution.hasVariable("super")).isTrue();
      assertThat(subVariables.isEmpty()).isTrue();

      subVariables.put("a", "b");
      subVariables.put("x", "y");
    }

    @Override
    public void mapOutputVariables(DelegateExecution superExecution, VariableScope subInstance) {
      assertThat(superExecution.hasVariable("super")).isTrue();
      assertThat(subInstance.hasVariable("super")).isFalse();

      Object a = subInstance.getVariable("a");
      Object x = subInstance.getVariable("x");

      superExecution.setVariable("a", x);
      superExecution.setVariable("x", a);
    }
  }
}
