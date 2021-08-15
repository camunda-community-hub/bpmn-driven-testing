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
import org.junit.Before;
import org.junit.Test;

public class BpmnSupportTest {

  private BpmnSupport bpmnSupport;

  private java.nio.file.Path advanced;
  private java.nio.file.Path simple;

  @Before
  public void setUp() {
    advanced = Paths.get("./src/test/it/advanced/src/main/resources");
    simple = Paths.get("./src/test/it/simple/src/main/resources");
  }

  @Test
  public void testToJavaLiteral() {
    assertThat(BpmnSupport.toJavaLiteral("Happy Path"), equalTo("Happy_Path"));
    assertThat(BpmnSupport.toJavaLiteral("Happy-Path"), equalTo("Happy_Path"));
    assertThat(BpmnSupport.toJavaLiteral("Happy Path!"), equalTo("Happy_Path_"));
    assertThat(BpmnSupport.toJavaLiteral("startEvent__endEvent"), equalTo("startEvent__endEvent"));
    assertThat(BpmnSupport.toJavaLiteral("123\nABC"), equalTo("123_ABC"));
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
  public void testGetTestCases() {
    List<TestCase> testCases;

    bpmnSupport = BpmnSupport.of(Paths.get("./src/test/resources/bpmn/happyPath.bpmn"));

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
  public void testGetNoTestCases() {
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
