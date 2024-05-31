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
class MessageEndEventTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;
  ZeebeClient client;

  private JobHandler handler;

  @BeforeEach
  void setUp() {
    handler = new JobHandler("messageEndEvent");
  }

  @Test
  void testExecute() {
    var workerBuilder = client.newWorker().jobType("messageEndEventType").handler((client, job) ->
        client.newCompleteCommand(job).send()
    );

    try (var ignored = workerBuilder.open()) {
      tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
    }
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "startEvent");
      instance.isWaitingAt(processInstanceKey, "messageEndEvent");
      instance.apply(processInstanceKey, handler);
      instance.hasPassed(processInstanceKey, "messageEndEvent");
    }

    @Override
    public String getBpmnProcessId() {
      return "simpleMessageEndEvent";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.simple("simpleMessageEndEvent.bpmn"));
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
      return "messageEndEvent";
    }
  }
}
