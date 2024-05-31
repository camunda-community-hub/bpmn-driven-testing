package org.example.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.simplemessageendevent.TC_startEvent__messageEndEvent;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class SimpleMessageEndEventTest {

  @RegisterExtension
  TC_startEvent__messageEndEvent tc = new TC_startEvent__messageEndEvent();

  ZeebeTestEngine engine;
  ZeebeClient client;

  @Test
  void testExecute() {
    var workerBuilder = client.newWorker().jobType("messageEndEventType").handler((client, job) ->
        client.newCompleteCommand(job).send()
    );

    try (var ignored = workerBuilder.open()) {
      tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
    }
  }
}
