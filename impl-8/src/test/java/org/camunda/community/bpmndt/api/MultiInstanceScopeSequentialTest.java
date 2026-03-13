package org.camunda.community.bpmndt.api;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class MultiInstanceScopeSequentialTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  private CustomMultiInstanceHandler handler;

  @BeforeEach
  public void setUp() {
    handler = new CustomMultiInstanceHandler("multiInstanceScope");
  }

  @Test
  void testExecute() {
    var elements = List.of(1, 2, 3);

    handler.verifyLoopCount(3).executeLoop((testCaseInstance, flowScopeKey) -> {
      var userTaskHandler = new UserTaskHandler("userTask");
      var messageCatchEventHandler = new MessageEventHandler("messageCatchEvent");
      var serviceTaskHandler = new JobHandler("serviceTask");
      var callActivityHandler = new CallActivityHandler("callActivity");

      testCaseInstance.apply(flowScopeKey, userTaskHandler);
      testCaseInstance.apply(flowScopeKey, messageCatchEventHandler);

      var workerBuilder = client.newWorker()
          .jobType("advanced")
          .handler((client, job) -> client.newCompleteCommand(job).send());

      try (var ignored = workerBuilder.open()) {
        testCaseInstance.apply(flowScopeKey, serviceTaskHandler);
        testCaseInstance.hasPassed(flowScopeKey, "serviceTask");
      }

      testCaseInstance.apply(flowScopeKey, callActivityHandler);
    });

    tc.createExecutor(client, processTestContext)
        .simulateProcess("advanced")
        .withVariable("elements", elements)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  void testErrorContainsElementInstances() {
    var e = assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext)
        .withVariable("elements", List.of(1, 2, 3))
        .verify(ProcessInstanceAssert::isCompleted)
        .execute()
    );
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "startEvent");
      instance.apply(processInstanceKey, handler);
      instance.hasPassed(processInstanceKey, "multiInstanceScope");
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
