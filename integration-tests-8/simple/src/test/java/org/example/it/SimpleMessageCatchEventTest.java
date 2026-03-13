package org.example.it;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.simplemessagecatchevent.TC_startEvent__endEvent;
import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class SimpleMessageCatchEventTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  @Test
  void testExecute() {
    var correlationKey = String.valueOf(System.currentTimeMillis());

    tc.handleMessageCatchEvent()
        .verifyCorrelationKey(value -> assertThat(value).isEqualTo(correlationKey))
        .verifyCorrelationKeyExpression(expr -> assertThat(expr).isEqualTo("=correlationKey"))
        .verifyMessageName(messageName -> assertThat(messageName).isEqualTo("simpleMessage"))
        .verifyMessageNameExpression(expr -> assertThat(expr).isEqualTo("=\"simpleMessage\""));

    tc.createExecutor(client, processTestContext)
        .withVariable("correlationKey", correlationKey)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }
}
