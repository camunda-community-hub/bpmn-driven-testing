package org.example.it;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.simplesignalcatchevent.TC_startEvent__endEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class SimpleSignalCatchEventTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  ZeebeTestEngine engine;

  @Test
  void testExecute() {
    tc.handleSignalCatchEvent()
        .verifySignalName(signalName -> assertThat(signalName).isEqualTo("simpleSignal"))
        .verifySignalNameExpression(expr -> assertThat(expr).isEqualTo("=\"simpleSignal\""));

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }
}
