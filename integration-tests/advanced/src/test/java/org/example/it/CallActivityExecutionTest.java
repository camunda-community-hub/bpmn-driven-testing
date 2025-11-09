package org.example.it;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.bpm.engine.variable.VariableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.callactivityexecution1.TC_startEvent__endEvent;
import generated.callactivityexecution1.TC_startEvent__errorEndEvent;
import generated.callactivityexecution1.TC_startEvent__escalationEndEvent;
import generated.callactivityexecution1.TC_startEvent__messageEndEvent;

public class CallActivityExecutionTest {

  @RegisterExtension
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();
  @RegisterExtension
  public TC_startEvent__escalationEndEvent tcEscalation = new TC_startEvent__escalationEndEvent();
  @RegisterExtension
  public TC_startEvent__errorEndEvent tcError = new TC_startEvent__errorEndEvent();
  @RegisterExtension
  public TC_startEvent__messageEndEvent tcMessage = new TC_startEvent__messageEndEvent();

  @Test
  public void testExecute() {
    tc.handleCallActivity().executeTestCase(new generated.callactivityexecution2.TC_startEvent__endEvent(), it -> {
      it.handleCallActivityA().executeTestCase(new generated.callactivityexecution3.TC_startEvent__endEvent(), null);
    });

    tc.createExecutor()
        .withVariable("end", "none")
        .withBean("callActivityMapping", new CallActivityMapping())
        .verify(ProcessInstanceAssert::isEnded)
        .execute();
  }

  @Test
  public void testExecuteEscalation() {
    tcEscalation.handleCallActivity().executeTestCase(new generated.callactivityexecution2.TC_startEvent__escalationEndEvent(), null);

    tcEscalation.createExecutor()
        .withVariable("end", "escalation")
        .withBean("callActivityMapping", new CallActivityMapping())
        .verify(ProcessInstanceAssert::isEnded)
        .execute();
  }

  @Test
  public void testExecuteError() {
    tcError.handleCallActivity().executeTestCase(new generated.callactivityexecution2.TC_startEvent__errorEndEvent(), null);

    tcError.createExecutor()
        .withVariable("end", "error")
        .withBean("callActivityMapping", new CallActivityMapping())
        .verify(ProcessInstanceAssert::isEnded)
        .execute();
  }

  @Test
  public void testExecuteMessage() {
    tcMessage.handleCallActivity().executeTestCase(new generated.callactivityexecution2.TC_startEvent__callActivityB(), null);

    tcMessage.createExecutor()
        .withBean("callActivityMapping", new CallActivityMapping())
        .verify(ProcessInstanceAssert::isEnded)
        .execute();
  }

  /**
   * Normally a delegate implementation from src/main/java
   */
  private static class CallActivityMapping implements DelegateVariableMapping {

    @Override
    public void mapInputVariables(DelegateExecution superExecution, VariableMap subVariables) {
      subVariables.put("end", superExecution.getVariable("end"));
    }

    @Override
    public void mapOutputVariables(DelegateExecution superExecution, VariableScope subInstance) {
      // empty implementation
    }
  }
}
