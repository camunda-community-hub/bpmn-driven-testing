package org.camunda.community.bpmndt.cmd;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.camunda.community.bpmndt.BpmnSupport;
import org.camunda.community.bpmndt.GeneratorContext;
import org.camunda.community.bpmndt.TestCaseActivity;
import org.camunda.community.bpmndt.TestCaseActivityScope;
import org.camunda.community.bpmndt.TestCaseContext;
import org.junit.Before;
import org.junit.Test;

public class BuildTestCaseContextTest {

  private Path basePath;
  private BpmnSupport bpmnSupport;
  private GeneratorContext gCtx;

  private BuildTestCaseContext cmd;

  private TestCaseActivity activity;
  private TestCaseActivityScope scope;

  @Before
  public void setUp() {
    basePath = Paths.get("./src/test/it");

    gCtx = new GeneratorContext();
  }

  @Test
  public void testMultiInstanceScopeNested() {
    bpmnSupport = BpmnSupport.of(basePath.resolve("advanced-multi-instance/src/main/resources/scopeNested.bpmn"));
    cmd = new BuildTestCaseContext(gCtx, bpmnSupport);

    TestCaseContext ctx = cmd.apply(bpmnSupport.getTestCases().get(0), 0);
    assertThat(ctx.getActivities().size(), is(3));

    // subProcess
    activity = ctx.getActivities().get(1);
    assertThat(activity.getId(), equalTo("subProcess"));
    assertThat(activity.hasParent(), is(false));
    assertThat(activity.isMultiInstance(), is(true));
    assertThat(activity.isScope(), is(true));

    scope = (TestCaseActivityScope) activity;
    assertThat(scope.getActivities().size(), is(3));

    // nestedSubProcess
    activity = scope.getActivities().get(1);
    assertThat(activity.getId(), equalTo("nestedSubProcess"));
    assertThat(activity.hasParent(), is(true));
    assertThat(activity.isMultiInstance(), is(true));
    assertThat(activity.isScope(), is(true));

    scope = (TestCaseActivityScope) activity;
    assertThat(scope.getActivities().size(), is(3));

    // userTask
    activity = scope.getActivities().get(1);
    assertThat(activity.getId(), equalTo("userTask"));
    assertThat(activity.hasParent(), is(true));
    assertThat(activity.isMultiInstance(), is(false));
    assertThat(activity.isScope(), is(false));
  }

  @Test
  public void testSubProcessNested() {
    bpmnSupport = BpmnSupport.of(basePath.resolve("simple/src/main/resources/simpleSubProcessNested.bpmn"));
    cmd = new BuildTestCaseContext(gCtx, bpmnSupport);

    TestCaseContext ctx = cmd.apply(bpmnSupport.getTestCases().get(0), 0);
    assertThat(ctx.getActivities().size(), is(7));

    // all activities beside startEvent and endEvent
    for (int i = 1; i < ctx.getActivities().size() - 1; i++) {
      TestCaseActivity activity = ctx.getActivities().get(i);

      assertThat(activity.hasParent(), is(false));
      assertThat(activity.hasMultiInstanceParent(), is(false));
    }
  }
}
