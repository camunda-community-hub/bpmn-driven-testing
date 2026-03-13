package org.example.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.simplesubprocessnested.TC_startEvent__endEvent;
import generated.simplesubprocessnested.TC_userTask__endEvent;
import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class SimpleSubProcessNestedTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();
  @RegisterExtension
  TC_userTask__endEvent tcUserTask = new TC_userTask__endEvent();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  @Test
  void testExecute() {
    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testExecuteUserTask() {
    tcUserTask.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }
}
