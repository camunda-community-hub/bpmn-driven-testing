package org.camunda.community.bpmndt;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Path;

import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestCaseContextTest {

  private BpmnSupport bpmnSupport;
  private TestCaseContext ctx;

  private Path simple;

  @BeforeEach
  public void setUp() {
    simple = TestPaths.simple("simple.bpmn");
  }

  @Test
  public void testGetActivity() {
    bpmnSupport = BpmnSupport.of(simple);
    ctx = new TestCaseContext(bpmnSupport, bpmnSupport.getTestCases().get(0));

    assertThat(ctx.getStartActivity()).isNull();
    assertThat(ctx.getEndActivity()).isNull();

    ctx.addActivity(new TestCaseActivity(bpmnSupport.get("startEvent"), null));
    ctx.addActivity(new TestCaseActivity(bpmnSupport.get("endEvent"), null));

    assertThat(ctx.getStartActivity()).isSameInstanceAs(ctx.getActivities().get(0));
    assertThat(ctx.getEndActivity()).isSameInstanceAs(ctx.getActivities().get(1));
  }

  @Test
  public void testGetClassName() {
    bpmnSupport = BpmnSupport.of(simple);
    ctx = new TestCaseContext(bpmnSupport, bpmnSupport.getTestCases().get(0));
    assertThat(ctx.getClassName()).isEqualTo("TC_startEvent__endEvent");

    bpmnSupport = BpmnSupport.of(TestPaths.resources("happyPath.bpmn"));
    ctx = new TestCaseContext(bpmnSupport, bpmnSupport.getTestCases().get(0));
    assertThat(ctx.getClassName()).isEqualTo("TC_Happy_Path");
  }

  @Test
  public void testGetName() {
    bpmnSupport = BpmnSupport.of(simple);
    ctx = new TestCaseContext(bpmnSupport, bpmnSupport.getTestCases().get(0));
    assertThat(ctx.getName()).isEqualTo("startEvent__endEvent");

    bpmnSupport = BpmnSupport.of(TestPaths.resources(("happyPath.bpmn")));
    ctx = new TestCaseContext(bpmnSupport, bpmnSupport.getTestCases().get(0));
    assertThat(ctx.getName()).isEqualTo("Happy_Path");
  }

  @Test
  public void testGetPackageName() {
    bpmnSupport = BpmnSupport.of(simple);
    ctx = new TestCaseContext(bpmnSupport, bpmnSupport.getTestCases().get(0));
    assertThat(ctx.getPackageName()).isEqualTo("simple");

    bpmnSupport = BpmnSupport.of(TestPaths.resources(("happyPath.bpmn")));
    ctx = new TestCaseContext(bpmnSupport, bpmnSupport.getTestCases().get(0));
    assertThat(ctx.getPackageName()).isEqualTo("happy_path");
  }

  @Test
  public void testIsEmpty() {
    bpmnSupport = BpmnSupport.of(TestPaths.resources(("empty.bpmn")));
    ctx = new TestCaseContext(bpmnSupport, bpmnSupport.getTestCases().get(0));

    assertThat(ctx.isPathEmpty()).isTrue();
    assertThat(ctx.isPathIncomplete()).isFalse();
    assertThat(ctx.isPathInvalid()).isFalse();
    assertThat(ctx.isValid()).isFalse();
  }

  @Test
  public void testIsIncomplete() {
    bpmnSupport = BpmnSupport.of(TestPaths.resources(("incomplete.bpmn")));
    ctx = new TestCaseContext(bpmnSupport, bpmnSupport.getTestCases().get(0));

    assertThat(ctx.isPathEmpty()).isFalse();
    assertThat(ctx.isPathIncomplete()).isTrue();
    assertThat(ctx.isPathInvalid()).isFalse();
    assertThat(ctx.isValid()).isFalse();
  }

  @Test
  public void testIsInvalid() {
    bpmnSupport = BpmnSupport.of(TestPaths.resources(("invalid.bpmn")));
    ctx = new TestCaseContext(bpmnSupport, bpmnSupport.getTestCases().get(0));

    ctx.addInvalidFlowNodeId("a");
    ctx.addInvalidFlowNodeId("b");

    assertThat(ctx.isPathEmpty()).isFalse();
    assertThat(ctx.isPathIncomplete()).isFalse();
    assertThat(ctx.isPathInvalid()).isTrue();
    assertThat(ctx.isValid()).isFalse();
  }

  @Test
  public void testIsValid() {
    bpmnSupport = BpmnSupport.of(TestPaths.resources(("happyPath.bpmn")));
    ctx = new TestCaseContext(bpmnSupport, bpmnSupport.getTestCases().get(0));

    assertThat(ctx.isPathEmpty()).isFalse();
    assertThat(ctx.isPathIncomplete()).isFalse();
    assertThat(ctx.isPathInvalid()).isFalse();
    assertThat(ctx.isValid()).isTrue();
  }
}
