package org.example.it;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.scopemessageboundary.TC_startEvent__endEvent;
import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class ScopeMessageBoundaryTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  @Test
  void testExecute() {
    tc.handleSubProcess().verifyLoopCount(1).verifySequential().executeLoop((testCaseInstance, flowScopeKey) -> {
      testCaseInstance.apply(flowScopeKey, tc.handleMessageBoundaryEvent());
    });

    tc.createExecutor(client, processTestContext)
        .withVariable("correlationKey", String.valueOf(System.currentTimeMillis()))
        .withVariable("elements", List.of(1, 2, 3))
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }
}
