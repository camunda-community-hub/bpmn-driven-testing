package org.example.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.linkevent.TC_forkA__linkCatchEventA;
import generated.linkevent.TC_forkB__linkCatchEventB;
import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
public class LinkEventTest {

  @RegisterExtension
  TC_forkA__linkCatchEventA tcA = new TC_forkA__linkCatchEventA();
  @RegisterExtension
  TC_forkB__linkCatchEventB tcB = new TC_forkB__linkCatchEventB();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  @Test
  void testExecuteA() {
    tcA.createExecutor(client, processTestContext)
        .withVariable("forkA", true)
        .withVariable("forkB", false)
        .verify(ProcessInstanceAssert::isActive)
        .execute();
  }

  @Test
  void testExecuteB() {
    tcB.createExecutor(client, processTestContext)
        .withVariable("forkA", false)
        .withVariable("forkB", true)
        .verify(ProcessInstanceAssert::isActive)
        .execute();
  }
}
