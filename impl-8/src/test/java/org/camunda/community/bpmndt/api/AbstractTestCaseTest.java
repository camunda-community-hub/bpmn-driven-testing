package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class AbstractTestCaseTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;
  ZeebeClient client;

  private Long startedProcessInstanceKey;

  @Test
  void testExecute() {
    long processInstanceKey = tc.createExecutor(engine)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();

    assertThat(processInstanceKey).isNotEqualTo(-1);
  }

  @Test
  void testExecuteStartProcessInstanceRunnable() {
    long processInstanceKey = tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute(() ->
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

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute(processInstanceEvent);
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

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute(processInstanceEvent.getProcessInstanceKey());
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
