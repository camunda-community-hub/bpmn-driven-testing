package org.example.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.simpleeventbasedgateway.TC_Message;
import generated.simpleeventbasedgateway.TC_Timer;
import generated.simpleeventbasedgateway.TC_startEvent__eventBasedGateway;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class SimpleEventBasedGatewayTest {

  @RegisterExtension
  TC_startEvent__eventBasedGateway tc = new TC_startEvent__eventBasedGateway();
  @RegisterExtension
  TC_Message tcMessage = new TC_Message();
  @RegisterExtension
  TC_Timer tcTimer = new TC_Timer();

  ZeebeTestEngine engine;

  @Test
  void testExecute() {
    tc.createExecutor(engine).verify(ProcessInstanceAssert::isNotCompleted).execute();
  }

  @Test
  void testExecuteMessage() {
    tcMessage.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testExecuteTimer() {
    tcTimer.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }
}
