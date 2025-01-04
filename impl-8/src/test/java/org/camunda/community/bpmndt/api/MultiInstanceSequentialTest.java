package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
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

  private int executeCounter;

  private long processInstanceKey;
  private List<Long> elementInstanceKeys;

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
  void testLoopCount() {
    handler.verifyLoopCount(2);

    assertThrows(AssertionError.class, () ->
        tc.createExecutor(engine)
            .withVariable("elements", List.of(1, 2, 3))
            .verify(ProcessInstanceAssert::isCompleted)
            .execute()
    );

    handler.verifyLoopCount(3);

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

  @Test
  void testExecuteAction() {
    handler.execute((instance, processInstanceKey) -> {
      executeCounter++;

      assertThat(instance).isNotNull();
      this.processInstanceKey = processInstanceKey;
    });

    var processInstanceKey = tc.createExecutor(engine)
        .withVariable("elements", List.of(1, 2, 3))
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();

    assertThat(executeCounter).isEqualTo(1);

    assertThat(processInstanceKey).isEqualTo(this.processInstanceKey);
  }

  @Test
  void testExecuteLoopAction() {
    elementInstanceKeys = new ArrayList<>();

    handler.executeLoop((instance, elementInstanceKey) -> {
      executeCounter++;

      assertThat(instance).isNotNull();
      elementInstanceKeys.add(elementInstanceKey);
    });

    var processInstanceKey = tc.createExecutor(engine)
        .withVariable("elements", List.of(1, 2, 3))
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();

    assertThat(executeCounter).isEqualTo(3);

    assertThat(elementInstanceKeys).hasSize(3);
    assertThat(elementInstanceKeys.get(0)).isGreaterThan(0);
    assertThat(elementInstanceKeys.get(0)).isNotEqualTo(processInstanceKey);
    assertThat(elementInstanceKeys.get(1)).isGreaterThan(0);
    assertThat(elementInstanceKeys.get(1)).isNotEqualTo(processInstanceKey);
    assertThat(elementInstanceKeys.get(2)).isGreaterThan(0);
    assertThat(elementInstanceKeys.get(2)).isNotEqualTo(processInstanceKey);

    assertThat(elementInstanceKeys.get(0)).isNotEqualTo(elementInstanceKeys.get(1));
    assertThat(elementInstanceKeys.get(0)).isNotEqualTo(elementInstanceKeys.get(2));
    assertThat(elementInstanceKeys.get(1)).isNotEqualTo(elementInstanceKeys.get(2));
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
