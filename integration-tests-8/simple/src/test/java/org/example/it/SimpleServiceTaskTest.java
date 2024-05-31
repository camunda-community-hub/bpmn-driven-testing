package org.example.it;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.simpleservicetask.TC_startEvent__endEvent;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class SimpleServiceTaskTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  ZeebeTestEngine engine;
  ZeebeClient client;

  @Test
  void testExecute() {
    tc.handleServiceTask()
        .verifyRetries(3)
        .verifyRetriesExpression(expr -> assertThat(expr).isEqualTo("=3"))
        .verifyType(type -> assertThat(type).isEqualTo("serviceTaskType"))
        .verifyTypeExpression(expr -> assertThat(expr).isEqualTo("=\"serviceTaskType\""));

    var workerBuilder = client.newWorker().jobType("serviceTaskType").handler((client, job) -> client.newCompleteCommand(job).send());

    try (var ignored = workerBuilder.open()) {
      tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
    }
  }
}
