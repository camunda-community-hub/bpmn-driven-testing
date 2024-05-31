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

import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class OutboundConnectorTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;

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
    element.taskDefinitionType = "io.camunda:http-json:1";
    element.taskHeaders = Map.of(
        "resultVariable", "responseBody",
        "resultExpression", "={}",
        "errorExpression", "if error.code = \"400\" then\n"
            + "  bpmnError(\"400\", \"bad request\")\n"
            + "else\n"
            + "  null",
        "retries", "3",
        "retryBackoff", "PT1H"
    );

    handler = new OutboundConnectorHandler(element);
  }

  @Test
  void testExecute() {
    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerify() {
    handler.verify(processInstanceAssert -> {
      processInstanceAssert.hasVariableWithValue("authentication", Map.of("type", "noAuth"));
      processInstanceAssert.hasVariableWithValue("method", "GET");
      processInstanceAssert.hasVariableWithValue("url", "https://example.org");
      processInstanceAssert.hasVariableWithValue("headers", null);
      processInstanceAssert.hasVariableWithValue("queryParameters", null);
      processInstanceAssert.hasVariableWithValue("connectionTimeoutInSeconds", "20");
    });

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyInputMapping() {
    handler.verifyInputMapping(inputMapping -> assertThat(inputMapping).containsEntry("x", "y"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyInputMapping(inputMapping -> {
      assertThat(inputMapping).containsEntry("authentication.type", "noAuth");
      assertThat(inputMapping).containsEntry("method", "GET");
      assertThat(inputMapping).containsEntry("url", "=\"https://example.org\"");
      assertThat(inputMapping).containsEntry("headers", "=headers");
      assertThat(inputMapping).containsEntry("queryParameters", "=queryParameters");
      assertThat(inputMapping).containsEntry("connectionTimeoutInSeconds", "20");
    });

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyOutputMapping() {
    handler.verifyOutputMapping(outputMapping -> assertThat(outputMapping).containsEntry("a", "b"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyOutputMapping(outputMapping -> assertThat(outputMapping).containsEntry("x", "y"));

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyRetries() {
    handler.verifyRetries(2);

    var e = assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());
    assertThat(e).hasMessageThat().contains("but was 3");
    assertThat(e).hasMessageThat().contains("retry count of 2");

    handler.verifyRetries(3);

    handler.verifyRetries(retries -> assertThat(retries).isEqualTo(2));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyRetries(retries -> assertThat(retries).isEqualTo(3));

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyTaskDefinitionType() {
    handler.verifyTaskDefinitionType("wrong type");

    var e = assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());
    assertThat(e).hasMessageThat().contains("'wrong type'");
    assertThat(e).hasMessageThat().contains("'io.camunda:http-json:1'");

    handler.verifyTaskDefinitionType("io.camunda:http-json:1");

    handler.verifyTaskDefinitionType(type -> assertThat(type).isEqualTo("wrong type"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyTaskDefinitionType(type -> assertThat(type).isEqualTo("io.camunda:http-json:1"));

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyTaskHeaders() {
    handler.verifyTaskHeaders(taskHeaders -> assertThat(taskHeaders).containsEntry("resultVariable", null));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyTaskHeaders(taskHeaders -> {
      assertThat(taskHeaders).containsEntry("resultVariable", "responseBody");
      assertThat(taskHeaders).containsEntry("resultExpression", "={}");
      assertThat(taskHeaders).containsKey("errorExpression");
      assertThat(taskHeaders.get("errorExpression")).contains("bpmnError(\"400\", \"bad request\")");
      assertThat(taskHeaders).containsEntry("retries", "3");
      assertThat(taskHeaders).containsEntry("retryBackoff", "PT1H");
    });

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testExecuteAction() {
    handler.execute((client, jobKey) -> client.newCompleteCommand(jobKey).send());

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testCompleteWithVariables() {
    var variables = new TestVariables();
    variables.setX("test");
    variables.setY(1);
    variables.setZ(true);

    handler.withVariables(variables).complete();

    tc.createExecutor(engine).verify(piAssert -> {
      piAssert.isCompleted();

      piAssert.hasVariableWithValue("x", "test");
      piAssert.hasVariableWithValue("y", 1);
      piAssert.hasVariableWithValue("z", true);
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

    tc.createExecutor(engine).verify(piAssert -> {
      piAssert.isCompleted();

      piAssert.hasVariableWithValue("x", "test");
      piAssert.hasVariableWithValue("y", 1);
      piAssert.hasVariableWithValue("z", true);
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
