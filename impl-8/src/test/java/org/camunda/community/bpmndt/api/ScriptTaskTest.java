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
class ScriptTaskTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  private JobHandler handler;

  @BeforeEach
  void setUp() {
    handler = new JobHandler("scriptTask");
  }

  @Test
  void testExecute() {
    var workerBuilder = client.newWorker().jobType("scriptTaskType").handler((client, job) ->
        client.newCompleteCommand(job).send()
    );

    try (var ignored = workerBuilder.open()) {
      tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
    }
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "startEvent");
      instance.isWaitingAt(processInstanceKey, "scriptTask");
      instance.apply(processInstanceKey, handler);
      instance.hasPassed(processInstanceKey, "scriptTask");
      instance.hasPassed(processInstanceKey, "endEvent");
    }

    @Override
    public String getBpmnProcessId() {
      return "simpleScriptTask";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.simple("simpleScriptTask.bpmn"));
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
