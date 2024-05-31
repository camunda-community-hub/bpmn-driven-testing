package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.zeebe.client.api.command.ClientStatusException;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class SignalStartEventTest {

  @RegisterExtension
  TestCase tc = new TestCase();
  @RegisterExtension
  TestCaseMessageStart tcSignalStart = new TestCaseMessageStart();

  ZeebeTestEngine engine;

  /**
   * Tests that create instance commands without #startBeforeElement only support none start events.
   */
  @Test
  void testExecute() {
    var e = assertThrows(ClientStatusException.class, () -> tc.createExecutor(engine).execute());
    assertThat(e.getMessage()).contains("Expected to create instance of process with none start event, but there is no such event");
  }

  @Test
  void testExecuteMessageStart() {
    tcSignalStart.createExecutor(engine)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  private static class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "signalStartEvent");
      instance.hasPassed(processInstanceKey, "endEvent");
    }

    @Override
    public String getBpmnProcessId() {
      return "simpleSignalStartEvent";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.simple("simpleSignalStartEvent.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getStart() {
      return "signalStartEvent";
    }

    @Override
    public String getEnd() {
      return "endEvent";
    }
  }

  private static class TestCaseMessageStart extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "signalStartEvent");
      instance.hasPassed(processInstanceKey, "endEvent");
    }

    @Override
    public String getBpmnProcessId() {
      return "simpleSignalStartEvent";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.simple("simpleSignalStartEvent.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getStart() {
      return "signalStartEvent";
    }

    @Override
    public String getEnd() {
      return "endEvent";
    }

    @Override
    protected boolean isSignalStart() {
      return true;
    }
  }
}
