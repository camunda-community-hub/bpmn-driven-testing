package org.example.it;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.simpleoutboundconnector.TC_startEvent__endEvent;
import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class SimpleOutboundConnectorTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  @Test
  void testExecute() {
    tc.handleOutboundConnector()
        .verifyInputMapping(inputMapping -> {
          assertThat(inputMapping).containsEntry("authentication.type", "noAuth");
          assertThat(inputMapping).containsEntry("method", "GET");
          assertThat(inputMapping).containsEntry("url", "=\"https://example.org\"");
          assertThat(inputMapping).containsEntry("headers", "=headers");
          assertThat(inputMapping).containsEntry("queryParameters", "=queryParameters");
          assertThat(inputMapping).containsEntry("connectionTimeoutInSeconds", "=20");
        })
        .verifyOutputMapping(outputMapping -> assertThat(outputMapping).isNull())
        .verifyRetries(3)
        .verifyRetries(retries -> assertThat(retries).isEqualTo(3))
        .verifyTaskDefinitionType("io.camunda:http-json:1")
        .verifyTaskDefinitionType(type -> assertThat(type).isEqualTo("io.camunda:http-json:1"))
        .verifyTaskHeaders(taskHeaders -> {
          assertThat(taskHeaders).containsEntry("resultVariable", "responseBody");
          assertThat(taskHeaders).containsEntry("resultExpression", "={}");
          assertThat(taskHeaders).containsKey("errorExpression");
          assertThat(taskHeaders.get("errorExpression")).contains("bpmnError(\"400\", \"bad request\")");
          assertThat(taskHeaders).containsEntry("retryBackoff", "PT1H");
        });

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }
}
