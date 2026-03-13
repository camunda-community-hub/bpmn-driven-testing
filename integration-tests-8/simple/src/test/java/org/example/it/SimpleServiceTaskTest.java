package org.example.it;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.simpleservicetask.TC_startEvent__endEvent;
import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class SimpleServiceTaskTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  @Test
  void testExecute() {
    tc.handleServiceTask()
        .verifyRetries(3)
        .verifyRetriesExpression(expr -> assertThat(expr).isEqualTo("=3"))
        .verifyType(type -> assertThat(type).isEqualTo("serviceTaskType"))
        .verifyTypeExpression(expr -> assertThat(expr).isEqualTo("=\"serviceTaskType\""));

    var workerBuilder = client.newWorker().jobType("serviceTaskType").handler((client, job) -> client.newCompleteCommand(job).send());

    try (var ignored = workerBuilder.open()) {
      tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
    }
  }
}
