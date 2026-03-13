package org.example.it;

import java.util.List;

import org.camunda.community.bpmndt.api.UserTaskHandler;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.usertaskerror.TC_startEvent__endEvent;
import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class UserTaskErrorTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  private int loopCount;

  @Disabled // currently not supported
  @Test
  void testExecute() {
    var elements = List.of(1, 2, 3);

    var userTaskHandler = new UserTaskHandler("userTask");

    tc.handleUserTask().verifyLoopCount(3).executeLoop((instance, elementInstanceKey) -> {
      var flowScopeKey = instance.getFlowScopeKey(elementInstanceKey);

      if (loopCount == 2) {
        userTaskHandler.throwBpmnError("ERROR_CODE", "test error message");

        instance.apply(flowScopeKey, userTaskHandler);
        instance.hasTerminated(flowScopeKey, "userTask");
      } else {
        instance.apply(flowScopeKey, userTaskHandler);
      }

      loopCount++;
    });

    tc.createExecutor(client, processTestContext)
        .withVariable("elements", elements)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }
}
