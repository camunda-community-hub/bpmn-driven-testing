package org.example.it;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.simplesignalcatchevent.TC_startEvent__endEvent;
import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class SimpleSignalCatchEventTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  @Test
  void testExecute() {
    var correlationKey = String.valueOf(System.currentTimeMillis());

    tc.handleSignalCatchEvent()
        .verifySignalName(signalName -> assertThat(signalName).isEqualTo("simpleSignal"))
        .verifySignalNameExpression(expr -> assertThat(expr).isEqualTo("=\"simpleSignal\""))
        .execute((client, ignored) -> client.newBroadcastSignalCommand().signalName("simpleSignal").execute());

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }
}
