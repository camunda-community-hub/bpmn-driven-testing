package org.example.it;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.receivetasktimer.TC_startEvent__endEvent;
import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
public class ReceiveTaskTimerTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  @Test
  void testExecute() {
    tc.handleTimerBoundaryEvent().execute(() -> processTestContext.increaseTime(Duration.ofHours(1)));

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }
}
