package org.example.it;

import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.scopeerrorendevent.TC_Error;
import generated.scopeerrorendevent.TC_None;

public class ScopeErrorEndEventTest {

  @RegisterExtension
  public TC_Error tcError = new TC_Error();
  @RegisterExtension
  public TC_None tcNone = new TC_None();

  @Test
  public void testExecuteError() {
    tcError.handleSubProcess().verifyLoopCount(1);

    tcError.createExecutor().withVariable("error", true).execute();
  }

  @Test
  public void testExecuteErrorWithCustomAction() {
    tcError.handleSubProcess().verifyLoopCount(1);
    tcError.handleSubProcess().execute(0, this::executeSubProcessEndEvent);

    tcError.createExecutor().withVariable("error", true).execute();
  }

  @Test
  public void testExecuteNone() {
    tcNone.handleSubProcess().verifyLoopCount(2);

    tcNone.createExecutor().withVariable("error", false).execute();
  }

  private Boolean executeSubProcessEndEvent(ProcessInstanceAssert pi, Integer loopIndex) {
    pi.hasPassed("subProcessStartEvent");
    pi.hasPassed("fork");
    pi.hasPassed("subProcessErrorEndEvent");

    return Boolean.FALSE;
  }
}
