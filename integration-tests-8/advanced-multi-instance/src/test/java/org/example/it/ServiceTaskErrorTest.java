package org.example.it;

import java.util.List;

import org.camunda.community.bpmndt.api.JobHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.servicetaskerror.TC_startEvent__endEvent;
import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class ServiceTaskErrorTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  private int loopCount;

  @Test
  void testExecute() {
    var elements = List.of(1, 2, 3);

    var serviceTaskHandler = new JobHandler("serviceTask");

    tc.handleServiceTask().verifyLoopCount(3).executeLoop((instance, elementInstanceKey) -> {
      var flowScopeKey = instance.getFlowScopeKey(elementInstanceKey);

      if (loopCount == 2) {
        serviceTaskHandler.throwBpmnError("ERROR_CODE", "test error message");

        instance.apply(flowScopeKey, serviceTaskHandler);
        instance.hasTerminated(flowScopeKey, "serviceTask");
      } else {
        serviceTaskHandler.complete();
        instance.apply(flowScopeKey, serviceTaskHandler);
      }

      loopCount++;
    });

    tc.createExecutor(client, processTestContext)
        .withVariable("elements", elements)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }
}
