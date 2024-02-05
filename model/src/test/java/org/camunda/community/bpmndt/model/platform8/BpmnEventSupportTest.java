package org.camunda.community.bpmndt.model.platform8;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.camunda.community.bpmndt.test.Platform8TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.instance.Process;
import io.camunda.zeebe.model.bpmn.instance.ThrowEvent;

public class BpmnEventSupportTest {

  private Path advancedMultiInstance;

  @BeforeEach
  public void setUp() {
    advancedMultiInstance = Platform8TestPaths.advancedMultiInstance();
  }

  @Test
  public void testIsNoneEndEvent() {
    var bpmnSupport = of(advancedMultiInstance.resolve("scopeSequential.bpmn"));
    var bpmnEventSupport = new BpmnEventSupport((ThrowEvent) bpmnSupport.get("subProcessEndEvent"));
    assertThat(bpmnEventSupport.isNoneEnd()).isTrue();
  }

  @Test
  public void testIsNotNoneEndEvent() {
    var bpmnSupport = of(advancedMultiInstance.resolve("scopeErrorEndEvent.bpmn"));
    var bpmnEventSupport = new BpmnEventSupport((ThrowEvent) bpmnSupport.get("subProcessErrorEndEvent"));
    assertThat(bpmnEventSupport.isNoneEnd()).isFalse();
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
