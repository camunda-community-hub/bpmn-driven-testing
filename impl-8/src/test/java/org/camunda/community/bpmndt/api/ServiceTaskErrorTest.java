package org.camunda.community.bpmndt.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class ServiceTaskErrorTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;
  ZeebeClient client;

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
      tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
    }
  }

  @Test
  void testThrowBpmnError() {
    handler
        .withVariable("x", "test")
        .withVariable("y", 1)
        .withVariable("z", true)
        .throwBpmnError("ADVANCED_ERROR", "test error message");

    tc.createExecutor(engine)
        .verify(piAssert -> {
          piAssert.hasVariableWithValue("x", "test");
          piAssert.hasVariableWithValue("y", 1);
          piAssert.hasVariableWithValue("z", true);

          // TODO map error message via FEEL to verify throw error command included the specified error message
          // but it seems that it is currently not possible!?
          // piAssert.hasVariableWithValue("errorMessage", "test message");

          piAssert.isCompleted();
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
