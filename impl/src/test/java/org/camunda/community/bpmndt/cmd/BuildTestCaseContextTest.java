package org.camunda.community.bpmndt.cmd;

import static com.google.common.truth.Truth.assertThat;

import org.camunda.community.bpmndt.BpmnSupport;
import org.camunda.community.bpmndt.GeneratorContext;
import org.camunda.community.bpmndt.TestCaseActivity;
import org.camunda.community.bpmndt.TestCaseActivityScope;
import org.camunda.community.bpmndt.TestCaseActivityType;
import org.camunda.community.bpmndt.TestCaseContext;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BuildTestCaseContextTest {

  private BpmnSupport bpmnSupport;
  private GeneratorContext gCtx;

  private BuildTestCaseContext cmd;

  private TestCaseActivity activity;
  private TestCaseActivityScope scope;

  @BeforeEach
  public void setUp() {
    gCtx = new GeneratorContext();
  }

  @Test
  public void testLinkEvent() {
    bpmnSupport = BpmnSupport.of(TestPaths.advanced("linkEvent.bpmn"));
    cmd = new BuildTestCaseContext(gCtx, bpmnSupport);

    TestCaseContext ctx = cmd.apply(bpmnSupport.getTestCases().get(0), 0);
    assertThat(ctx.getActivities()).hasSize(3);

    activity = ctx.getActivities().get(1);
    assertThat(activity.getId()).isEqualTo("linkThrowEventA");
    assertThat(activity.getType()).isEqualTo(TestCaseActivityType.LINK_THROW);

    activity = ctx.getActivities().get(2);
    assertThat(activity.getId()).isEqualTo("linkCatchEventA");
    assertThat(activity.getType()).isEqualTo(TestCaseActivityType.OTHER);
  }

  @Test
  public void testMultiInstanceScopeNested() {
    bpmnSupport = BpmnSupport.of(TestPaths.advancedMultiInstance("scopeNested.bpmn"));
    cmd = new BuildTestCaseContext(gCtx, bpmnSupport);

    TestCaseContext ctx = cmd.apply(bpmnSupport.getTestCases().get(0), 0);
    assertThat(ctx.getActivities()).hasSize(3);

    // subProcess
    activity = ctx.getActivities().get(1);
    assertThat(activity.getId()).isEqualTo("subProcess");
    assertThat(activity.hasParent()).isFalse();
    assertThat(activity.isMultiInstance()).isTrue();
    assertThat(activity.isScope()).isTrue();

    scope = (TestCaseActivityScope) activity;
    assertThat(scope.getActivities()).hasSize(3);

    // nestedSubProcess
    activity = scope.getActivities().get(1);
    assertThat(activity.getId()).isEqualTo("nestedSubProcess");
    assertThat(activity.hasParent()).isTrue();
    assertThat(activity.isMultiInstance()).isTrue();
    assertThat(activity.isScope()).isTrue();

    scope = (TestCaseActivityScope) activity;
    assertThat(scope.getActivities()).hasSize(3);

    // userTask
    activity = scope.getActivities().get(1);
    assertThat(activity.getId()).isEqualTo("userTask");
    assertThat(activity.hasParent()).isTrue();
    assertThat(activity.isMultiInstance()).isFalse();
    assertThat(activity.isScope()).isFalse();
  }

  @Test
  public void testSubProcessNested() {
    bpmnSupport = BpmnSupport.of(TestPaths.simple("simpleSubProcessNested.bpmn"));
    cmd = new BuildTestCaseContext(gCtx, bpmnSupport);

    TestCaseContext ctx = cmd.apply(bpmnSupport.getTestCases().get(0), 0);
    assertThat(ctx.getActivities()).hasSize(7);

    // all activities beside startEvent and endEvent
    for (int i = 1; i < ctx.getActivities().size() - 1; i++) {
      TestCaseActivity activity = ctx.getActivities().get(i);

      assertThat(activity.hasParent()).isFalse();
      assertThat(activity.hasMultiInstanceParent()).isFalse();
    }
  }
}
