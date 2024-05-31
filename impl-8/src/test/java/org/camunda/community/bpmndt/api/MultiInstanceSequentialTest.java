package org.camunda.community.bpmndt.api;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import org.camunda.community.bpmndt.api.TestCaseInstanceElement.MultiInstanceElement;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class MultiInstanceSequentialTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;

  private CustomMultiInstanceHandler handler;

  @BeforeEach
  public void setUp() {
    MultiInstanceElement element = new MultiInstanceElement();
    element.id = "multiInstanceManualTask";
    element.sequential = true;

    handler = new CustomMultiInstanceHandler(element);
  }

  @Test
  void testExecute() {
    tc.createExecutor(engine)
        .withVariable("elements", List.of(1, 2, 3))
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  void testSequential() {
    handler.verifyParallel();

    assertThrows(AssertionError.class, () ->
        tc.createExecutor(engine)
            .withVariable("elements", List.of(1, 2, 3))
            .verify(ProcessInstanceAssert::isCompleted)
            .execute()
    );

    handler.verifySequential();

    tc.createExecutor(engine)
        .withVariable("elements", List.of(1, 2, 3))
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "startEvent");
      instance.apply(processInstanceKey, handler);
      instance.hasPassedMultiInstance(processInstanceKey, "multiInstanceManualTask");
      instance.hasPassed(processInstanceKey, "endEvent");
    }

    @Override
    public String getBpmnProcessId() {
      return "sequential";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.advancedMultiInstance("sequential.bpmn"));
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
