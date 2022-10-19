package org.camunda.community.bpmndt;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.camunda.community.bpmndt.model.Path;
import org.camunda.community.bpmndt.model.TestCase;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.Before;
import org.junit.Test;

public class BpmnSupportTest {

  private BpmnSupport bpmnSupport;

  private java.nio.file.Path advanced;
  private java.nio.file.Path advancedMultiInstance;
  private java.nio.file.Path simple;

  @Before
  public void setUp() {
    advanced = TestPaths.advanced();
    advancedMultiInstance = TestPaths.advancedMultiInstance();
    simple = TestPaths.simple();
  }

  @Test
  public void testToJavaLiteral() {
    assertThat(BpmnSupport.toJavaLiteral("Happy Path"), equalTo("happy_path"));
    assertThat(BpmnSupport.toJavaLiteral("Happy-Path"), equalTo("happy_path"));
    assertThat(BpmnSupport.toJavaLiteral("Happy Path!"), equalTo("happy_path_"));
    assertThat(BpmnSupport.toJavaLiteral("startEvent__endEvent"), equalTo("startevent__endevent"));
    assertThat(BpmnSupport.toJavaLiteral("123\nABC"), equalTo("_123_abc"));
    assertThat(BpmnSupport.toJavaLiteral("New"), equalTo("_new"));
  }

  @Test
  public void testToLiteral() {
    assertThat(BpmnSupport.toLiteral("Happy Path"), equalTo("Happy_Path"));
    assertThat(BpmnSupport.toLiteral("Happy-Path"), equalTo("Happy_Path"));
    assertThat(BpmnSupport.toLiteral("Happy Path!"), equalTo("Happy_Path_"));
    assertThat(BpmnSupport.toLiteral("startEvent__endEvent"), equalTo("startEvent__endEvent"));
    assertThat(BpmnSupport.toLiteral("123\nABC"), equalTo("123_ABC"));
  }

  @Test
  public void testOfErrorFileNotFound() {
    try {
      BpmnSupport.of(Paths.get("./not-existing"));
      fail("should throw RuntimeException");
    } catch (RuntimeException e) {
      assertThat(e.getCause(), notNullValue());
      assertThat(e.getCause() instanceof IOException, is(true));
    }
  }

  @Test
  public void testOfErrorFileNotRead() {
    try {
      BpmnSupport.of(Paths.get("."));
      fail("should throw RuntimeException");
    } catch (RuntimeException e) {
      assertThat(e.getCause(), notNullValue());
      assertThat(e.getCause() instanceof IOException, is(true));
    }
  }

  @Test
  public void testGetMultiInstance() {
    bpmnSupport = BpmnSupport.of(advancedMultiInstance.resolve("sequential.bpmn"));
    assertThat(bpmnSupport.getMultiInstance("multiInstanceManualTask"), notNullValue());
  }

  @Test
  public void testGetTestCases() {
    List<TestCase> testCases;

    bpmnSupport = BpmnSupport.of(TestPaths.resources("happyPath.bpmn"));

    testCases = bpmnSupport.getTestCases();
    assertThat(testCases, hasSize(1));

    assertThat(testCases.get(0).getName(), equalTo("Happy Path"));
    assertThat(testCases.get(0).getDescription(), equalTo("The happy path"));

    Path path = testCases.get(0).getPath();
    assertThat(path, notNullValue());
    assertThat(path.getFlowNodeIds(), hasSize(2));
    assertThat(path.getFlowNodeIds().get(0), equalTo("startEvent"));
    assertThat(path.getFlowNodeIds().get(1), equalTo("endEvent"));

    bpmnSupport = BpmnSupport.of(simple.resolve("simple.bpmn"));

    testCases = bpmnSupport.getTestCases();
    assertThat(testCases, hasSize(1));

    assertThat(testCases.get(0).getName(), nullValue());
    assertThat(testCases.get(0).getDescription(), nullValue());
    assertThat(testCases.get(0).getPath(), notNullValue());
  }

  @Test
  public void testGetTestCasesWhenNotDefined() {
    bpmnSupport = BpmnSupport.of(simple.resolve("noTestCases.bpmn"));

    List<TestCase> testCases = bpmnSupport.getTestCases();
    assertThat(testCases, notNullValue());
    assertThat(testCases, hasSize(0));
  }

  @Test
  public void testHas() {
    bpmnSupport = BpmnSupport.of(simple.resolve("simple.bpmn"));

    assertThat(bpmnSupport.has("startEvent"), is(true));
    assertThat(bpmnSupport.has("not-existing"), is(false));
  }

  @Test
  public void testIsBoundaryEvent() {
    bpmnSupport = BpmnSupport.of(advanced.resolve("userTaskError.bpmn"));
    assertThat(bpmnSupport.isBoundaryEvent("errorBoundaryEvent"), is(true));
  }

  @Test
  public void testIsCallActivity() {
    bpmnSupport = BpmnSupport.of(simple.resolve("simpleCallActivity.bpmn"));
    assertThat(bpmnSupport.isCallActivity("callActivity"), is(true));
  }

  @Test
  public void testIsEventBasedGateway() {
    bpmnSupport = BpmnSupport.of(simple.resolve("simpleEventBasedGateway.bpmn"));
    assertThat(bpmnSupport.isEventBasedGateway("eventBasedGateway"), is(true));
  }

  @Test
  public void testIsExternalTask() {
    bpmnSupport = BpmnSupport.of(simple.resolve("simpleExternalTask.bpmn"));
    assertThat(bpmnSupport.isExternalTask("externalTask"), is(true));
  }

  @Test
  public void testIsIntermediateCatchEvent() {
    bpmnSupport = BpmnSupport.of(simple.resolve("simpleMessageCatchEvent.bpmn"));
    assertThat(bpmnSupport.isIntermediateCatchEvent("messageCatchEvent"), is(true));

    bpmnSupport = BpmnSupport.of(simple.resolve("simpleSignalCatchEvent.bpmn"));
    assertThat(bpmnSupport.isIntermediateCatchEvent("signalCatchEvent"), is(true));

    bpmnSupport = BpmnSupport.of(simple.resolve("simpleTimerCatchEvent.bpmn"));
    assertThat(bpmnSupport.isIntermediateCatchEvent("timerCatchEvent"), is(true));
  }

  @Test
  public void testIsProcessEnd() {
    bpmnSupport = BpmnSupport.of(simple.resolve("simpleSubProcess.bpmn"));
    assertThat(bpmnSupport.isProcessEnd("startEvent"), is(false));
    assertThat(bpmnSupport.isProcessEnd("subProcessStartEvent"), is(false));
    assertThat(bpmnSupport.isProcessEnd("subProcessEndEvent"), is(false));
    assertThat(bpmnSupport.isProcessEnd("endEvent"), is(true));
  }

  @Test
  public void testIsReceiveTask() {
    bpmnSupport = BpmnSupport.of(simple.resolve("simpleReceiveTask.bpmn"));
    assertThat(bpmnSupport.isReceiveTask("receiveTask"), is(true));
  }

  @Test
  public void testIsUserTask() {
    bpmnSupport = BpmnSupport.of(simple.resolve("simpleUserTask.bpmn"));
    assertThat(bpmnSupport.isUserTask("userTask"), is(true));
  }

  @Test
  public void shouldWorkWithCollaboration() {
    bpmnSupport = BpmnSupport.of(simple.resolve("simpleCollaboration.bpmn"));

    assertThat(bpmnSupport.getProcessId(), equalTo("simpleCollaboration"));
    assertThat(bpmnSupport.has("startEvent"), is(true));
    assertThat(bpmnSupport.has("endEvent"), is(true));
  }
}
