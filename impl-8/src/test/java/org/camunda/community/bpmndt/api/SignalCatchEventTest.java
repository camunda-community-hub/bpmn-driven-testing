package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.community.bpmndt.api.TestCaseInstanceElement.SignalEventElement;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class SignalCatchEventTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;

  private SignalEventHandler handler;

  @BeforeEach
  void setUp() {
    SignalEventElement element = new SignalEventElement();
    element.id = "signalCatchEvent";
    element.signalName = "=\"simpleSignal\"";

    handler = new SignalEventHandler(element);
  }

  @Test
  void testExecute() {
    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testExecuteWithCustomAction() {
    handler.execute((client, signalName) -> client.newBroadcastSignalCommand().signalName(signalName).send());

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
  void testVerifySignalName() {
    handler.verifySignalName("wrong signal name");

    var e = assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());
    assertThat(e).hasMessageThat().contains("'wrong signal name'");
    assertThat(e).hasMessageThat().contains("'simpleSignal'");

    handler.verifySignalName("simpleSignal");

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();

    handler.verifySignalName(signalName -> assertThat(signalName).isEqualTo("wrong signal name"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifySignalName(signalName -> assertThat(signalName).isEqualTo("simpleSignal"));

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifySignalNameExpression() {
    handler.verifySignalNameExpression(expr -> assertThat(expr).isEqualTo("wrong signal name expression"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifySignalNameExpression(expr -> assertThat(expr).isEqualTo("=\"simpleSignal\""));

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
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
