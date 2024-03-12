package org.camunda.community.bpmndt.model.platform8;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.camunda.community.bpmndt.test.Platform8TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.model.bpmn.instance.Process;

public class BpmnSupportTest {

  private Path advanced;
  private Path advancedMultiInstance;
  private Path simple;

  @BeforeEach
  public void setUp() {
    advanced = Platform8TestPaths.advanced();
    advancedMultiInstance = Platform8TestPaths.advancedMultiInstance();
    simple = Platform8TestPaths.simple();
  }

  @Test
  public void testGetMultiInstance() {
    BpmnSupport bpmnSupport = of(advancedMultiInstance.resolve("sequential.bpmn"));
    assertThat(bpmnSupport.getMultiInstanceLoopCharacteristics("multiInstanceManualTask")).isNotNull();
  }

  @Test
  public void testHas() {
    BpmnSupport bpmnSupport = of(simple.resolve("simple.bpmn"));
    assertThat(bpmnSupport.has("startEvent")).isTrue();
    assertThat(bpmnSupport.has("not-existing")).isFalse();
  }

  @Test
  public void testIsBoundaryEvent() {
    BpmnSupport bpmnSupport = of(advanced.resolve("userTaskError.bpmn"));
    assertThat(bpmnSupport.isBoundaryEvent("errorBoundaryEvent")).isTrue();
  }

  @Test
  public void testIsCallActivity() {
    BpmnSupport bpmnSupport = of(simple.resolve("simpleCallActivity.bpmn"));
    assertThat(bpmnSupport.isCallActivity("callActivity")).isTrue();
  }

  @Test
  public void testIsEventBasedGateway() {
    BpmnSupport bpmnSupport = of(simple.resolve("simpleEventBasedGateway.bpmn"));
    assertThat(bpmnSupport.isEventBasedGateway("eventBasedGateway")).isTrue();
  }

  @Test
  public void testIsIntermediateCatchEvent() {
    BpmnSupport bpmnSupport = of(simple.resolve("simpleMessageCatchEvent.bpmn"));
    assertThat(bpmnSupport.isIntermediateCatchEvent("messageCatchEvent")).isTrue();

    bpmnSupport = of(simple.resolve("simpleSignalCatchEvent.bpmn"));
    assertThat(bpmnSupport.isIntermediateCatchEvent("signalCatchEvent")).isTrue();

    bpmnSupport = of(simple.resolve("simpleTimerCatchEvent.bpmn"));
    assertThat(bpmnSupport.isIntermediateCatchEvent("timerCatchEvent")).isTrue();
  }

  @Test
  public void testIsIntermediateThrowEvent() {
    BpmnSupport bpmnSupport = of(advanced.resolve("linkEvent.bpmn"));
    assertThat(bpmnSupport.isIntermediateThrowEvent("linkThrowEventA")).isTrue();
    assertThat(bpmnSupport.isIntermediateThrowEvent("linkCatchEventA")).isFalse();
  }

  @Test
  public void testIsProcessEnd() {
    BpmnSupport bpmnSupport = of(simple.resolve("simpleSubProcess.bpmn"));
    assertThat(bpmnSupport.isProcessEnd("startEvent")).isFalse();
    assertThat(bpmnSupport.isProcessEnd("subProcessStartEvent")).isFalse();
    assertThat(bpmnSupport.isProcessEnd("subProcessEndEvent")).isFalse();
    assertThat(bpmnSupport.isProcessEnd("endEvent")).isTrue();
  }

  @Test
  public void testIsProcessStart() {
    BpmnSupport bpmnSupport = of(simple.resolve("simpleSubProcess.bpmn"));
    assertThat(bpmnSupport.isProcessStart("startEvent")).isTrue();
    assertThat(bpmnSupport.isProcessStart("subProcessStartEvent")).isFalse();
    assertThat(bpmnSupport.isProcessStart("subProcessEndEvent")).isFalse();
    assertThat(bpmnSupport.isProcessStart("endEvent")).isFalse();
  }

  @Test
  public void testIsReceiveTask() {
    BpmnSupport bpmnSupport = of(simple.resolve("simpleReceiveTask.bpmn"));
    assertThat(bpmnSupport.isReceiveTask("receiveTask")).isTrue();
  }

  @Test
  public void testIsSendTask() {
    BpmnSupport bpmnSupport = of(simple.resolve("simpleSendTask.bpmn"));
    assertThat(bpmnSupport.isSendTask("sendTask")).isTrue();
  }

  @Test
  public void testIsServiceTask() {
    BpmnSupport bpmnSupport = of(simple.resolve("simpleServiceTask.bpmn"));
    assertThat(bpmnSupport.isServiceTask("serviceTask")).isTrue();
  }

  @Test
  public void testIsUserTask() {
    BpmnSupport bpmnSupport = of(simple.resolve("simpleUserTask.bpmn"));
    assertThat(bpmnSupport.isUserTask("userTask")).isTrue();
  }

  @Test
  public void shouldWorkWithCollaboration() {
    BpmnSupport bpmnSupport = of(simple.resolve("simpleCollaboration.bpmn"));
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
