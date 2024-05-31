package org.example.it;

import java.util.List;

import org.camunda.community.bpmndt.api.CallActivityHandler;
import org.camunda.community.bpmndt.api.JobHandler;
import org.camunda.community.bpmndt.api.MessageEventHandler;
import org.camunda.community.bpmndt.api.UserTaskHandler;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.scopeparallel.TC_startEvent__endEvent;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class ScopeParallelTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  ZeebeTestEngine engine;
  ZeebeClient client;

  // currently not supported, because parallel events for the same element ID are not supported
  @Disabled
  @Test
  void testExecute() {
    var elements = List.of(1, 2, 3);

    tc.handleMultiInstanceScope().verifyParallel().execute((testCaseInstance, processInstanceKey) -> {
      var userTaskHandler = new UserTaskHandler("userTask");
      var messageCatchEventHandler = new MessageEventHandler("messageCatchEvent");
      var serviceTaskHandler = new JobHandler("serviceTask");

      var callActivityHandler = new CallActivityHandler("callActivity");

      serviceTaskHandler.execute((client, jobKey) -> {
        // nothing to do here, since the jobs are completed by a worker
      });

      for (int i = 0; i < elements.size(); i++) {
        testCaseInstance.apply(processInstanceKey, userTaskHandler);
        testCaseInstance.apply(processInstanceKey, messageCatchEventHandler);

        testCaseInstance.apply(processInstanceKey, serviceTaskHandler);
        testCaseInstance.hasPassed(processInstanceKey, "serviceTask");

        testCaseInstance.apply(processInstanceKey, callActivityHandler);
      }
    });

    var workerBuilder = client.newWorker()
        .jobType("advanced")
        .handler((client, job) -> client.newCompleteCommand(job).send());

    try (var ignored = workerBuilder.open()) {
      tc.createExecutor(engine)
          .simulateProcess("advanced")
          .withVariable("elements", elements)
          .verify(ProcessInstanceAssert::isCompleted)
          .execute();
    }
  }
}
