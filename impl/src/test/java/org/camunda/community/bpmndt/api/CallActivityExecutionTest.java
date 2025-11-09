package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class CallActivityExecutionTest {

  @RegisterExtension
  TestCase1 tc1 = new TestCase1();

  private boolean verifyInputCalled;
  private boolean verifyOutputCalled;

  @Test
  public void testExecute() {
    TestCase2 testCase2 = new TestCase2();
    TestCase3 testCase3 = new TestCase3();

    tc1.handler.verify((pi, callActivity) -> {
      assertThat(pi).isNotNull();
      pi.isNotEnded();
      pi.variables().containsEntry("super", true);

      assertThat(callActivity.getBinding()).isEqualTo(CallableElementBinding.VERSION_TAG);
      assertThat(callActivity.getBusinessKey()).isEqualTo("advancedKey");
      assertThat(callActivity.getDefinitionKey()).isEqualTo("advanced");
      assertThat(callActivity.getDefinitionTenantId()).isNull();
      assertThat(callActivity.getVersion()).isNull();
      assertThat(callActivity.getVersion()).isNull();
      assertThat(callActivity.getVersionTag()).isEqualTo("v1");
      assertThat(callActivity.hasInputs()).isFalse();
      assertThat(callActivity.hasOutputs()).isFalse();
    }).verifyInput(variables -> {
      assertThat(variables.getVariable("a")).isEqualTo("b");
      assertThat(variables.getVariable("x")).isEqualTo("y");

      verifyInputCalled = true;
    }).verifyOutput(variables -> {
      assertThat(variables.getVariable("a")).isEqualTo("y");
      assertThat(variables.getVariable("x")).isEqualTo("b");

      verifyOutputCalled = true;
    }).executeTestCase(testCase2, tc2 -> {
      tc2.handlerA.executeTestCase(testCase3, null);
    });

    tc1.createExecutor()
        .withBusinessKey("advancedKey")
        .withVariable("super", true)
        .withVariable("end", "none")
        .withBean("callActivityMapping", new CallActivityMapping())
        .verify(ProcessInstanceAssert::isEnded)
        .execute();

    assertThat(testCase2.executed).isTrue();
    assertThat(testCase3.executed).isTrue();

    assertThat(verifyInputCalled).isTrue();
    assertThat(verifyOutputCalled).isTrue();
  }

  private static class TestCase1 extends AbstractJUnit5TestCase<TestCase1> {

    private CallActivityHandler handler;

    @Override
    protected void beforeEach() {
      super.beforeEach();

      handler = new CallActivityHandler(this, "callActivity");
    }

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi).isNotNull();

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("callActivity");

      ProcessEngineTests.execute(ProcessEngineTests.job());
      instance.apply(handler);

      piAssert.hasPassed("callActivity", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.advanced("callActivityExecution1.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "callActivityExecution1";
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

      subVariables.put("end", superExecution.getVariable("end"));
    }

    @Override
    public void mapOutputVariables(DelegateExecution superExecution, VariableScope subInstance) {
      Object a = subInstance.getVariable("a");
      Object x = subInstance.getVariable("x");

      superExecution.setVariable("a", x);
      superExecution.setVariable("x", a);
    }
  }

  private static class TestCase2 extends AbstractJUnit5TestCase<TestCase1> {

    private CallActivityHandler handlerA;
    private CallActivityHandler handlerB;

    private boolean executed;

    @Override
    protected void beforeEach() {
      super.beforeEach();

      handlerA = new CallActivityHandler(this, "callActivityA");
      handlerB = new CallActivityHandler(this, "callActivityB");
    }

    @Override
    protected void execute(ProcessInstance pi) {
      executed = true;

      assertThat(pi).isNotNull();

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent");

      piAssert.isWaitingAt("callActivityA");
      ProcessEngineTests.execute(ProcessEngineTests.job(pi));
      instance.apply(handlerA);
      piAssert.hasPassed("callActivityA");

      piAssert.isWaitingAt("callActivityB");
      ProcessEngineTests.execute(ProcessEngineTests.job(pi));
      instance.apply(handlerB);
      piAssert.hasPassed("callActivityB");

      piAssert.hasPassed("fork", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.advanced("callActivityExecution2.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "callActivityExecution2";
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

  private static class TestCase3 extends AbstractJUnit5TestCase<TestCase1> {

    private boolean executed;

    @Override
    protected void execute(ProcessInstance pi) {
      executed = true;

      assertThat(pi).isNotNull();

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.advanced("callActivityExecution3.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "callActivityExecution3";
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
