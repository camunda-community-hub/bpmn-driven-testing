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
class ServiceTaskErrorTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  private JobHandler handler;

  @BeforeEach
  void setUp() {
    handler = new JobHandler("serviceTask");
  }

  @Test
  void testExecute() {
    var workerBuilder = client.newWorker()
        .jobType("serviceTaskType")
        .handler((client, job) -> client.newThrowErrorCommand(job).errorCode("ADVANCED_ERROR").send());

    try (var ignored = workerBuilder.open()) {
      tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
    }
  }

  @Test
  void testThrowBpmnError() {
    handler
        .withVariable("x", "test")
        .withVariable("y", 1)
        .withVariable("z", true)
        .throwBpmnError("ADVANCED_ERROR", "test error message");

    tc.createExecutor(client, processTestContext)
        .verify(piAssert -> {
          piAssert.isCompleted();

          piAssert.hasVariable("x", "test");
          piAssert.hasVariable("y", 1);
          piAssert.hasVariable("z", true);

          // TODO map error message via FEEL to verify throw error command included the specified error message
          // but it seems that it is currently not possible!?
          // piAssert.hasVariableWithValue("errorMessage", "test message");
        })
        .execute();
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "startEvent");
      instance.isWaitingAt(processInstanceKey, "serviceTask");
      instance.apply(processInstanceKey, handler);
      instance.hasTerminated(processInstanceKey, "serviceTask");
      instance.hasPassed(processInstanceKey, "errorBoundaryEvent");
      instance.hasPassed(processInstanceKey, "endEvent");
      instance.isCompleted(processInstanceKey);
    }

    @Override
    public String getBpmnProcessId() {
      return "serviceTaskError";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.advanced("serviceTaskError.bpmn"));
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
