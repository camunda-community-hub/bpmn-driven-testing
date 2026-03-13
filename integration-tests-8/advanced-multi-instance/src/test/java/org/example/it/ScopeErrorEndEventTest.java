package org.example.it;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.scopeerrorendevent.TC_Error;
import generated.scopeerrorendevent.TC_None;
import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class ScopeErrorEndEventTest {

  @RegisterExtension
  TC_None tc = new TC_None();
  @RegisterExtension
  TC_Error tcError = new TC_Error();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  @Test
  void testExecute() {
    tc.createExecutor(client, processTestContext)
        .withVariable("elements", List.of(1, 2, 3))
        .withVariable("error", false)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  void testExecuteError() {
    tcError.createExecutor(client, processTestContext)
        .withVariable("elements", List.of(1, 2, 3))
        .withVariable("error", true)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }
}
