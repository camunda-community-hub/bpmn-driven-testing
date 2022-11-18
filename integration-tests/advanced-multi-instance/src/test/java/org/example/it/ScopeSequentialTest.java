package org.example.it;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.community.bpmndt.api.TestCaseExecutor;
import org.junit.Rule;
import org.junit.Test;

import generated.scopesequential.TC_startEvent__endEvent;

public class ScopeSequentialTest {

  @Rule
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.handleMultiInstanceScope().verifySequential();
    tc.handleMultiInstanceScope().verifyLoopCount(3);

    tc.createExecutor().customize(this::addBeans).execute();
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

    tc.createExecutor().customize(this::addBeans).verify(pi -> {
      pi.variables().containsEntry("test", 2);

      pi.isEnded();
    }).execute();
  }

  private void addBeans(TestCaseExecutor executor) {
    executor.withBean("serviceTask", new ServiceTask());
    executor.withBean("callActivityMapping", new CallActivityMapping());
  }

  private static class ServiceTask implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
      // nothing to do here
    }
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
