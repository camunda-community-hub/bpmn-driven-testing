package org.camunda.community.bpmndt.model;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.instance.Process;

class BpmnSupportTest {

  private Path advanced;
  private Path advancedMultiInstance;
  private Path simple;

  @BeforeEach
  void setUp() {
    advanced = TestPaths.advanced();
    advancedMultiInstance = TestPaths.advancedMultiInstance();
    simple = TestPaths.simple();
  }

  @Test
  void testGetMultiInstance() {
    var bpmnSupport = of(advancedMultiInstance.resolve("sequential.bpmn"));
    assertThat(bpmnSupport.getMultiInstanceLoopCharacteristics("multiInstanceManualTask")).isNotNull();
  }

  @Test
  void testHas() {
    var bpmnSupport = of(simple.resolve("simple.bpmn"));
    assertThat(bpmnSupport.has("startEvent")).isTrue();
    assertThat(bpmnSupport.has("not-existing")).isFalse();
  }

  @Test
  void testIsBoundaryEvent() {
    var bpmnSupport = of(advanced.resolve("userTaskError.bpmn"));
    assertThat(bpmnSupport.isBoundaryEvent("errorBoundaryEvent")).isTrue();
  }

  @Test
  void testIsBusinessRuleTask() {
    var bpmnSupport = of(simple.resolve("simpleBusinessRuleTask.bpmn"));
    assertThat(bpmnSupport.isBusinessRuleTask("businessRuleTask")).isTrue();
  }

  @Test
  void testIsCallActivity() {
    var bpmnSupport = of(simple.resolve("simpleCallActivity.bpmn"));
    assertThat(bpmnSupport.isCallActivity("callActivity")).isTrue();
  }

  @Test
  void testIsEndEvent() {
    var bpmnSupport = of(simple.resolve("simple.bpmn"));
    assertThat(bpmnSupport.isEndEvent("endEvent")).isTrue();

    bpmnSupport = of(simple.resolve("simpleMessageEndEvent.bpmn"));
    assertThat(bpmnSupport.isEndEvent("messageEndEvent")).isTrue();
  }

  @Test
  void testIsEventBasedGateway() {
    var bpmnSupport = of(simple.resolve("simpleEventBasedGateway.bpmn"));
    assertThat(bpmnSupport.isEventBasedGateway("eventBasedGateway")).isTrue();
  }

  @Test
  void testIsIntermediateCatchEvent() {
    var bpmnSupport = of(simple.resolve("simpleMessageCatchEvent.bpmn"));
    assertThat(bpmnSupport.isIntermediateCatchEvent("messageCatchEvent")).isTrue();

    bpmnSupport = of(simple.resolve("simpleSignalCatchEvent.bpmn"));
    assertThat(bpmnSupport.isIntermediateCatchEvent("signalCatchEvent")).isTrue();

    bpmnSupport = of(simple.resolve("simpleTimerCatchEvent.bpmn"));
    assertThat(bpmnSupport.isIntermediateCatchEvent("timerCatchEvent")).isTrue();
  }

  @Test
  void testIsIntermediateThrowEvent() {
    var bpmnSupport = of(advanced.resolve("linkEvent.bpmn"));
    assertThat(bpmnSupport.isIntermediateThrowEvent("linkThrowEventA")).isTrue();
    assertThat(bpmnSupport.isIntermediateThrowEvent("linkCatchEventA")).isFalse();
  }

  @Test
  void testIsProcessEnd() {
    var bpmnSupport = of(simple.resolve("simpleSubProcess.bpmn"));
    assertThat(bpmnSupport.isProcessEnd("startEvent")).isFalse();
    assertThat(bpmnSupport.isProcessEnd("subProcessStartEvent")).isFalse();
    assertThat(bpmnSupport.isProcessEnd("subProcessEndEvent")).isFalse();
    assertThat(bpmnSupport.isProcessEnd("endEvent")).isTrue();
  }

  @Test
  void testIsProcessStart() {
    var bpmnSupport = of(simple.resolve("simpleSubProcess.bpmn"));
    assertThat(bpmnSupport.isProcessStart("startEvent")).isTrue();
    assertThat(bpmnSupport.isProcessStart("subProcessStartEvent")).isFalse();
    assertThat(bpmnSupport.isProcessStart("subProcessEndEvent")).isFalse();
    assertThat(bpmnSupport.isProcessStart("endEvent")).isFalse();
  }

  @Test
  void testIsReceiveTask() {
    var bpmnSupport = of(simple.resolve("simpleReceiveTask.bpmn"));
    assertThat(bpmnSupport.isReceiveTask("receiveTask")).isTrue();
  }

  @Test
  void testIsScriptTask() {
    var bpmnSupport = of(simple.resolve("simpleScriptTask.bpmn"));
    assertThat(bpmnSupport.isScriptTask("scriptTask")).isTrue();
  }

  @Test
  void testIsSendTask() {
    var bpmnSupport = of(simple.resolve("simpleSendTask.bpmn"));
    assertThat(bpmnSupport.isSendTask("sendTask")).isTrue();
  }

  @Test
  void testIsServiceTask() {
    var bpmnSupport = of(simple.resolve("simpleServiceTask.bpmn"));
    assertThat(bpmnSupport.isServiceTask("serviceTask")).isTrue();
  }

  @Test
  void testIsUserTask() {
    var bpmnSupport = of(simple.resolve("simpleUserTask.bpmn"));
    assertThat(bpmnSupport.isUserTask("userTask")).isTrue();
  }

  @Test
  void shouldWorkWithCollaboration() {
    var bpmnSupport = of(simple.resolve("simpleCollaboration.bpmn"));
    assertThat(bpmnSupport.getProcess().getId()).isEqualTo("simpleCollaboration");
    assertThat(bpmnSupport.has("startEvent")).isTrue();
    assertThat(bpmnSupport.has("endEvent")).isTrue();
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
