package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Tests a sequential multi instance call activity without simulation.
 */
public class MultiInstanceCallActivityExecutionTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  private MultiInstanceCallActivityHandler handler;

  private boolean verifyInputCalled0;
  private boolean verifyInputCalled1;
  private boolean verifyInputCalled2;

  @BeforeEach
  public void setUp() {
    handler = new MultiInstanceCallActivityHandler(tc, "multiInstanceCallActivity");
  }

  @Test
  public void testExecute() {
    handler.verifyLoopCount(3);

    handler.handle(0).executeTestCase(new AdvancedTestCase(), null);
    handler.handle(1).executeTestCase(new AdvancedTestCase(), null);
    handler.handle(2).executeTestCase(new AdvancedTestCase(), null);

    handler.handle(0).verifyInput(variables -> {
      assertThat(variables.getVariable("nrOfActiveInstances")).isNotNull();
      assertThat(variables.getVariable("nrOfActiveInstances")).isEqualTo(1);
      assertThat(variables.getVariable("nrOfCompletedInstances")).isNotNull();
      assertThat(variables.getVariable("nrOfCompletedInstances")).isEqualTo(0);
      assertThat(variables.getVariable("nrOfInstances")).isNotNull();
      assertThat(variables.getVariable("nrOfInstances")).isEqualTo(3);
      assertThat(variables.getVariable("loopCounter")).isNotNull();
      assertThat(variables.getVariable("loopCounter")).isEqualTo(0);

      verifyInputCalled0 = true;
    });

    handler.handle(1).verifyInput(variables -> {
      assertThat(variables.getVariable("nrOfActiveInstances")).isNotNull();
      assertThat(variables.getVariable("nrOfActiveInstances")).isEqualTo(1);
      assertThat(variables.getVariable("nrOfCompletedInstances")).isNotNull();
      assertThat(variables.getVariable("nrOfCompletedInstances")).isEqualTo(1);
      assertThat(variables.getVariable("nrOfInstances")).isNotNull();
      assertThat(variables.getVariable("nrOfInstances")).isEqualTo(3);
      assertThat(variables.getVariable("loopCounter")).isNotNull();
      assertThat(variables.getVariable("loopCounter")).isEqualTo(1);

      verifyInputCalled1 = true;
    });

    handler.handle(2).verifyInput(variables -> {
      assertThat(variables.getVariable("nrOfActiveInstances")).isNotNull();
      assertThat(variables.getVariable("nrOfActiveInstances")).isEqualTo(1);
      assertThat(variables.getVariable("nrOfCompletedInstances")).isNotNull();
      assertThat(variables.getVariable("nrOfCompletedInstances")).isEqualTo(2);
      assertThat(variables.getVariable("nrOfInstances")).isNotNull();
      assertThat(variables.getVariable("nrOfInstances")).isEqualTo(3);
      assertThat(variables.getVariable("loopCounter")).isNotNull();
      assertThat(variables.getVariable("loopCounter")).isEqualTo(2);

      verifyInputCalled2 = true;
    });

    tc.createExecutor().withBean("multiInstanceCallActivityMapping", new MultiInstanceCallActivityMapping()).execute();

    assertThat(verifyInputCalled0).isTrue();
    assertThat(verifyInputCalled1).isTrue();
    assertThat(verifyInputCalled2).isTrue();
  }

  private class TestCase extends AbstractJUnit5TestCase<TestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi).isNotNull();

      instance.apply(handler);

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent", "multiInstanceCallActivity#multiInstanceBody", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.advancedMultiInstance("callActivity.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "callActivity";
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

  private static class MultiInstanceCallActivityHandler extends MultiInstanceHandler<MultiInstanceCallActivityHandler, CallActivityHandler> {

    public MultiInstanceCallActivityHandler(AbstractTestCase<?> testCase, String activityId) {
      super(testCase, activityId);
    }

    @Override
    protected CallActivityHandler createHandler(int loopIndex) {
      return new CallActivityHandler(testCase, "multiInstanceCallActivity");
    }

    @Override
    protected boolean apply(ProcessInstance pi, int loopIndex) {
      CallActivityHandler handler = getHandler(loopIndex);
      registerCallActivityHandler(handler);

      instance.apply(getHandlerBefore(loopIndex));
      instance.apply(handler);
      instance.apply(getHandlerAfter(loopIndex));

      return true;
    }
  }

  private static class MultiInstanceCallActivityMapping implements DelegateVariableMapping {

    @Override
    public void mapInputVariables(DelegateExecution superExecution, VariableMap subVariables) {
      subVariables.putAll(superExecution.getVariables());
    }

    @Override
    public void mapOutputVariables(DelegateExecution superExecution, VariableScope subInstance) {
      // nothing to do here
    }
  }

  private static class AdvancedTestCase extends AbstractJUnit5TestCase<AdvancedTestCase> {

    private JobHandler handler;

    @Override
    protected void beforeEach() {
      super.beforeEach();

      handler = new JobHandler(getProcessEngine(), "timerEvent");
    }

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi).isNotNull();

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("timerEvent");

      instance.apply(handler);

      piAssert.hasPassed("timerEvent", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.advancedMultiInstance().resolve("sub").resolve("advanced.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "advanced";
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
