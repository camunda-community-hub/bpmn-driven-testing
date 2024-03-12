package org.camunda.community.bpmndt.platform8.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceElement.MessageEventElement;
import org.camunda.community.bpmndt.test.Platform8TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
public class MessageCatchEventTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;

  private MessageEventHandler handler;

  @BeforeEach
  public void setUp() {
    MessageEventElement element = new MessageEventElement();
    element.setId("messageCatchEvent");

    handler = new MessageEventHandler(element);
  }

  @Test
  public void testExecute() {
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

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, ProcessInstanceEvent processInstanceEvent) {
      instance.hasPassed(processInstanceEvent, "startEvent");
      instance.isWaitingAt(processInstanceEvent, "messageCatchEvent");
      instance.apply(processInstanceEvent, handler);
      instance.hasPassed(processInstanceEvent, "messageCatchEvent");
      instance.hasPassed(processInstanceEvent, "endEvent");
    }

    @Override
    public String getBpmnProcessId() {
      return "simpleMessageCatchEvent";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(Platform8TestPaths.simple("simpleMessageCatchEvent.bpmn"));
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
