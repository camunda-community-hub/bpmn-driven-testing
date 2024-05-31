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

import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class MessageCatchEventTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;

  private MessageEventHandler handler;

  @BeforeEach
  void setUp() {
    var element = new MessageEventElement();
    element.id = "messageCatchEvent";
    element.correlationKey = "=\"simple\"";
    element.messageName = "=\"simpleMessage\"";

    handler = new MessageEventHandler(element);
  }

  @Test
  void testExecute() {
    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testExecuteWithCustomAction() {
    handler.execute((client, messageName, correlationKey) ->
        client.newPublishMessageCommand().messageName(messageName).correlationKey(correlationKey).send()
    );

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerify() {
    handler.verify(processInstanceAssert -> processInstanceAssert.hasVariableWithValue("x", "test"));

    tc.createExecutor(engine)
        .withVariable("x", "test")
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  void testVerifyCorrelationKey() {
    handler.verifyCorrelationKey("wrong correlation key");

    var e = assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());
    assertThat(e).hasMessageThat().contains("'wrong correlation key'");
    assertThat(e).hasMessageThat().contains("'simple'");

    handler.verifyCorrelationKey("simple");

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();

    handler.verifyCorrelationKey(correlationKey -> assertThat(correlationKey).isEqualTo("wrong correlation key"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyCorrelationKey(correlationKey -> assertThat(correlationKey).isEqualTo("simple"));

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyCorrelationKeyExpression() {
    handler.verifyCorrelationKeyExpression(expr -> assertThat(expr).isEqualTo("wrong correlation key expression"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyCorrelationKeyExpression(expr -> assertThat(expr).isEqualTo("=\"simple\""));

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyMessageName() {
    handler.verifyMessageName("wrong message name");

    var e = assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());
    assertThat(e).hasMessageThat().contains("'wrong message name'");
    assertThat(e).hasMessageThat().contains("'simpleMessage'");

    handler.verifyMessageName("simpleMessage");

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();

    handler.verifyMessageName(messageName -> assertThat(messageName).isEqualTo("wrong message name"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyMessageName(messageName -> assertThat(messageName).isEqualTo("simpleMessage"));

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyMessageNameExpression() {
    handler.verifyMessageNameExpression(expr -> assertThat(expr).isEqualTo("wrong message name expression"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyMessageNameExpression(expr -> assertThat(expr).isEqualTo("=\"simpleMessage\""));

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "startEvent");
      instance.isWaitingAt(processInstanceKey, "messageCatchEvent");
      instance.apply(processInstanceKey, handler);
      instance.hasPassed(processInstanceKey, "messageCatchEvent");
      instance.hasPassed(processInstanceKey, "endEvent");
    }

    @Override
    public String getBpmnProcessId() {
      return "simpleMessageCatchEvent";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.simple("simpleMessageCatchEvent.bpmn"));
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
