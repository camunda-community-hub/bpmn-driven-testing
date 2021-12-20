package org.example.it;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.variable.VariableMap;
import org.junit.Rule;
import org.junit.Test;

import generated.simplecallactivity.TC_startEvent__callActivity;
import generated.simplecallactivity.TC_startEvent__endEvent;

public class SimpleCallActivityTest {

  @Rule
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();
  @Rule
  public TC_startEvent__callActivity tcWaitAfter = new TC_startEvent__callActivity();

  @Test
  public void testExecute() {
    tc.createExecutor()
      .withBean("callActivityMapping", new CallActivityMapping())
      .verify(pi -> {
        pi.isEnded();
      })
      .execute();
  }

  @Test
  public void testExecuteAndWaitAfter() {
    tcWaitAfter.createExecutor()
      .withBean("callActivityMapping", new CallActivityMapping())
      .verify(pi -> {
        pi.isNotEnded();
      })
      .execute();
  }

  /**
   * Normally a delegate implementation from src/main/java
   */
  private class CallActivityMapping implements DelegateVariableMapping {

    @Override
    public void mapInputVariables(DelegateExecution superExecution, VariableMap subVariables) {
      // empty implementation
    }

    @Override
    public void mapOutputVariables(DelegateExecution superExecution, VariableScope subInstance) {
      // empty implementation
    }
  }
}
