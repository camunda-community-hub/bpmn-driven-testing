package org.example.it;

import static com.google.common.truth.Truth.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.simpleconditionalcatchevent.TC_startEvent__endEvent;
import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class SimpleConditionalCatchEventTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  @Test
  void testExecute() {
    tc.handleConditionalCatchEvent()
        .verifyConditionExpression(expr -> assertThat(expr).isEqualTo("=x > 10"))
        .withVariableMap(Map.of("x", 11));

    tc.createExecutor(client, processTestContext)
        .withVariable("x", 10)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }
}
