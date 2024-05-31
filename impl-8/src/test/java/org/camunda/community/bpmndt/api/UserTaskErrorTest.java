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
class UserTaskErrorTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;

  private UserTaskHandler handler;

  @BeforeEach
  void setUp() {
    handler = new UserTaskHandler("userTask");
  }

  @Test
  void testExecute() {
    handler.throwBpmnError("ERROR_CODE", "test error message");

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "startEvent");
      instance.isWaitingAt(processInstanceKey, "userTask");
      instance.apply(processInstanceKey, handler);
      instance.hasPassed(processInstanceKey, "errorBoundaryEvent");
      instance.hasPassed(processInstanceKey, "endEvent");
    }

    @Override
    public String getBpmnProcessId() {
      return "userTaskError";
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
        return Files.newInputStream(TestPaths.advanced("userTaskError.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }
  }
}
