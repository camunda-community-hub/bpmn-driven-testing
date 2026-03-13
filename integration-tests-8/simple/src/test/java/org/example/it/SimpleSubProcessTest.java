package org.example.it;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.simplesubprocess.TC_startEvent__endEvent;
import generated.simplesubprocess.TC_startEvent__subProcessEndEvent;
import generated.simplesubprocess.TC_subProcessStartEvent__endEvent;
import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class SimpleSubProcessTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();
  @RegisterExtension
  TC_startEvent__subProcessEndEvent tcSubProcessEndEvent = new TC_startEvent__subProcessEndEvent();
  @RegisterExtension
  TC_subProcessStartEvent__endEvent tcSubProcessStartEvent = new TC_subProcessStartEvent__endEvent();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  @Test
  void testExecute() {
    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testExecuteSubProcessEndEvent() {
    tcSubProcessEndEvent.createExecutor(client, processTestContext).execute();
  }

  // currently not supported
  // element with id 'subProcessStartEvent' targets unsupported element type 'START_EVENT'
  @Test
  @Disabled
  void testExecuteSubProcessStartEvent() {
    tcSubProcessStartEvent.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }
}

