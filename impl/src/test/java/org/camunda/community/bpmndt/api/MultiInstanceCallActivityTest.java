package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
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

public class MultiInstanceCallActivityTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  private MultiInstanceCallActivityHandler handler;

  @BeforeEach
  public void setUp() {
    handler = new MultiInstanceCallActivityHandler(tc.instance, "multiInstanceCallActivity");
  }

  @Test
  public void testExecute() {
    handler.verifyLoopCount(3);

    handler.handle(0).verifyInput(variables -> {
      assertThat(variables.getVariable("nrOfActiveInstances")).isNotNull();
      assertThat(variables.getVariable("nrOfActiveInstances")).isEqualTo(1);
      assertThat(variables.getVariable("nrOfCompletedInstances")).isNotNull();
      assertThat(variables.getVariable("nrOfCompletedInstances")).isEqualTo(0);
      assertThat(variables.getVariable("nrOfInstances")).isNotNull();
      assertThat(variables.getVariable("nrOfInstances")).isEqualTo(3);
      assertThat(variables.getVariable("loopCounter")).isNotNull();
      assertThat(variables.getVariable("loopCounter")).isEqualTo(0);
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
    });

    tc.createExecutor().withBean("multiInstanceCallActivityMapping", new MultiInstanceCallActivityMapping()).execute();
  }

  /**
   * Tests that an {@link AssertionError} is correctly unwrapped and rethrown.
   */
  @Test
  public void testVerifyWithAssertionError() {
    handler.handle(0).verify((pi, callActivity) -> {
      assertThat(callActivity.getDefinitionKey()).isEqualTo("not-equal");
    });

    AssertionError e = assertThrows(AssertionError.class, () -> {
      try {
        // disable logger temporary to avoid stacktrace in log output
        LogManager.getLogger("org.camunda").setLevel(Level.OFF);

        tc.createExecutor().withBean("multiInstanceCallActivityMapping", new MultiInstanceCallActivityMapping()).execute();
      } finally {
        LogManager.getLogger("org.camunda").setLevel(Level.WARN);
      }
    });

    assertThat(e.getMessage()).contains("expected: not-equal");
    assertThat(e.getMessage()).contains("but was : advanced");
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

    public MultiInstanceCallActivityHandler(TestCaseInstance instance, String activityId) {
      super(instance, activityId);
    }

    @Override
    protected CallActivityHandler createHandler(int loopIndex) {
      return new CallActivityHandler(instance, "multiInstanceCallActivity");
    }

    @Override
    protected boolean apply(ProcessInstance pi, int loopIndex) {
      registerCallActivityHandler(getHandler(loopIndex));

      instance.apply(getHandlerBefore(loopIndex));
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
}
