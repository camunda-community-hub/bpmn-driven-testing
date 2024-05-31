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
class ReceiveTaskTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;

  private MessageEventHandler handler;

  @BeforeEach
  void setUp() {
    handler = new MessageEventHandler("receiveTask");
  }

  @Test
  void testExecute() {
    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "startEvent");
      instance.isWaitingAt(processInstanceKey, "receiveTask");
      instance.apply(processInstanceKey, handler);
      instance.hasPassed(processInstanceKey, "receiveTask");
      instance.hasPassed(processInstanceKey, "endEvent");
    }

    @Override
    public String getBpmnProcessId() {
      return "simpleReceiveTask";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.simple("simpleReceiveTask.bpmn"));
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
