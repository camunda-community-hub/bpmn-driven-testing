package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class AbstractTestCaseTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  private Long startedProcessInstanceKey;

  @Test
  void testExecute() {
    long processInstanceKey = tc.createExecutor(client, processTestContext)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();

    assertThat(processInstanceKey).isNotEqualTo(-1);
  }

  @Test
  void testExecuteStartProcessInstanceRunnable() {
    long processInstanceKey = tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute(() ->
        startedProcessInstanceKey = client.newCreateInstanceCommand()
            .bpmnProcessId(tc.getBpmnProcessId())
            .latestVersion()
            .send()
            .join()
            .getProcessInstanceKey()
    );

    assertThat(processInstanceKey).isEqualTo(startedProcessInstanceKey);
  }

  @Test
  void testExecuteProcessInstanceEvent() {
    client.newDeployResourceCommand()
        .addResourceStream(tc.getBpmnResource(), "simple.bpmn")
        .send()
        .join();

    var processInstanceEvent = client.newCreateInstanceCommand()
        .bpmnProcessId(tc.getBpmnProcessId())
        .latestVersion()
        .send()
        .join();

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute(processInstanceEvent);
  }

  @Test
  void testExecuteProcessInstanceKey() {
    client.newDeployResourceCommand()
        .addResourceStream(tc.getBpmnResource(), "simple.bpmn")
        .send()
        .join();

    var processInstanceEvent = client.newCreateInstanceCommand()
        .bpmnProcessId(tc.getBpmnProcessId())
        .latestVersion()
        .send()
        .join();

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute(processInstanceEvent.getProcessInstanceKey());
  }

  private static class TestCase extends AbstractJUnit5TestCase {

    @Override
    public String getBpmnProcessId() {
      return "simple";
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
        return Files.newInputStream(TestPaths.simple("simple.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "startEvent");
      instance.hasPassed(processInstanceKey, "endEvent");
      instance.isCompleted(processInstanceKey);
    }
  }
}
