package org.example.it;

import org.camunda.community.bpmndt.api.CallActivityBindingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.callactivitybindingdeployment.TC_startEvent__endEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class CallActivityBindingDeploymentTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  ZeebeTestEngine engine;

  @Test
  void testExecute() {
    tc.handleCallActivity().verifyBindingType(CallActivityBindingType.DEPLOYMENT);

    tc.createExecutor(engine).simulateProcess("advanced").verify(ProcessInstanceAssert::isCompleted).execute();
  }
}
