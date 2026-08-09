package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;

import org.camunda.community.bpmndt.api.TestCaseInstanceElement.ConditionalEventElement;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class ConditionalCatchEventTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  private ConditionalEventHandler handler;

  @BeforeEach
  void setUp() {
    var element = new ConditionalEventElement();
    element.id = "timerCatchEvent";
    element.condition = "x > 10";

    handler = new ConditionalEventHandler(element);
  }

  @Test
  void testExecute() {
    tc.createExecutor(client, processTestContext)
        .withVariable("x", 11)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  void testExecuteWithVariableMap() {
    handler.withVariableMap(Map.of("x", 11));

    tc.createExecutor(client, processTestContext)
        .withVariable("x", 10)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  void testExecuteWithCustomAction() {
    handler.execute((client, processInstanceKey) -> client.newSetVariablesCommand(processInstanceKey).variables(Map.of("x", 11)).execute());

    tc.createExecutor(client, processTestContext)
        .withVariable("x", 10)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  void testVerify() {
    handler.verify(processInstanceAssert -> processInstanceAssert.hasVariable("x", 11));

    tc.createExecutor(client, processTestContext)
        .withVariable("x", 11)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  void testVerifyConditionExpression() {
    handler.verifyConditionExpression(expr -> assertThat(expr).isEqualTo("wrong condition"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());

    handler.verifyConditionExpression(expr -> assertThat(expr).isEqualTo("x > 10"));

    tc.createExecutor(client, processTestContext)
        .withVariable("x", 11)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "startEvent");
      instance.isWaitingAt(processInstanceKey, "conditionalCatchEvent");
      instance.apply(processInstanceKey, handler);
      instance.hasPassed(processInstanceKey, "conditionalCatchEvent");
      instance.hasPassed(processInstanceKey, "endEvent");
    }

    @Override
    public String getBpmnProcessId() {
      return "simpleConditionalCatchEvent";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.simple("simpleConditionalCatchEvent.bpmn"));
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
