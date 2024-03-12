package org.camunda.community.bpmndt.model.platform8;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.camunda.community.bpmndt.test.Platform8TestPaths;
import org.junit.jupiter.api.Test;

import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.model.bpmn.instance.CatchEvent;
import io.camunda.zeebe.model.bpmn.instance.Process;
import io.camunda.zeebe.model.bpmn.instance.ThrowEvent;

public class BpmnEventSupportTest {

  @Test
  public void testGetTimerEventDefinition() {
    BpmnSupport bpmnSupport = of(Platform8TestPaths.simple("simpleTimerCatchEvent.bpmn"));
    BpmnEventSupport bpmnEventSupport = new BpmnEventSupport((CatchEvent) bpmnSupport.get("timerCatchEvent"));
    assertThat(bpmnEventSupport.getTimerDefinition()).isNotNull();
    assertThat(bpmnEventSupport.getTimerDefinition().getTimeDuration().getTextContent()).isEqualTo("PT1H");
  }

  @Test
  public void testIsNoneEndEvent() {
    BpmnSupport bpmnSupport = of(Platform8TestPaths.advancedMultiInstance("scopeSequential.bpmn"));
    BpmnEventSupport bpmnEventSupport = new BpmnEventSupport((ThrowEvent) bpmnSupport.get("subProcessEndEvent"));
    assertThat(bpmnEventSupport.isNoneEnd()).isTrue();
  }

  @Test
  public void testIsNotNoneEndEvent() {
    BpmnSupport bpmnSupport = of(Platform8TestPaths.advancedMultiInstance("scopeErrorEndEvent.bpmn"));
    BpmnEventSupport bpmnEventSupport = new BpmnEventSupport((ThrowEvent) bpmnSupport.get("subProcessErrorEndEvent"));
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
