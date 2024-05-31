package org.example.it;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.simplemessagecatchevent.TC_startEvent__endEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class SimpleMessageCatchEventTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  ZeebeTestEngine engine;

  @Test
  void testExecute() {
    tc.handleMessageCatchEvent()
        .verifyCorrelationKey(correlationKey -> assertThat(correlationKey).isEqualTo("simple"))
        .verifyCorrelationKeyExpression(expr -> assertThat(expr).isEqualTo("=\"simple\""))
        .verifyMessageName(messageName -> assertThat(messageName).isEqualTo("simpleMessage"))
        .verifyMessageNameExpression(expr -> assertThat(expr).isEqualTo("=\"simpleMessage\""));

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }
}
