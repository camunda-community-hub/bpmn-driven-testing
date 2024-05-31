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
class ServiceTaskMessageTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;

  private JobHandler handler;
  private MessageEventHandler boundaryEventHandler;

  @BeforeEach
  void setUp() {
    handler = new JobHandler("serviceTask");
    boundaryEventHandler = new MessageEventHandler("messageBoundaryEvent");
  }

  @Test
  void testExecute() {
    handler.waitForBoundaryEvent();

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    public String getBpmnProcessId() {
      return "serviceTaskMessage";
    }

    @Override
    public String getEnd() {
      return "endEvent";
    }

    @Override
    public String getStart() {
      return "startEvent";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.advanced("serviceTaskMessage.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "startEvent");
      instance.isWaitingAt(processInstanceKey, "serviceTask");
      instance.apply(processInstanceKey, handler);
      instance.apply(processInstanceKey, boundaryEventHandler);
      instance.hasPassed(processInstanceKey, "messageBoundaryEvent");
      instance.hasPassed(processInstanceKey, "endEvent");
      instance.isCompleted(processInstanceKey);
    }
  }
}
