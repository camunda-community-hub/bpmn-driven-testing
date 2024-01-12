package org.example.it;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.variable.VariableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.scopeparallel.TC_startEvent__endEvent;

public class ScopeParallelTest {

  @RegisterExtension
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.handleMultiInstanceScope().verifyParallel();
    tc.handleMultiInstanceScope().verifyLoopCount(3);

    tc.createExecutor().withBean("callActivityMapping", new CallActivityMapping()).execute();
  }

  @Test
  public void testCallActivityHandlers() {
    tc.handleMultiInstanceScope().handleCallActivity().verifyInput(variables -> {
      variables.setVariable("test", 0);
    });

    tc.handleMultiInstanceScope().handleCallActivity(1).verify((pi, __) -> {
      pi.variables().containsEntry("test", 0);
    }).verifyInput(variables -> {
      variables.setVariable("test", 1);
    });

    tc.handleMultiInstanceScope().handleCallActivity(2).verify((pi, __) -> {
      pi.variables().containsEntry("test", 1);
    }).verifyInput(variables -> {
      variables.setVariable("test", 2);
    });

    tc.createExecutor().withBean("callActivityMapping", new CallActivityMapping()).verify(pi -> {
      pi.variables().containsEntry("test", 2);

      pi.isEnded();
    }).execute();
  }

  private static class CallActivityMapping implements DelegateVariableMapping {

    @Override
    public void mapInputVariables(DelegateExecution superExecution, VariableMap subVariables) {
      // nothing to do here
    }

    @Override
    public void mapOutputVariables(DelegateExecution superExecution, VariableScope subInstance) {
      superExecution.setVariable("test", subInstance.getVariable("test"));
    }
  }
}
