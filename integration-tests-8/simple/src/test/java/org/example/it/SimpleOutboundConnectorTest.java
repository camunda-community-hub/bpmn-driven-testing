package org.example.it;

import static com.google.common.truth.Truth.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.simpleoutboundconnector.TC_startEvent__endEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class SimpleOutboundConnectorTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  ZeebeTestEngine engine;

  @Test
  void testExecute() {
    tc.handleOutboundConnector()
        .verify(processInstanceAssert -> {
          processInstanceAssert.hasVariableWithValue("authentication", Map.of("type", "noAuth"));
          processInstanceAssert.hasVariableWithValue("method", "GET");
          processInstanceAssert.hasVariableWithValue("url", "https://example.org");
          processInstanceAssert.hasVariableWithValue("headers", null);
          processInstanceAssert.hasVariableWithValue("queryParameters", null);
          processInstanceAssert.hasVariableWithValue("connectionTimeoutInSeconds", "20");
        })
        .verifyInputMapping(inputMapping -> {
          assertThat(inputMapping).containsEntry("authentication.type", "noAuth");
          assertThat(inputMapping).containsEntry("method", "GET");
          assertThat(inputMapping).containsEntry("url", "=\"https://example.org\"");
          assertThat(inputMapping).containsEntry("headers", "=headers");
          assertThat(inputMapping).containsEntry("queryParameters", "=queryParameters");
          assertThat(inputMapping).containsEntry("connectionTimeoutInSeconds", "20");
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
          assertThat(taskHeaders).containsEntry("retries", "3");
          assertThat(taskHeaders).containsEntry("retryBackoff", "PT1H");
        });

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }
}
