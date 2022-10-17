package org.example.it;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.variable.VariableMap;
import org.junit.Rule;
import org.junit.Test;

import generated.callactivityerror.TC_startEvent__endEvent;

public class CallActivityErrorTest {

  @Rule
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.handleMultiInstanceCallActivity().verifyLoopCount(2);

    tc.handleMultiInstanceCallActivity().handle(0).verifyInput(variables -> {
      assertThat(variables.getVariable("nrOfActiveInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfActiveInstances"), is(1));
      assertThat(variables.getVariable("nrOfCompletedInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfCompletedInstances"), is(0));
      assertThat(variables.getVariable("nrOfInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfInstances"), is(3));
      assertThat(variables.getVariable("loopCounter"), notNullValue());
      assertThat(variables.getVariable("loopCounter"), is(0));
    });

    tc.handleMultiInstanceCallActivity().handle(1).verifyInput(variables -> {
      assertThat(variables.getVariable("nrOfActiveInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfActiveInstances"), is(1));
      assertThat(variables.getVariable("nrOfCompletedInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfCompletedInstances"), is(1));
      assertThat(variables.getVariable("nrOfInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfInstances"), is(3));
      assertThat(variables.getVariable("loopCounter"), notNullValue());
      assertThat(variables.getVariable("loopCounter"), is(1));
    });

    tc.handleMultiInstanceCallActivity().handle(1).simulateBpmnError("callActivityError", "callActivityErrorMessage");

    tc.createExecutor().withBean("multiInstanceCallActivityMapping", new MultiInstanceCallActivityMapping()).execute();
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
