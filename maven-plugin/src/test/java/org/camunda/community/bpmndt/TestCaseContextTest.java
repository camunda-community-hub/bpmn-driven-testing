package org.camunda.community.bpmndt;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

public class TestCaseContextTest {

  private BpmnSupport bpmnSupport;
  private TestCaseContext ctx;

  private Path resources;
  private Path simple;

  @Before
  public void setUp() {
    resources = Paths.get("./src/test/resources/bpmn");
    simple = Paths.get("./src/test/it/simple/src/main/resources/simple.bpmn");
  }

  @Test
  public void testGetActivity() {
    bpmnSupport = BpmnSupport.of(simple);
    ctx = new TestCaseContext(bpmnSupport, bpmnSupport.getTestCases().get(0));

    assertThat(ctx.getStartActivity(), nullValue());
    assertThat(ctx.getEndActivity(), nullValue());

    ctx.addActivity(new TestCaseActivity(bpmnSupport.get("startEvent")));
    ctx.addActivity(new TestCaseActivity(bpmnSupport.get("endEvent")));

    assertThat(ctx.getStartActivity(), is(ctx.getActivities().get(0)));
    assertThat(ctx.getEndActivity(), is(ctx.getActivities().get(1)));
  }

  @Test
  public void testGetClassName() {
    bpmnSupport = BpmnSupport.of(simple);
    ctx = new TestCaseContext(bpmnSupport, bpmnSupport.getTestCases().get(0));
    assertThat(ctx.getClassName(), equalTo("TC_startEvent__endEvent"));

    bpmnSupport = BpmnSupport.of(resources.resolve("happyPath.bpmn"));
    ctx = new TestCaseContext(bpmnSupport, bpmnSupport.getTestCases().get(0));
    assertThat(ctx.getClassName(), equalTo("TC_Happy_Path"));
  }

  @Test
  public void testGetName() {
    bpmnSupport = BpmnSupport.of(simple);
    ctx = new TestCaseContext(bpmnSupport, bpmnSupport.getTestCases().get(0));
    assertThat(ctx.getName(), equalTo("startEvent__endEvent"));

    bpmnSupport = BpmnSupport.of(resources.resolve("happyPath.bpmn"));
    ctx = new TestCaseContext(bpmnSupport, bpmnSupport.getTestCases().get(0));
    assertThat(ctx.getName(), equalTo("Happy_Path"));
  }

  @Test
  public void testGetPackageName() {
    bpmnSupport = BpmnSupport.of(simple);
    ctx = new TestCaseContext(bpmnSupport, bpmnSupport.getTestCases().get(0));
    assertThat(ctx.getPackageName(), equalTo("simple"));

    bpmnSupport = BpmnSupport.of(resources.resolve("happyPath.bpmn"));
    ctx = new TestCaseContext(bpmnSupport, bpmnSupport.getTestCases().get(0));
    assertThat(ctx.getPackageName(), equalTo("happy_path"));
  }

  @Test
  public void testIsEmpty() {
    bpmnSupport = BpmnSupport.of(resources.resolve("empty.bpmn"));
    ctx = new TestCaseContext(bpmnSupport, bpmnSupport.getTestCases().get(0));

    assertThat(ctx.isPathEmpty(), is(true));
    assertThat(ctx.isPathIncomplete(), is(false));
    assertThat(ctx.isPathInvalid(), is(false));
    assertThat(ctx.isValid(), is(false));
  }

  @Test
  public void testIsIncomplete() {
    bpmnSupport = BpmnSupport.of(resources.resolve("incomplete.bpmn"));
    ctx = new TestCaseContext(bpmnSupport, bpmnSupport.getTestCases().get(0));

    assertThat(ctx.isPathEmpty(), is(false));
    assertThat(ctx.isPathIncomplete(), is(true));
    assertThat(ctx.isPathInvalid(), is(false));
    assertThat(ctx.isValid(), is(false));
  }

  @Test
  public void testIsInvalid() {
    bpmnSupport = BpmnSupport.of(resources.resolve("invalid.bpmn"));
    ctx = new TestCaseContext(bpmnSupport, bpmnSupport.getTestCases().get(0));

    ctx.addInvalidFlowNodeId("a");
    ctx.addInvalidFlowNodeId("b");

    assertThat(ctx.isPathEmpty(), is(false));
    assertThat(ctx.isPathIncomplete(), is(false));
    assertThat(ctx.isPathInvalid(), is(true));
    assertThat(ctx.isValid(), is(false));
  }

  @Test
  public void testIsValid() {
    bpmnSupport = BpmnSupport.of(resources.resolve("happyPath.bpmn"));
    ctx = new TestCaseContext(bpmnSupport, bpmnSupport.getTestCases().get(0));

    assertThat(ctx.isPathEmpty(), is(false));
    assertThat(ctx.isPathIncomplete(), is(false));
    assertThat(ctx.isPathInvalid(), is(false));
    assertThat(ctx.isValid(), is(true));
  }
}
