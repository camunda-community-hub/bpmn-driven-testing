package org.example.it;

import static com.google.common.truth.Truth.assertThat;

import org.camunda.community.bpmndt.api.CallActivityBindingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.simplecallactivity.TC_startEvent__endEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class SimpleCallActivityTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  ZeebeTestEngine engine;

  @Test
  void testExecute() {
    tc.handleCallActivity()
        .verifyBindingType(CallActivityBindingType.LATEST)
        .verifyBindingType(bindingType -> assertThat(bindingType).isEqualTo(CallActivityBindingType.LATEST))
        .verifyProcessId("simple")
        .verifyProcessIdExpression(expr -> assertThat(expr).isEqualTo("=\"simple\""))
        .verifyPropagateAllChildVariables(true)
        .verifyPropagateAllParentVariables(true);

    tc.createExecutor(engine).simulateProcess("simple").verify(ProcessInstanceAssert::isCompleted).execute();
  }
}
