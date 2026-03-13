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
class CallActivityErrorTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  private CallActivityHandler handler;

  @BeforeEach
  void setUp() {
    handler = new CallActivityHandler("callActivity");
  }

  @Test
  void testExecute() {
    handler.withErrorCode("advancedError");

    tc.createExecutor(client, processTestContext)
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
      instance.hasTerminated(processInstanceKey, "callActivity");
      instance.hasPassed(processInstanceKey, "errorBoundaryEvent");
      instance.hasPassed(processInstanceKey, "endEvent");
    }

    @Override
    public String getBpmnProcessId() {
      return "callActivityError";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.advanced("callActivityError.bpmn"));
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
