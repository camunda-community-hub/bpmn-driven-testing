package org.example.it;

import static com.google.common.truth.Truth.assertThat;

import org.camunda.community.bpmndt.api.CallActivityBindingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.simplecallactivity.TC_startEvent__endEvent;
import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class SimpleCallActivityTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  @Test
  void testExecute() {
    tc.handleCallActivity()
        .verifyBindingType(CallActivityBindingType.LATEST)
        .verifyBindingType(bindingType -> assertThat(bindingType).isEqualTo(CallActivityBindingType.LATEST))
        .verifyProcessId("simple")
        .verifyProcessIdExpression(expr -> assertThat(expr).isEqualTo("=\"simple\""))
        .verifyPropagateAllChildVariables(true)
        .verifyPropagateAllParentVariables(true);

    tc.createExecutor(client, processTestContext).simulateProcess("simple").verify(ProcessInstanceAssert::isCompleted).execute();
  }
}
