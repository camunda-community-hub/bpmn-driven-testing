package org.camunda.community.bpmndt.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class TerminateEndEventTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;

  @Test
  void testExecute() {
    tc.createExecutor(engine).withVariable("end", "terminate").verify(ProcessInstanceAssert::isCompleted).execute();
  }

  private static class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, getStart());
      instance.hasPassed(processInstanceKey, getEnd());
    }

    @Override
    public String getBpmnProcessId() {
      return "callActivitySubProcess";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.advanced("callActivitySubProcess.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getStart() {
      return "fork";
    }

    @Override
    public String getEnd() {
      return "terminateEndEvent";
    }

    @Override
    protected boolean isProcessStart() {
      return false;
    }
  }
}
