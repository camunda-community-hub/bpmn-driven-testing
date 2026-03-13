package org.camunda.community.bpmndt.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class UserTaskMessageTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  private UserTaskHandler handler;
  private MessageEventHandler boundaryEventHandler;

  @BeforeEach
  void setUp() {

    handler = new UserTaskHandler("userTask");
    boundaryEventHandler = new MessageEventHandler("messageBoundaryEvent", "userTask");
  }

  @Test
  void testExecute() {
    handler.waitForBoundaryEvent();

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "startEvent");
      instance.isWaitingAt(processInstanceKey, "userTask");
      instance.apply(processInstanceKey, handler);
      instance.apply(processInstanceKey, boundaryEventHandler);
      instance.hasPassed(processInstanceKey, "messageBoundaryEvent");
      instance.hasPassed(processInstanceKey, "endEvent");
    }

    @Override
    public String getBpmnProcessId() {
      return "userTaskMessage";
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
        return Files.newInputStream(TestPaths.advanced("userTaskMessage.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }
  }
}
