package org.example.it;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.engine.variable.VariableMap;

import generated.TC_simpleCallActivity__startEvent__endEvent;

public class SimpleCallActivityTest extends TC_simpleCallActivity__startEvent__endEvent {

  @Override
  protected String before(VariableMap variables) {
    Mocks.register("callActivityMapping", new CallActivityMapping());

    return null;
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

  @Override
  protected void after() {
    assertThatPi().isEnded();
  }
}
