package org.camunda.community.bpmndt.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class SubProcessNestedTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;
  ZeebeClient client;

  private UserTaskHandler userTaskHandler;

  @BeforeEach
  void setUp() {
    userTaskHandler = new UserTaskHandler("userTask");
  }

  @Test
  void testExecute() {
    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "startEvent");
      executeSubProcess(instance, processInstanceKey);
      instance.hasPassed(processInstanceKey, "subProcess");
      instance.hasPassed(processInstanceKey, "endEvent");
    }

    protected void executeSubProcess(TestCaseInstance instance, long parentFlowScopeKey) {
      var flowScopeKey = instance.getElementInstanceKey(parentFlowScopeKey, "subProcess");

      instance.hasPassed(flowScopeKey, "subProcessStartEvent");
      executeSubProcessNested(instance, flowScopeKey);
      instance.hasPassed(flowScopeKey, "nestedSubProcess");
      instance.hasPassed(flowScopeKey, "subProcessEndEvent");
    }

    protected void executeSubProcessNested(TestCaseInstance instance, long parentFlowScopeKey) {
      var flowScopeKey = instance.getElementInstanceKey(parentFlowScopeKey, "nestedSubProcess");

      instance.hasPassed(flowScopeKey, "nestedSubProcessStartEvent");
      instance.apply(flowScopeKey, userTaskHandler);
      instance.hasPassed(flowScopeKey, "nestedSubProcessEndEvent");
    }

    @Override
    public String getBpmnProcessId() {
      return "simpleSubProcessNested";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.simple("simpleSubProcessNested.bpmn"));
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
