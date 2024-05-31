package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class MultiInstanceScopeSequentialTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;
  ZeebeClient client;

  private CustomMultiInstanceHandler handler;

  @BeforeEach
  public void setUp() {
    handler = new CustomMultiInstanceHandler("multiInstanceScope");
  }

  @Test
  void testExecute() {
    var elements = List.of(1, 2, 3);

    handler.execute((testCaseInstance, processInstanceKey) -> {
      var userTaskHandler = new UserTaskHandler("userTask");
      var messageCatchEventHandler = new MessageEventHandler("messageCatchEvent");
      var serviceTaskHandler = new JobHandler("serviceTask");
      var callActivityHandler = new CallActivityHandler("callActivity");

      for (int i = 0; i < elements.size(); i++) {
        testCaseInstance.apply(processInstanceKey, userTaskHandler);
        testCaseInstance.apply(processInstanceKey, messageCatchEventHandler);

        var workerBuilder = client.newWorker()
            .jobType("advanced")
            .handler((client, job) -> client.newCompleteCommand(job).send());

        try (var ignored = workerBuilder.open()) {
          testCaseInstance.apply(processInstanceKey, serviceTaskHandler);
          testCaseInstance.hasPassed(processInstanceKey, "serviceTask");
        }

        testCaseInstance.apply(processInstanceKey, callActivityHandler);
      }
    });

    tc.createExecutor(engine)
        .simulateProcess("advanced")
        .withVariable("elements", elements)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  void testErrorContainsActiveElements() {
    var e = assertThrows(AssertionError.class, () -> tc.createExecutor(engine)
        .withTaskTimeout(1000)
        .withVariable("elements", List.of(1, 2, 3))
        .verify(ProcessInstanceAssert::isCompleted)
        .execute()
    );

    assertThat(e.getMessage()).contains("found active elements:");
    assertThat(e.getMessage()).contains("  - multiInstanceScope");
    assertThat(e.getMessage()).contains("  - userTask");
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "startEvent");
      instance.apply(processInstanceKey, handler);
      instance.hasPassedMultiInstance(processInstanceKey, "multiInstanceScope");
      instance.hasPassed(processInstanceKey, "endEvent");
    }

    @Override
    public String getBpmnProcessId() {
      return "scopeSequential";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.advancedMultiInstance("scopeSequential.bpmn"));
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
