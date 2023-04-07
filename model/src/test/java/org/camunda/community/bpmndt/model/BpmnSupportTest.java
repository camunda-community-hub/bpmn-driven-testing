package org.camunda.community.bpmndt.model;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BpmnSupportTest {

  private BpmnSupport bpmnSupport;

  private Path advanced;
  private Path advancedMultiInstance;
  private Path simple;

  @BeforeEach
  public void setUp() {
    advanced = TestPaths.advanced();
    advancedMultiInstance = TestPaths.advancedMultiInstance();
    simple = TestPaths.simple();
  }

  @Test
  public void testGetMultiInstance() {
    bpmnSupport = of(advancedMultiInstance.resolve("sequential.bpmn"));
    assertThat(bpmnSupport.getMultiInstanceLoopCharacteristics("multiInstanceManualTask")).isNotNull();
  }

  @Test
  public void testHas() {
    bpmnSupport = of(simple.resolve("simple.bpmn"));
    assertThat(bpmnSupport.has("startEvent")).isTrue();
    assertThat(bpmnSupport.has("not-existing")).isFalse();
  }

  @Test
  public void testIsBoundaryEvent() {
    bpmnSupport = of(advanced.resolve("userTaskError.bpmn"));
    assertThat(bpmnSupport.isBoundaryEvent("errorBoundaryEvent")).isTrue();
  }

  @Test
  public void testIsCallActivity() {
    bpmnSupport = of(simple.resolve("simpleCallActivity.bpmn"));
    assertThat(bpmnSupport.isCallActivity("callActivity")).isTrue();
  }

  @Test
  public void testIsEventBasedGateway() {
    bpmnSupport = of(simple.resolve("simpleEventBasedGateway.bpmn"));
    assertThat(bpmnSupport.isEventBasedGateway("eventBasedGateway")).isTrue();
  }

  @Test
  public void testIsExternalTask() {
    bpmnSupport = of(simple.resolve("simpleExternalTask.bpmn"));
    assertThat(bpmnSupport.isExternalTask("externalTask")).isTrue();
  }

  @Test
  public void testIsIntermediateCatchEvent() {
    bpmnSupport = of(simple.resolve("simpleMessageCatchEvent.bpmn"));
    assertThat(bpmnSupport.isIntermediateCatchEvent("messageCatchEvent")).isTrue();

    bpmnSupport = of(simple.resolve("simpleSignalCatchEvent.bpmn"));
    assertThat(bpmnSupport.isIntermediateCatchEvent("signalCatchEvent")).isTrue();

    bpmnSupport = of(simple.resolve("simpleTimerCatchEvent.bpmn"));
    assertThat(bpmnSupport.isIntermediateCatchEvent("timerCatchEvent")).isTrue();
  }

  @Test
  public void testIsIntermediateThrowEvent() {
    bpmnSupport = of(advanced.resolve("linkEvent.bpmn"));
    assertThat(bpmnSupport.isIntermediateThrowEvent("linkThrowEventA")).isTrue();
    assertThat(bpmnSupport.isIntermediateThrowEvent("linkCatchEventA")).isFalse();
  }

  @Test
  public void testIsProcessEnd() {
    bpmnSupport = of(simple.resolve("simpleSubProcess.bpmn"));
    assertThat(bpmnSupport.isProcessEnd("startEvent")).isFalse();
    assertThat(bpmnSupport.isProcessEnd("subProcessStartEvent")).isFalse();
    assertThat(bpmnSupport.isProcessEnd("subProcessEndEvent")).isFalse();
    assertThat(bpmnSupport.isProcessEnd("endEvent")).isTrue();
  }

  @Test
  public void testIsProcessStart() {
    bpmnSupport = of(simple.resolve("simpleSubProcess.bpmn"));
    assertThat(bpmnSupport.isProcessStart("startEvent")).isTrue();
    assertThat(bpmnSupport.isProcessStart("subProcessStartEvent")).isFalse();
    assertThat(bpmnSupport.isProcessStart("subProcessEndEvent")).isFalse();
    assertThat(bpmnSupport.isProcessStart("endEvent")).isFalse();
  }

  @Test
  public void testIsReceiveTask() {
    bpmnSupport = of(simple.resolve("simpleReceiveTask.bpmn"));
    assertThat(bpmnSupport.isReceiveTask("receiveTask")).isTrue();
  }

  @Test
  public void testIsUserTask() {
    bpmnSupport = of(simple.resolve("simpleUserTask.bpmn"));
    assertThat(bpmnSupport.isUserTask("userTask")).isTrue();
  }

  @Test
  public void shouldWorkWithCollaboration() {
    bpmnSupport = of(simple.resolve("simpleCollaboration.bpmn"));
    assertThat(bpmnSupport.getProcess().getId()).isEqualTo("simpleCollaboration");
    assertThat(bpmnSupport.has("startEvent")).isTrue();
    assertThat(bpmnSupport.has("endEvent")).isTrue();
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
