package org.example.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.simpleeventbasedgateway.TC_Message;
import generated.simpleeventbasedgateway.TC_Timer;
import generated.simpleeventbasedgateway.TC_startEvent__eventBasedGateway;
import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class SimpleEventBasedGatewayTest {

  @RegisterExtension
  TC_startEvent__eventBasedGateway tc = new TC_startEvent__eventBasedGateway();
  @RegisterExtension
  TC_Message tcMessage = new TC_Message();
  @RegisterExtension
  TC_Timer tcTimer = new TC_Timer();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  @Test
  void testExecute() {
    tc.createExecutor(client, processTestContext)
        .withVariable("correlationKey", String.valueOf(System.currentTimeMillis()))
        .verify(ProcessInstanceAssert::isActive)
        .execute();
  }

  @Test
  void testExecuteMessage() {
    tcMessage.createExecutor(client, processTestContext)
        .withVariable("correlationKey", String.valueOf(System.currentTimeMillis()))
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  void testExecuteTimer() {
    tcTimer.createExecutor(client, processTestContext)
        .withVariable("correlationKey", String.valueOf(System.currentTimeMillis()))
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }
}
