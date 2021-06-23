package org.camunda.bpm.extension.bpmndt.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.extension.bpmndt.BpmnNode;
import org.camunda.bpm.extension.bpmndt.BpmnSupport;
import org.camunda.bpm.extension.bpmndt.type.Path;
import org.camunda.bpm.extension.bpmndt.type.TestCase;
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants;
import org.junit.Before;
import org.junit.Test;

public class BpmnSupportImplTest {

  private BpmnSupportImpl bpmnSupport;

  private java.nio.file.Path basePath;

  @Before
  public void setUp() {
    basePath = Paths.get("./src/test/resources/bpmn");
  }

  @Test
  public void testConvert() {
    assertThat(BpmnSupportImpl.convert("Happy Path"), equalTo("Happy_Path"));
    assertThat(BpmnSupportImpl.convert("Happy-Path"), equalTo("Happy_Path"));
    assertThat(BpmnSupportImpl.convert("Happy Path!"), equalTo("Happy_Path_"));
    assertThat(BpmnSupportImpl.convert("startEvent__endEvent"), equalTo("startEvent__endEvent"));
    assertThat(BpmnSupportImpl.convert("123\nABC"), equalTo("123_ABC"));
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

    bpmnSupport = BpmnSupportImpl.of(basePath.resolve("happyPath.bpmn"));

    testCases = bpmnSupport.getTestCases();
    assertThat(testCases, hasSize(1));

    assertThat(testCases.get(0).getName(), equalTo("Happy_Path"));
    assertThat(testCases.get(0).getDescription(), equalTo("The happy path"));

    Path path = testCases.get(0).getPath();
    assertThat(path, notNullValue());
    assertThat(path.getFlowNodeIds(), hasSize(2));
    assertThat(path.getFlowNodeIds().get(0), equalTo("startEvent"));
    assertThat(path.getFlowNodeIds().get(1), equalTo("endEvent"));
    assertThat(path.getStart(), equalTo("startEvent"));
    assertThat(path.getEnd(), equalTo("endEvent"));

    bpmnSupport = BpmnSupportImpl.of(basePath.resolve("simple.bpmn"));

    testCases = bpmnSupport.getTestCases();
    assertThat(testCases, hasSize(1));

    assertThat(testCases.get(0).getName(), equalTo("startEvent__endEvent"));
    assertThat(testCases.get(0).getDescription(), nullValue());
    assertThat(testCases.get(0).getPath(), notNullValue());
  }

  @Test
  public void testGetNoTestCases() {
    bpmnSupport = BpmnSupportImpl.of(basePath.resolve("noTestCases.bpmn"));

    List<TestCase> testCases = bpmnSupport.getTestCases();
    assertThat(testCases, notNullValue());
    assertThat(testCases, hasSize(0));
  }

  @Test
  public void testHas() {
    bpmnSupport = BpmnSupportImpl.of(basePath.resolve("simple.bpmn"));

    assertThat(bpmnSupport.has(Collections.emptyList()), is(true));
    assertThat(bpmnSupport.has(Collections.singletonList("startEvent")), is(true));
    assertThat(bpmnSupport.has(Collections.singletonList("not-existing")), is(false));
    assertThat(bpmnSupport.has("startEvent"), is(true));
    assertThat(bpmnSupport.has("not-existing"), is(false));
  }

  @Test
  public void testIs() {
    bpmnSupport = BpmnSupportImpl.of(basePath.resolve("simple.bpmn"));

    BpmnNodeImpl node;

    node = (BpmnNodeImpl) bpmnSupport.get("startEvent");
    assertThat(node.is(BpmnModelConstants.BPMN_ELEMENT_START_EVENT), is(true));
    assertThat(node.is("startEvent"), is(true));

    node = (BpmnNodeImpl) bpmnSupport.get("endEvent");
    assertThat(node.is(BpmnModelConstants.BPMN_ELEMENT_START_EVENT), is(false));
    assertThat(node.is(null), is(false));
  }

  @Test
  public void testIsJob() {
    bpmnSupport = BpmnSupportImpl.of(basePath.resolve("simpleAsync.bpmn"));

    BpmnNode node;

    node = bpmnSupport.get("startEvent");
    assertThat(node.isJob(), is(true));
    node = bpmnSupport.get("endEvent");
    assertThat(node.isJob(), is(true));

    bpmnSupport = BpmnSupportImpl.of(basePath.resolve("simpleMessageCatchEvent.bpmn"));

    node = bpmnSupport.get("messageCatchEvent");
    assertThat(node.isJob(), is(false));

    bpmnSupport = BpmnSupportImpl.of(basePath.resolve("simpleTimerCatchEvent.bpmn"));

    node = bpmnSupport.get("startEvent");
    assertThat(node.isJob(), is(false));
    node = bpmnSupport.get("timerCatchEvent");
    assertThat(node.isJob(), is(true));
    node = bpmnSupport.get("endEvent");
    assertThat(node.isJob(), is(false));
  }

  @Test
  public void shouldWorkWithCollaboration() {
    bpmnSupport = BpmnSupportImpl.of(basePath.resolve("simpleCollaboration.bpmn"));

    assertThat(bpmnSupport.getProcessId(), equalTo("simpleCollaboration"));
    assertThat(bpmnSupport.has("startEvent"), is(true));
    assertThat(bpmnSupport.has("endEvent"), is(true));
  }
}
