package org.camunda.community.bpmndt.platform8.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceElement.JobElement;
import org.camunda.community.bpmndt.test.Platform8TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.client.api.worker.JobWorkerBuilderStep1.JobWorkerBuilderStep3;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
public class ServiceTaskErrorTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;
  ZeebeClient client;

  private JobHandler handler;

  @BeforeEach
  public void setUp() {
    JobElement element = new JobElement();
    element.setErrorCode("ADVANCED_ERROR");
    element.setId("serviceTask");
    element.setType("serviceTaskType");

    handler = new JobHandler(element);
  }

  @Test
  public void testExecute() {
    JobWorkerBuilderStep3 workerBuilder = client.newWorker().jobType("serviceTaskType").handler((client, job) -> {
      client.newThrowErrorCommand(job).errorCode("ADVANCED_ERROR").send();
    });

    try (JobWorker ignored = workerBuilder.open()) {
      tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
    }
  }

  @Test
  public void testThrowBpmnError() {
    handler
        .withVariable("x", "test")
        .withVariable("y", 1)
        .withVariable("z", true)
        .throwBpmnError("test message");

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
    public String getBpmnProcessId() {
      return "serviceTaskError";
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
        return Files.newInputStream(Platform8TestPaths.advanced("serviceTaskError.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    protected void execute(TestCaseInstance instance, ProcessInstanceEvent processInstanceEvent) {
      instance.hasPassed(processInstanceEvent, "startEvent");
      instance.isWaitingAt(processInstanceEvent, "serviceTask");
      instance.apply(processInstanceEvent, handler);
      instance.hasPassed(processInstanceEvent, "errorBoundaryEvent");
      instance.hasPassed(processInstanceEvent, "endEvent");
      instance.isCompleted(processInstanceEvent);
    }
  }
}
