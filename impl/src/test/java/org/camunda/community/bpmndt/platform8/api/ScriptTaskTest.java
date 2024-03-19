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
public class ScriptTaskTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;
  ZeebeClient client;

  private JobHandler handler;

  @BeforeEach
  public void setUp() {
    JobElement element = new JobElement();
    element.setId("scriptTask");
    element.setType("scriptTaskType");

    handler = new JobHandler(element);
  }

  @Test
  public void testExecute() {
    JobWorkerBuilderStep3 workerBuilder = client.newWorker().jobType("scriptTaskType").handler((client, job) ->
        client.newCompleteCommand(job).send()
    );

    try (JobWorker ignored = workerBuilder.open()) {
      tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
    }
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, ProcessInstanceEvent processInstanceEvent) {
      instance.hasPassed(processInstanceEvent, "startEvent");
      instance.isWaitingAt(processInstanceEvent, "scriptTask");
      instance.apply(processInstanceEvent, handler);
      instance.hasPassed(processInstanceEvent, "scriptTask");
      instance.hasPassed(processInstanceEvent, "endEvent");
    }

    @Override
    public String getBpmnProcessId() {
      return "simpleScriptTask";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(Platform8TestPaths.simple("simpleScriptTask.bpmn"));
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
