package org.example.it;

import java.util.List;

import org.camunda.community.bpmndt.api.CustomMultiInstanceHandler;
import org.camunda.community.bpmndt.api.UserTaskHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.scopenested.TC_startEvent__endEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class ScopeNestedTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  ZeebeTestEngine engine;

  @Test
  void testExecute() {
    var elements = List.of(1, 2, 3);
    var nestedElements = List.of(4, 5);

    tc.handleSubProcess().execute((testCaseInstance, processInstanceKey) -> {
      var nestedSubProcessHandler = new CustomMultiInstanceHandler("nestedSubProcess");

      nestedSubProcessHandler.execute((__, ___) -> {
        var userTaskHandler = new UserTaskHandler("userTask");

        for (int i = 0; i < nestedElements.size(); i++) {
          testCaseInstance.apply(processInstanceKey, userTaskHandler);
        }
      });

      for (int i = 0; i < elements.size(); i++) {
        testCaseInstance.apply(processInstanceKey, nestedSubProcessHandler);
      }
    });

    tc.createExecutor(engine)
        .simulateProcess("advanced")
        .withVariable("elements", elements)
        .withVariable("nestedElements", nestedElements)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }
}
