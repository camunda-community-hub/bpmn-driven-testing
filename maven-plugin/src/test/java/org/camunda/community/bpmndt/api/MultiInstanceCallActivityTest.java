package org.camunda.community.bpmndt.api;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.bpm.engine.variable.VariableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class MultiInstanceCallActivityTest {

  @Rule
  public TestCase tc = new TestCase();

  private MultiInstanceCallActivityHandler handler;

  @Before
  public void setUp() {
    handler = new MultiInstanceCallActivityHandler(tc.instance, "multiInstanceCallActivity");
  }

  @Test
  public void testExecute() {
    handler.verifyLoopCount(3);

    handler.handle(0).verifyInput(variables -> {
      assertThat(variables.getVariable("nrOfActiveInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfActiveInstances"), is(1));
      assertThat(variables.getVariable("nrOfCompletedInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfCompletedInstances"), is(0));
      assertThat(variables.getVariable("nrOfInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfInstances"), is(3));
      assertThat(variables.getVariable("loopCounter"), notNullValue());
      assertThat(variables.getVariable("loopCounter"), is(0));
    });

    handler.handle(1).verifyInput(variables -> {
      assertThat(variables.getVariable("nrOfActiveInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfActiveInstances"), is(1));
      assertThat(variables.getVariable("nrOfCompletedInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfCompletedInstances"), is(1));
      assertThat(variables.getVariable("nrOfInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfInstances"), is(3));
      assertThat(variables.getVariable("loopCounter"), notNullValue());
      assertThat(variables.getVariable("loopCounter"), is(1));
    });

    handler.handle(2).verifyInput(variables -> {
      assertThat(variables.getVariable("nrOfActiveInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfActiveInstances"), is(1));
      assertThat(variables.getVariable("nrOfCompletedInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfCompletedInstances"), is(2));
      assertThat(variables.getVariable("nrOfInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfInstances"), is(3));
      assertThat(variables.getVariable("loopCounter"), notNullValue());
      assertThat(variables.getVariable("loopCounter"), is(2));
    });

    tc.createExecutor().withBean("multiInstanceCallActivityMapping", new MultiInstanceCallActivityMapping()).execute();
  }

  /**
   * Tests that an {@link AssertionError} is correctly unwrapped and rethrown.
   */
  @Test
  public void testVerifyWithAssertionError() {
    handler.handle(0).verify((pi, callActivity) -> {
      assertThat(callActivity.getDefinitionKey(), equalTo("not-equal"));
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

    assertThat(e.getMessage(), containsString("Expected: \"not-equal\""));
    assertThat(e.getMessage(), containsString("but: was \"advanced\""));
  }

  private class TestCase extends AbstractJUnit4TestCase {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi, notNullValue());

      handler.apply(pi);

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent", "multiInstanceCallActivity#multiInstanceBody", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(Paths.get("./src/test/it/advanced-multi-instance/src/main/resources/callActivity.bpmn"));
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

  private class MultiInstanceCallActivityHandler extends MultiInstanceHandler<MultiInstanceCallActivityHandler, CallActivityHandler> {
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

  private class MultiInstanceCallActivityMapping implements DelegateVariableMapping {

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
