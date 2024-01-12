package org.example.it;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.servicetask.TC_startEvent__doA;

public class ServiceTaskTest {

  @RegisterExtension
  public TC_startEvent__doA tc = new TC_startEvent__doA();

  @Test
  public void testExecute() {
    tc.createExecutor()
      .withBean("doA", new DoA())
      .verify(pi -> {
        pi.variables().containsEntry("aDone", true);
        
        pi.isNotEnded();
      }).execute();
  }

  private static class DoA implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
      execution.setVariable("aDone", true);
    }
  }
}
