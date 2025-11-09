package org.example.it;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.variable.VariableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.callactivityparallel.TC_startEvent__endEvent;
import generated.callactivityparallel.TC_startEvent__multiInstanceCallActivity;

public class CallActivityParallelExecutionTest {

  @RegisterExtension
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();
  @RegisterExtension
  public TC_startEvent__multiInstanceCallActivity tcWaitAfter = new TC_startEvent__multiInstanceCallActivity();

  @Test
  public void testExecute() {
    tc.handleMultiInstanceCallActivity().verifyLoopCount(3);

    tc.handleMultiInstanceCallActivity().handle(0).executeTestCase(new generated.advanced.TC_startEvent__endEvent(), null);
    tc.handleMultiInstanceCallActivity().handle(1).executeTestCase(new generated.advanced.TC_startEvent__endEvent(), null);
    tc.handleMultiInstanceCallActivity().handle(2).executeTestCase(new generated.advanced.TC_startEvent__endEvent(), null);

    tc.handleMultiInstanceCallActivity().handle(0).verifyInput(variables -> {
      assertThat(variables.getVariable("nrOfActiveInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfActiveInstances"), is(3));
      assertThat(variables.getVariable("nrOfCompletedInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfCompletedInstances"), is(0));
      assertThat(variables.getVariable("nrOfInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfInstances"), is(3));
      assertThat(variables.getVariable("loopCounter"), notNullValue());
      assertThat(variables.getVariable("loopCounter"), is(0));
    });

    tc.handleMultiInstanceCallActivity().handle(1).verifyInput(variables -> {
      assertThat(variables.getVariable("nrOfActiveInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfActiveInstances"), is(2));
      assertThat(variables.getVariable("nrOfCompletedInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfCompletedInstances"), is(1));
      assertThat(variables.getVariable("nrOfInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfInstances"), is(3));
      assertThat(variables.getVariable("loopCounter"), notNullValue());
      assertThat(variables.getVariable("loopCounter"), is(1));
    });

    tc.handleMultiInstanceCallActivity().handle(2).verifyInput(variables -> {
      assertThat(variables.getVariable("nrOfActiveInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfActiveInstances"), is(1));
      assertThat(variables.getVariable("nrOfCompletedInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfCompletedInstances"), is(2));
      assertThat(variables.getVariable("nrOfInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfInstances"), is(3));
      assertThat(variables.getVariable("loopCounter"), notNullValue());
      assertThat(variables.getVariable("loopCounter"), is(2));
    });

    tc.createExecutor().withBean("multiInstanceCallActivityMapping", new MultiInstanceCallActivityMapping())
      .verify(pi -> {
        pi.isEnded();
      })
      .execute();
  }

  @Test
  public void testExecuteAndWaitAfter() {
    tcWaitAfter.handleMultiInstanceCallActivity().verifyLoopCount(3);

    tc.handleMultiInstanceCallActivity().handle(0).executeTestCase(new generated.advanced.TC_startEvent__endEvent(), null);
    tc.handleMultiInstanceCallActivity().handle(1).executeTestCase(new generated.advanced.TC_startEvent__endEvent(), null);
    tc.handleMultiInstanceCallActivity().handle(2).executeTestCase(new generated.advanced.TC_startEvent__endEvent(), null);

    tcWaitAfter.handleMultiInstanceCallActivity().handle(0).verifyInput(variables -> {
      assertThat(variables.getVariable("nrOfActiveInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfActiveInstances"), is(3));
      assertThat(variables.getVariable("nrOfCompletedInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfCompletedInstances"), is(0));
      assertThat(variables.getVariable("nrOfInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfInstances"), is(3));
      assertThat(variables.getVariable("loopCounter"), notNullValue());
      assertThat(variables.getVariable("loopCounter"), is(0));
    });

    tcWaitAfter.handleMultiInstanceCallActivity().handle(1).verifyInput(variables -> {
      assertThat(variables.getVariable("nrOfActiveInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfActiveInstances"), is(2));
      assertThat(variables.getVariable("nrOfCompletedInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfCompletedInstances"), is(1));
      assertThat(variables.getVariable("nrOfInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfInstances"), is(3));
      assertThat(variables.getVariable("loopCounter"), notNullValue());
      assertThat(variables.getVariable("loopCounter"), is(1));
    });

    tcWaitAfter.handleMultiInstanceCallActivity().handle(2).verifyInput(variables -> {
      assertThat(variables.getVariable("nrOfActiveInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfActiveInstances"), is(1));
      assertThat(variables.getVariable("nrOfCompletedInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfCompletedInstances"), is(2));
      assertThat(variables.getVariable("nrOfInstances"), notNullValue());
      assertThat(variables.getVariable("nrOfInstances"), is(3));
      assertThat(variables.getVariable("loopCounter"), notNullValue());
      assertThat(variables.getVariable("loopCounter"), is(2));
    });

    tcWaitAfter.createExecutor().withBean("multiInstanceCallActivityMapping", new MultiInstanceCallActivityMapping())
      .verify(pi -> {
        pi.isNotEnded();
      })
      .execute();
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
