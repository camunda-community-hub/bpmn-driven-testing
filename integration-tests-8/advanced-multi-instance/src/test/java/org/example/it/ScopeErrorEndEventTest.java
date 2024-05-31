package org.example.it;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.scopeerrorendevent.TC_Error;
import generated.scopeerrorendevent.TC_None;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class ScopeErrorEndEventTest {

  @RegisterExtension
  TC_None tc = new TC_None();
  @RegisterExtension
  TC_Error tcError = new TC_Error();

  ZeebeTestEngine engine;

  @Test
  void testExecute() {
    tc.createExecutor(engine)
        .withVariable("elements", List.of(1, 2, 3))
        .withVariable("error", false)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  void testExecuteError() {
    tcError.createExecutor(engine)
        .withVariable("elements", List.of(1, 2, 3))
        .withVariable("error", true)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }
}
