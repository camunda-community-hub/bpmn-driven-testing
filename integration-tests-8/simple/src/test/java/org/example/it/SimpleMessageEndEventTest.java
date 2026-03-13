package org.example.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.simplemessageendevent.TC_startEvent__messageEndEvent;
import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class SimpleMessageEndEventTest {

  @RegisterExtension
  TC_startEvent__messageEndEvent tc = new TC_startEvent__messageEndEvent();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  @Test
  void testExecute() {
    var workerBuilder = client.newWorker().jobType("messageEndEventType").handler((client, job) ->
        client.newCompleteCommand(job).send()
    );

    try (var ignored = workerBuilder.open()) {
      tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
    }
  }
}
