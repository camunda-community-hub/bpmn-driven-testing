package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.community.bpmndt.api.TestCaseInstanceElement.SignalEventElement;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class SignalCatchEventTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  private SignalEventHandler handler;

  @BeforeEach
  void setUp() {
    SignalEventElement element = new SignalEventElement();
    element.id = "signalCatchEvent";
    element.signalName = "=\"simpleSignal\"";

    handler = new SignalEventHandler(element);
    handler.execute((client, ignored) -> client.newBroadcastSignalCommand().signalName("simpleSignal").execute());
  }

  @Test
  void testExecute() {
    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Disabled
  @Test
  void testExecuteWithCustomAction() {
    handler.execute((client, signalName) -> client.newBroadcastSignalCommand().signalName(signalName).send());

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerify() {
    handler.verify(processInstanceAssert -> processInstanceAssert.hasVariable("x", "test"));

    tc.createExecutor(client, processTestContext)
        .withVariable("x", "test")
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Disabled
  @Test
  void testVerifySignalName() {
    handler.verifySignalName("wrong signal name");

    var e = assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());
    assertThat(e).hasMessageThat().contains("'wrong signal name'");
    assertThat(e).hasMessageThat().contains("'simpleSignal'");

    handler.verifySignalName("simpleSignal");

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();

    handler.verifySignalName(signalName -> assertThat(signalName).isEqualTo("wrong signal name"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());

    handler.verifySignalName(signalName -> assertThat(signalName).isEqualTo("simpleSignal"));

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifySignalNameExpression() {
    handler.verifySignalNameExpression(expr -> assertThat(expr).isEqualTo("wrong signal name expression"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());

    handler.verifySignalNameExpression(expr -> assertThat(expr).isEqualTo("=\"simpleSignal\""));

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "startEvent");
      instance.isWaitingAt(processInstanceKey, "signalCatchEvent");
      instance.apply(processInstanceKey, handler);
      instance.hasPassed(processInstanceKey, "signalCatchEvent");
      instance.hasPassed(processInstanceKey, "endEvent");
    }

    @Override
    public String getBpmnProcessId() {
      return "simpleSignalCatchEvent";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.simple("simpleSignalCatchEvent.bpmn"));
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
