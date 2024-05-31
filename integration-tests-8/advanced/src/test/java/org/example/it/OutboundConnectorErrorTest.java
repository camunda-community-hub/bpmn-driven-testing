package org.example.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.outboundconnectorerror.TC_startEvent__endEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
public class OutboundConnectorErrorTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  ZeebeTestEngine engine;

  @Test
  void testExecute() {
    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testExecuteAction() {
    tc.handleOutboundConnector().execute((client, jobKey) -> client.newThrowErrorCommand(jobKey).errorCode("ADVANCED_ERROR").send());

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testThrowBpmnError() {
    tc.handleOutboundConnector().throwBpmnError("ADVANCED_ERROR", "test error message");

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }
}
