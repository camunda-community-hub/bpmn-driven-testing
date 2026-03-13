package org.example.it;

import java.util.List;

import org.camunda.community.bpmndt.api.CustomMultiInstanceHandler;
import org.camunda.community.bpmndt.api.UserTaskHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.scopenested.TC_startEvent__endEvent;
import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class ScopeNestedTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  @Test
  void testExecute() {
    var elements = List.of(1, 2, 3);
    var nestedElements = List.of(4, 5);

    tc.handleSubProcess().verifyLoopCount(3).executeLoop((testCaseInstance, subProcessKey) -> {
      var nestedSubProcessHandler = new CustomMultiInstanceHandler("nestedSubProcess");

      nestedSubProcessHandler.verifyLoopCount(2).executeLoop((__, nestedSubProcessKey) -> {
        var userTaskHandler = new UserTaskHandler("userTask");

        testCaseInstance.apply(nestedSubProcessKey, userTaskHandler);
      });

      testCaseInstance.apply(subProcessKey, nestedSubProcessHandler);
    });

    tc.createExecutor(client, processTestContext)
        .simulateProcess("advanced")
        .withVariable("elements", elements)
        .withVariable("nestedElements", nestedElements)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }
}
