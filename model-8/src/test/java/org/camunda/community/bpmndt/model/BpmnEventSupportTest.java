package org.camunda.community.bpmndt.model;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.Test;

import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.instance.CatchEvent;
import io.camunda.zeebe.model.bpmn.instance.Process;

class BpmnEventSupportTest {

  @Test
  void testGetTimerEventDefinition() {
    var bpmnSupport = of(TestPaths.simple("simpleTimerCatchEvent.bpmn"));
    var bpmnEventSupport = new BpmnEventSupport((CatchEvent) bpmnSupport.get("timerCatchEvent"));
    assertThat(bpmnEventSupport.getTimerDefinition()).isNotNull();
    assertThat(bpmnEventSupport.getTimerDefinition().getTimeDuration().getTextContent()).isEqualTo("PT1H");
  }

  @Test
  void testIsMesageEvent() {
    var bpmnSupport = of(TestPaths.simple("simpleMessageStartEvent.bpmn"));
    var bpmnEventSupport = new BpmnEventSupport((CatchEvent) bpmnSupport.get("messageStartEvent"));
    assertThat(bpmnEventSupport.isMessage()).isTrue();
  }


  @Test
  void testIsNotMessageEvent() {
    var bpmnSupport = of(TestPaths.simple("simple.bpmn"));
    var bpmnEventSupport = new BpmnEventSupport((CatchEvent) bpmnSupport.get("startEvent"));
    assertThat(bpmnEventSupport.isMessage()).isFalse();
  }

  private BpmnSupport of(Path bpmnFile) {
    try {
      var modelInstance = Bpmn.readModelFromStream(Files.newInputStream(bpmnFile));
      var process = (Process) modelInstance.getDefinitions().getUniqueChildElementByType(Process.class);
      return new BpmnSupport(process);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
