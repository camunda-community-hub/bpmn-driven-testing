package org.example.it;

import java.util.List;

import org.camunda.community.bpmndt.api.CallActivityHandler;
import org.camunda.community.bpmndt.api.JobHandler;
import org.camunda.community.bpmndt.api.MessageEventHandler;
import org.camunda.community.bpmndt.api.UserTaskHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.scopeparallel.TC_startEvent__endEvent;
import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class ScopeParallelTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  @Test
  void testExecute() {
    var elements = List.of(1, 2, 3);

    tc.handleMultiInstanceScope().verifyLoopCount(3).verifyParallel().executeLoop((testCaseInstance, flowScopeKey) -> {
      var userTaskHandler = new UserTaskHandler("userTask");
      var messageCatchEventHandler = new MessageEventHandler("messageCatchEvent");
      var serviceTaskHandler = new JobHandler("serviceTask");
      var callActivityHandler = new CallActivityHandler("callActivity");

      testCaseInstance.apply(flowScopeKey, userTaskHandler);
      testCaseInstance.apply(flowScopeKey, messageCatchEventHandler);

      var workerBuilder = client.newWorker()
          .jobType("advanced")
          .handler((client, job) -> client.newCompleteCommand(job).send());

      try (var ignored = workerBuilder.open()) {
        testCaseInstance.apply(flowScopeKey, serviceTaskHandler);
        testCaseInstance.hasPassed(flowScopeKey, "serviceTask");
      }

      testCaseInstance.apply(flowScopeKey, callActivityHandler);
    });

    var workerBuilder = client.newWorker()
        .jobType("advanced")
        .handler((client, job) -> client.newCompleteCommand(job).send());

    try (var ignored = workerBuilder.open()) {
      tc.createExecutor(client, processTestContext)
          .simulateProcess("advanced")
          .withVariable("elements", elements)
          .verify(ProcessInstanceAssert::isCompleted)
          .execute();
    }
  }
}
