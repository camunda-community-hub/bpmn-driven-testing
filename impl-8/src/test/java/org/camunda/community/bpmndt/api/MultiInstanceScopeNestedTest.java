package org.camunda.community.bpmndt.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class MultiInstanceScopeNestedTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;

  private CustomMultiInstanceHandler handler;

  @BeforeEach
  public void setUp() {
    handler = new CustomMultiInstanceHandler("subProcess");
  }

  @Test
  void testExecute() {
    var elements = List.of(1, 2, 3);
    var nestedElements = List.of(4, 5);

    handler.verifyLoopCount(3).executeLoop((testCaseInstance, subProcessKey) -> {
      var nestedSubProcessHandler = new CustomMultiInstanceHandler("nestedSubProcess");

      nestedSubProcessHandler.verifyLoopCount(2).executeLoop((__, nestedSubProcessKey) -> {
        var userTaskHandler = new UserTaskHandler("userTask");

        testCaseInstance.apply(nestedSubProcessKey, userTaskHandler);
      });

      testCaseInstance.apply(subProcessKey, nestedSubProcessHandler);
    });

    tc.createExecutor(engine)
        .simulateProcess("advanced")
        .withVariable("elements", elements)
        .withVariable("nestedElements", nestedElements)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "startEvent");
      instance.apply(processInstanceKey, handler);
      instance.hasPassedMultiInstance(processInstanceKey, "subProcess");
      instance.hasPassed(processInstanceKey, "endEvent");
    }

    @Override
    public String getBpmnProcessId() {
      return "scopeNested";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.advancedMultiInstance("scopeNested.bpmn"));
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
