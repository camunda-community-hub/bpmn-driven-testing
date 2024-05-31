package org.example.it;

import java.util.List;

import org.camunda.community.bpmndt.api.UserTaskHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.usertaskerror.TC_startEvent__endEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class UserTaskErrorTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  ZeebeTestEngine engine;

  @Test
  void testExecute() {
    var elements = List.of(1, 2, 3);

    var userTaskHandler = new UserTaskHandler("userTask");

    tc.handleUserTask().execute((testCaseInstance, processInstanceKey) -> {
      for (int i = 0; i < elements.size(); i++) {
        if (i == 2) {
          userTaskHandler.throwBpmnError("ERROR_CODE", "test error message");

          testCaseInstance.apply(processInstanceKey, userTaskHandler);
          testCaseInstance.hasTerminated(processInstanceKey, "userTask");
        } else {
          testCaseInstance.apply(processInstanceKey, userTaskHandler);
        }
      }
    });

    tc.createExecutor(engine)
        .withVariable("elements", elements)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }
}
