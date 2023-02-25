package org.camunda.community.bpmndt;


import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.camunda.community.bpmndt.model.Path;
import org.camunda.community.bpmndt.model.TestCase;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BpmnSupportTest {

  private BpmnSupport bpmnSupport;

  private java.nio.file.Path advanced;
  private java.nio.file.Path advancedMultiInstance;
  private java.nio.file.Path simple;

  @BeforeEach
  public void setUp() {
    advanced = TestPaths.advanced();
    advancedMultiInstance = TestPaths.advancedMultiInstance();
    simple = TestPaths.simple();
  }

  @Test
  public void testToJavaLiteral() {
    assertThat(BpmnSupport.toJavaLiteral("Happy Path")).isEqualTo("happy_path");
    assertThat(BpmnSupport.toJavaLiteral("Happy-Path")).isEqualTo("happy_path");
    assertThat(BpmnSupport.toJavaLiteral("Happy Path!")).isEqualTo("happy_path_");
    assertThat(BpmnSupport.toJavaLiteral("startEvent__endEvent")).isEqualTo("startevent__endevent");
    assertThat(BpmnSupport.toJavaLiteral("123\nABC")).isEqualTo("_123_abc");
    assertThat(BpmnSupport.toJavaLiteral("New")).isEqualTo("_new");
  }

  @Test
  public void testToLiteral() {
    assertThat(BpmnSupport.toLiteral("Happy Path")).isEqualTo("Happy_Path");
    assertThat(BpmnSupport.toLiteral("Happy-Path")).isEqualTo("Happy_Path");
    assertThat(BpmnSupport.toLiteral("Happy Path!")).isEqualTo("Happy_Path_");
    assertThat(BpmnSupport.toLiteral("startEvent__endEvent")).isEqualTo("startEvent__endEvent");
    assertThat(BpmnSupport.toLiteral("123\nABC")).isEqualTo("123_ABC");
  }

  @Test
  public void testOfErrorFileNotFound() {
    try {
      BpmnSupport.of(Paths.get("./not-existing"));
      fail("should throw RuntimeException");
    } catch (RuntimeException e) {
      assertThat(e.getCause()).isNotNull();
      assertThat(e.getCause() instanceof IOException).isTrue();
    }
  }

  @Test
  public void testOfErrorFileNotRead() {
    try {
      BpmnSupport.of(Paths.get("."));
      fail("should throw RuntimeException");
    } catch (RuntimeException e) {
      assertThat(e.getCause()).isNotNull();
      assertThat(e.getCause() instanceof IOException).isTrue();
    }
  }

  @Test
  public void testGetMultiInstance() {
    bpmnSupport = BpmnSupport.of(advancedMultiInstance.resolve("sequential.bpmn"));
    assertThat(bpmnSupport.getMultiInstance("multiInstanceManualTask")).isNotNull();
  }

  @Test
  public void testGetTestCases() {
    List<TestCase> testCases;

    bpmnSupport = BpmnSupport.of(TestPaths.resources("happyPath.bpmn"));

    testCases = bpmnSupport.getTestCases();
    assertThat(testCases).hasSize(1);

    assertThat(testCases.get(0).getName()).isEqualTo("Happy Path");
    assertThat(testCases.get(0).getDescription()).isEqualTo("The happy path");

    Path path = testCases.get(0).getPath();
    assertThat(path).isNotNull();
    assertThat(path.getFlowNodeIds()).hasSize(2);
    assertThat(path.getFlowNodeIds().get(0)).isEqualTo("startEvent");
    assertThat(path.getFlowNodeIds().get(1)).isEqualTo("endEvent");

    bpmnSupport = BpmnSupport.of(simple.resolve("simple.bpmn"));

    testCases = bpmnSupport.getTestCases();
    assertThat(testCases).hasSize(1);

    assertThat(testCases.get(0).getName()).isNull();
    assertThat(testCases.get(0).getDescription()).isNull();
    assertThat(testCases.get(0).getPath()).isNotNull();
  }

  @Test
  public void testGetTestCasesWhenNotDefined() {
    bpmnSupport = BpmnSupport.of(simple.resolve("noTestCases.bpmn"));

    List<TestCase> testCases = bpmnSupport.getTestCases();
    assertThat(testCases).isNotNull();
    assertThat(testCases).hasSize(0);
  }

  @Test
  public void testHas() {
    bpmnSupport = BpmnSupport.of(simple.resolve("simple.bpmn"));

    assertThat(bpmnSupport.has("startEvent")).isTrue();
    assertThat(bpmnSupport.has("not-existing")).isFalse();
  }

  @Test
  public void testIsBoundaryEvent() {
    bpmnSupport = BpmnSupport.of(advanced.resolve("userTaskError.bpmn"));
    assertThat(bpmnSupport.isBoundaryEvent("errorBoundaryEvent")).isTrue();
  }

  @Test
  public void testIsCallActivity() {
    bpmnSupport = BpmnSupport.of(simple.resolve("simpleCallActivity.bpmn"));
    assertThat(bpmnSupport.isCallActivity("callActivity")).isTrue();
  }

  @Test
  public void testIsEventBasedGateway() {
    bpmnSupport = BpmnSupport.of(simple.resolve("simpleEventBasedGateway.bpmn"));
    assertThat(bpmnSupport.isEventBasedGateway("eventBasedGateway")).isTrue();
  }

  @Test
  public void testIsExternalTask() {
    bpmnSupport = BpmnSupport.of(simple.resolve("simpleExternalTask.bpmn"));
    assertThat(bpmnSupport.isExternalTask("externalTask")).isTrue();
  }

  @Test
  public void testIsIntermediateCatchEvent() {
    bpmnSupport = BpmnSupport.of(simple.resolve("simpleMessageCatchEvent.bpmn"));
    assertThat(bpmnSupport.isIntermediateCatchEvent("messageCatchEvent")).isTrue();

    bpmnSupport = BpmnSupport.of(simple.resolve("simpleSignalCatchEvent.bpmn"));
    assertThat(bpmnSupport.isIntermediateCatchEvent("signalCatchEvent")).isTrue();

    bpmnSupport = BpmnSupport.of(simple.resolve("simpleTimerCatchEvent.bpmn"));
    assertThat(bpmnSupport.isIntermediateCatchEvent("timerCatchEvent")).isTrue();
  }

  @Test
  public void testIsIntermediateThrowEvent() {
    bpmnSupport = BpmnSupport.of(advanced.resolve("linkEvent.bpmn"));
    assertThat(bpmnSupport.isIntermediateThrowEvent("linkThrowEventA")).isTrue();
    assertThat(bpmnSupport.isIntermediateThrowEvent("linkCatchEventA")).isFalse();
  }

  @Test
  public void testIsProcessEnd() {
    bpmnSupport = BpmnSupport.of(simple.resolve("simpleSubProcess.bpmn"));
    assertThat(bpmnSupport.isProcessEnd("startEvent")).isFalse();
    assertThat(bpmnSupport.isProcessEnd("subProcessStartEvent")).isFalse();
    assertThat(bpmnSupport.isProcessEnd("subProcessEndEvent")).isFalse();
    assertThat(bpmnSupport.isProcessEnd("endEvent")).isTrue();
  }

  @Test
  public void testIsReceiveTask() {
    bpmnSupport = BpmnSupport.of(simple.resolve("simpleReceiveTask.bpmn"));
    assertThat(bpmnSupport.isReceiveTask("receiveTask")).isTrue();
  }

  @Test
  public void testIsUserTask() {
    bpmnSupport = BpmnSupport.of(simple.resolve("simpleUserTask.bpmn"));
    assertThat(bpmnSupport.isUserTask("userTask")).isTrue();
  }

  @Test
  public void shouldWorkWithCollaboration() {
    bpmnSupport = BpmnSupport.of(simple.resolve("simpleCollaboration.bpmn"));

    assertThat(bpmnSupport.getProcessId()).isEqualTo("simpleCollaboration");
    assertThat(bpmnSupport.has("startEvent")).isTrue();
    assertThat(bpmnSupport.has("endEvent")).isTrue();
  }
}
