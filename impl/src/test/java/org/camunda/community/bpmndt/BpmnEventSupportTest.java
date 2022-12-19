package org.camunda.community.bpmndt;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Path;

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
    bpmnSupport = BpmnSupport.of(advancedMultiInstance.resolve("scopeSequential.bpmn"));
    bpmnEventSupport = new BpmnEventSupport((ThrowEvent) bpmnSupport.get("subProcessEndEvent"));
    assertThat(bpmnEventSupport.isNoneEnd()).isTrue();

    bpmnSupport = BpmnSupport.of(advancedMultiInstance.resolve("scopeErrorEndEvent.bpmn"));
    bpmnEventSupport = new BpmnEventSupport((ThrowEvent) bpmnSupport.get("subProcessErrorEndEvent"));
    assertThat(bpmnEventSupport.isNoneEnd()).isFalse();
  }
}
