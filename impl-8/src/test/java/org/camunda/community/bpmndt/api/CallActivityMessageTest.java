package org.camunda.community.bpmndt.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class CallActivityMessageTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;

  private CallActivityHandler handler;
  private MessageEventHandler boundaryEventHandler;

  @BeforeEach
  void setUp() {
    handler = new CallActivityHandler("callActivity");
    boundaryEventHandler = new MessageEventHandler("messageBoundaryEvent");
  }

  @Test
  void testExecute() {
    handler.waitForBoundaryEvent();

    tc.createExecutor(engine)
        .simulateProcess("advanced")
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "startEvent");
      instance.isWaitingAt(processInstanceKey, "callActivity");
      instance.apply(processInstanceKey, handler);
      instance.apply(processInstanceKey, boundaryEventHandler);
      instance.hasTerminated(processInstanceKey, "callActivity");
      instance.hasPassed(processInstanceKey, "messageBoundaryEvent");
      instance.hasPassed(processInstanceKey, "endEvent");
    }

    @Override
    public String getBpmnProcessId() {
      return "callActivityMessage";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.advanced("callActivityMessage.bpmn"));
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
