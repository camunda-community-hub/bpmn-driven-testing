package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.command.ClientStatusException;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class SignalStartEventTest {

  @RegisterExtension
  TestCase tc = new TestCase();
  @RegisterExtension
  TestCaseMessageStart tcSignalStart = new TestCaseMessageStart();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  /**
   * Tests that create instance commands without #startBeforeElement only support none start events.
   */
  @Test
  void testExecute() {
    var e = assertThrows(ClientStatusException.class, () -> tc.createExecutor(client, processTestContext).execute());
    assertThat(e.getMessage()).contains("Expected to create instance of process with none start event, but there is no such event");
  }

  @Test
  void testExecuteMessageStart() {
    tcSignalStart.createExecutor(client, processTestContext)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute(() -> client.newBroadcastSignalCommand().signalName("simpleSignal").execute());
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
