package org.camunda.community.bpmndt.model;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.ThrowEvent;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BpmnEventSupportTest {

  private BpmnEventSupport bpmnEventSupport;
  private BpmnSupport bpmnSupport;

  private Path advancedMultiInstance;

  @BeforeEach
  public void setUp() {
    advancedMultiInstance = TestPaths.advancedMultiInstance();
  }

  @Test
  public void testIsNoneEndEvent() {
    bpmnSupport = of(advancedMultiInstance.resolve("scopeSequential.bpmn"));
    bpmnEventSupport = new BpmnEventSupport((ThrowEvent) bpmnSupport.get("subProcessEndEvent"));
    assertThat(bpmnEventSupport.isNoneEnd()).isTrue();

    bpmnSupport = of(advancedMultiInstance.resolve("scopeErrorEndEvent.bpmn"));
    bpmnEventSupport = new BpmnEventSupport((ThrowEvent) bpmnSupport.get("subProcessErrorEndEvent"));
    assertThat(bpmnEventSupport.isNoneEnd()).isFalse();
  }

  private BpmnSupport of(Path bpmnFile) {
    try {
      BpmnModelInstance modelInstance = Bpmn.readModelFromStream(Files.newInputStream(bpmnFile));
      Process process = (Process) modelInstance.getDefinitions().getUniqueChildElementByType(Process.class);
      return new BpmnSupport(process);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
