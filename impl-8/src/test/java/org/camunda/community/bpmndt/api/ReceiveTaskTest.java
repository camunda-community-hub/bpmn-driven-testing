package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.community.bpmndt.api.TestCaseInstanceElement.MessageEventElement;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class ReceiveTaskTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  private ReceiveTaskHandler handler;

  private String correlationKey;

  @BeforeEach
  void setUp() {
    var element = new MessageEventElement();
    element.id = "receiveTask";
    element.correlationKey = "=\"simple\"";
    element.messageName = "=\"simpleMessage\"";

    handler = new ReceiveTaskHandler(element);

    correlationKey = String.valueOf(System.currentTimeMillis());
  }

  @Test
  void testExecute() {
    tc.createExecutor(client, processTestContext)
        .withVariable("correlationKey", correlationKey)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  void testExecuteWithCustomAction() {
    handler.execute((client, messageName, correlationKey) ->
        client.newPublishMessageCommand().messageName(messageName).correlationKey(correlationKey).send()
    );

    tc.createExecutor(client, processTestContext)
        .withVariable("correlationKey", correlationKey)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  void testVerify() {
    handler.verify(processInstanceAssert -> processInstanceAssert.hasVariable("x", "test"));

    tc.createExecutor(client, processTestContext)
        .withVariable("correlationKey", correlationKey)
        .withVariable("x", "test")
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  void testVerifyCorrelationKey() {
    handler.verifyCorrelationKey("wrong correlation key");

    var e = assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext)
        .withVariable("correlationKey", correlationKey)
        .execute()
    );
    assertThat(e).hasMessageThat().contains("'wrong correlation key'");
    assertThat(e).hasMessageThat().contains("'" + correlationKey + "'");

    correlationKey = String.valueOf(System.currentTimeMillis());
    handler.verifyCorrelationKey(correlationKey);

    tc.createExecutor(client, processTestContext)
        .withVariable("correlationKey", correlationKey)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();

    handler.verifyCorrelationKey((String) null);

    correlationKey = String.valueOf(System.currentTimeMillis());
    handler.verifyCorrelationKey(value -> assertThat(value).isEqualTo("wrong correlation key"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext)
        .withVariable("correlationKey", correlationKey)
        .execute()
    );

    correlationKey = String.valueOf(System.currentTimeMillis());
    handler.verifyCorrelationKey(value -> assertThat(value).isEqualTo(correlationKey));

    tc.createExecutor(client, processTestContext)
        .withVariable("correlationKey", correlationKey)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  void testVerifyCorrelationKeyExpression() {
    handler.verifyCorrelationKeyExpression(expr -> assertThat(expr).isEqualTo("wrong correlation key expression"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext)
        .withVariable("correlationKey", correlationKey)
        .execute()
    );

    handler.verifyCorrelationKeyExpression(expr -> assertThat(expr).isEqualTo("=\"simple\""));

    correlationKey = String.valueOf(System.currentTimeMillis());

    tc.createExecutor(client, processTestContext)
        .withVariable("correlationKey", correlationKey)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  void testVerifyMessageName() {
    handler.verifyMessageName("wrong message name");

    var e = assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext)
        .withVariable("correlationKey", correlationKey)
        .execute()
    );
    assertThat(e).hasMessageThat().contains("'wrong message name'");
    assertThat(e).hasMessageThat().contains("'simpleMessage'");

    handler.verifyMessageName("simpleMessage");

    correlationKey = String.valueOf(System.currentTimeMillis());

    tc.createExecutor(client, processTestContext)
        .withVariable("correlationKey", correlationKey)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();

    handler.verifyMessageName(messageName -> assertThat(messageName).isEqualTo("wrong message name"));

    correlationKey = String.valueOf(System.currentTimeMillis());

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext)
        .withVariable("correlationKey", correlationKey)
        .execute()
    );

    handler.verifyMessageName(messageName -> assertThat(messageName).isEqualTo("simpleMessage"));

    correlationKey = String.valueOf(System.currentTimeMillis());

    tc.createExecutor(client, processTestContext)
        .withVariable("correlationKey", correlationKey)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  void testVerifyMessageNameExpression() {
    handler.verifyMessageNameExpression(expr -> assertThat(expr).isEqualTo("wrong message name expression"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext)
        .withVariable("correlationKey", correlationKey)
        .execute()
    );

    handler.verifyMessageNameExpression(expr -> assertThat(expr).isEqualTo("=\"simpleMessage\""));

    correlationKey = String.valueOf(System.currentTimeMillis());

    tc.createExecutor(client, processTestContext)
        .withVariable("correlationKey", correlationKey)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "startEvent");
      instance.isWaitingAt(processInstanceKey, "receiveTask");
      instance.apply(processInstanceKey, handler);
      instance.hasPassed(processInstanceKey, "receiveTask");
      instance.hasPassed(processInstanceKey, "endEvent");
    }

    @Override
    public String getBpmnProcessId() {
      return "simpleReceiveTask";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.simple("simpleReceiveTask.bpmn"));
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
