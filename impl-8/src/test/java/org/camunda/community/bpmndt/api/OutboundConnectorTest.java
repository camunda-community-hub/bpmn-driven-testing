package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.camunda.community.bpmndt.api.TestCaseInstanceElement.OutboundConnectorElement;
import org.camunda.community.bpmndt.test.TestPaths;
import org.camunda.community.bpmndt.test.TestVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class OutboundConnectorTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  private OutboundConnectorHandler handler;

  @BeforeEach
  void setUp() {
    var element = new OutboundConnectorElement();
    element.id = "outboundConnector";
    element.inputs = Map.of(
        "authentication.type", "noAuth",
        "method", "GET",
        "url", "=\"https://example.org\"",
        "headers", "=headers",
        "queryParameters", "=queryParameters",
        "connectionTimeoutInSeconds", "20"
    );
    element.outputs = Map.of("x", "y");
    element.retries = "3";
    element.taskDefinitionType = "io.camunda:http-json:1";
    element.taskHeaders = Map.of(
        "resultVariable", "responseBody",
        "resultExpression", "={}",
        "errorExpression", "if error.code = \"400\" then\n"
            + "  bpmnError(\"400\", \"bad request\")\n"
            + "else\n"
            + "  null",
        "retryBackoff", "PT1H"
    );

    handler = new OutboundConnectorHandler(element);
  }

  @Test
  void testExecute() {
    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyInputMapping() {
    handler.verifyInputMapping(inputMapping -> assertThat(inputMapping).containsEntry("x", "y"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());

    handler.verifyInputMapping(inputMapping -> {
      assertThat(inputMapping).containsEntry("authentication.type", "noAuth");
      assertThat(inputMapping).containsEntry("method", "GET");
      assertThat(inputMapping).containsEntry("url", "=\"https://example.org\"");
      assertThat(inputMapping).containsEntry("headers", "=headers");
      assertThat(inputMapping).containsEntry("queryParameters", "=queryParameters");
      assertThat(inputMapping).containsEntry("connectionTimeoutInSeconds", "20");
    });

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyOutputMapping() {
    handler.verifyOutputMapping(outputMapping -> assertThat(outputMapping).containsEntry("a", "b"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());

    handler.verifyOutputMapping(outputMapping -> assertThat(outputMapping).containsEntry("x", "y"));

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyRetries() {
    handler.verifyRetries(2);

    var e = assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());
    assertThat(e).hasMessageThat().contains("but was 3");
    assertThat(e).hasMessageThat().contains("retry count of 2");

    handler.verifyRetries(3);

    handler.verifyRetries(retries -> assertThat(retries).isEqualTo(2));

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());

    handler.verifyRetries(retries -> assertThat(retries).isEqualTo(3));

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyRetriesExpression() {
    handler.verifyRetriesExpression(expr -> assertThat(expr).isEqualTo("wrong retries expression"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());

    handler.verifyRetriesExpression(expr -> assertThat(expr).isEqualTo("3"));

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyTaskDefinitionType() {
    handler.verifyTaskDefinitionType("wrong type");

    var e = assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());
    assertThat(e).hasMessageThat().contains("'wrong type'");
    assertThat(e).hasMessageThat().contains("'io.camunda:http-json:1'");

    handler.verifyTaskDefinitionType("io.camunda:http-json:1");

    handler.verifyTaskDefinitionType(type -> assertThat(type).isEqualTo("wrong type"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());

    handler.verifyTaskDefinitionType(type -> assertThat(type).isEqualTo("io.camunda:http-json:1"));

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyTaskHeaders() {
    handler.verifyTaskHeaders(taskHeaders -> assertThat(taskHeaders).containsEntry("resultVariable", null));

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());

    handler.verifyTaskHeaders(taskHeaders -> {
      assertThat(taskHeaders).containsEntry("resultVariable", "responseBody");
      assertThat(taskHeaders).containsEntry("resultExpression", "={}");
      assertThat(taskHeaders).containsKey("errorExpression");
      assertThat(taskHeaders.get("errorExpression")).contains("bpmnError(\"400\", \"bad request\")");
      assertThat(taskHeaders).containsEntry("retryBackoff", "PT1H");
    });

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testExecuteAction() {
    handler.execute((client, jobKey) -> client.newCompleteCommand(jobKey).send());

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testCompleteWithVariables() {
    var variables = new TestVariables();
    variables.setX("test");
    variables.setY(1);
    variables.setZ(true);

    handler.withVariables(variables).complete();

    tc.createExecutor(client, processTestContext).verify(piAssert -> {
      piAssert.isCompleted();

      piAssert.hasVariable("x", "test");
      piAssert.hasVariable("y", 1);
      piAssert.hasVariable("z", true);
    }).execute();
  }

  @Test
  void testCompleteWithVariableMap() {
    var variableMap = new HashMap<String, Object>();
    variableMap.put("y", 1);
    variableMap.put("z", true);

    handler
        .withVariable("x", "test")
        .withVariableMap(variableMap)
        .complete();

    tc.createExecutor(client, processTestContext).verify(piAssert -> {
      piAssert.isCompleted();

      piAssert.hasVariable("x", "test");
      piAssert.hasVariable("y", 1);
      piAssert.hasVariable("z", true);
    }).execute();
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "startEvent");
      instance.apply(processInstanceKey, handler);
      instance.hasPassed(processInstanceKey, "outboundConnector");
      instance.hasPassed(processInstanceKey, "endEvent");
      instance.isCompleted(processInstanceKey);
    }

    @Override
    public String getBpmnProcessId() {
      return "simpleOutboundConnector";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.simple("simpleOutboundConnector.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getStart() {
      return "startEvent";
    }

    @Override
    public String getEnd() {
      return "endEvent";
    }
  }
}
