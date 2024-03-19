package org.camunda.community.bpmndt.platform8.api;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceElement.SignalEventElement;
import org.camunda.community.bpmndt.test.Platform8TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
public class SignalCatchEventTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;

  private SignalEventHandler handler;

  @BeforeEach
  public void setUp() {
    SignalEventElement element = new SignalEventElement();
    element.setId("signalCatchEvent");
    element.setSignalName("=\"simpleSignal\"");

    handler = new SignalEventHandler(element);
  }

  @Test
  public void testExecute() {
    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  public void testExecuteWithCustomAction() {
    handler.broadcast((client, signalName) -> client.newBroadcastSignalCommand().signalName(signalName).send());

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  public void testVerify() {
    handler.verify(processInstanceAssert -> processInstanceAssert.hasVariableWithValue("x", "test"));

    tc.createExecutor(engine)
        .withVariable("x", "test")
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  public void testVerifySignalName() {
    handler.verifySignalName("wrong signal name");

    AssertionError e = assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());
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
  public void testVerifySignalNameExpression() {
    handler.verifySignalNameExpression(expr -> assertThat(expr).isEqualTo("wrong signal name expression"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifySignalNameExpression(expr -> assertThat(expr).isEqualTo("=\"simpleSignal\""));

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, ProcessInstanceEvent processInstanceEvent) {
      instance.hasPassed(processInstanceEvent, "startEvent");
      instance.isWaitingAt(processInstanceEvent, "signalCatchEvent");
      instance.apply(processInstanceEvent, handler);
      instance.hasPassed(processInstanceEvent, "signalCatchEvent");
      instance.hasPassed(processInstanceEvent, "endEvent");
    }

    @Override
    public String getBpmnProcessId() {
      return "simpleSignalCatchEvent";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(Platform8TestPaths.simple("simpleSignalCatchEvent.bpmn"));
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
