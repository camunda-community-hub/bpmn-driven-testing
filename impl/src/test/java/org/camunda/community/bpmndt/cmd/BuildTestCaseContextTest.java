package org.camunda.community.bpmndt.cmd;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.camunda.community.bpmndt.GeneratorContext;
import org.camunda.community.bpmndt.GeneratorStrategy;
import org.camunda.community.bpmndt.TestCaseContext;
import org.camunda.community.bpmndt.model.TestCase;
import org.camunda.community.bpmndt.model.TestCases;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BuildTestCaseContextTest {

  private TestCaseContext ctx;

  private GeneratorContext gCtx;

  @BeforeEach
  public void setUp() {
    gCtx = new GeneratorContext();
    gCtx.setMainResourcePath(Paths.get("./src/test/resources"));
    gCtx.setPackageName("org.example");
  }

  @Test
  public void testBuild() {
    ctx = of(TestPaths.simple("special/happyPath.bpmn"));
    assertThat(ctx.getClassName()).isEqualTo("TC_Happy_Path");
    assertThat(ctx.getName()).isEqualTo("Happy_Path");
    assertThat(ctx.getPackageName()).isEqualTo("org.example.happypath");
    assertThat(ctx.getResourceName()).endsWith("special/happyPath.bpmn");
    assertThat(ctx.getStrategy("startEvent")).isNotNull();
    assertThat(ctx.getStrategy("endEvent")).isNotNull();
    assertThat(ctx.getStrategy("xyz")).isNull();
    assertThat(ctx.getTestCase());
  }

  @Test
  public void testBuildMultiInstanceScopeInner() {
    ctx = of(TestPaths.advancedMultiInstance("scopeInner.bpmn"), 0);
    assertThat(ctx.getTestCase().getActivities()).hasSize(2);
    assertThat(ctx.getTestCase().getActivities().get(0).getId()).isEqualTo("subProcessStartEvent");
    assertThat(ctx.getTestCase().getActivities().get(1).getId()).isEqualTo("subProcessEndEvent");
    assertThat(ctx.getMultiInstanceScopes()).isEmpty();
    assertThat(ctx.getName()).isEqualTo("subProcessStartEvent__subProcessEndEvent");
    assertThat(ctx.getStrategy("subProcess")).isNull();

    ctx = of(TestPaths.advancedMultiInstance("scopeInner.bpmn"), 1);
    assertThat(ctx.getTestCase().getActivities()).hasSize(3);
    assertThat(ctx.getTestCase().getActivities().get(0).getId()).isEqualTo("startEvent");
    assertThat(ctx.getTestCase().getActivities().get(1).getId()).isEqualTo("subProcessStartEvent");
    assertThat(ctx.getTestCase().getActivities().get(2).getId()).isEqualTo("subProcessEndEvent");
    assertThat(ctx.getMultiInstanceScopes()).hasSize(1);
    assertThat(ctx.getMultiInstanceScopes().get(0).getId()).isEqualTo("subProcess");
    assertThat(ctx.getName()).isEqualTo("startEvent__subProcessEndEvent");
    assertThat(ctx.getStrategy("subProcess")).isNotNull();

    ctx = of(TestPaths.advancedMultiInstance("scopeInner.bpmn"), 2);
    assertThat(ctx.getTestCase().getActivities()).hasSize(3);
    assertThat(ctx.getTestCase().getActivities().get(0).getId()).isEqualTo("subProcessStartEvent");
    assertThat(ctx.getTestCase().getActivities().get(1).getId()).isEqualTo("subProcessEndEvent");
    assertThat(ctx.getTestCase().getActivities().get(2).getId()).isEqualTo("endEvent");
    assertThat(ctx.getMultiInstanceScopes()).isEmpty();
    assertThat(ctx.getName()).isEqualTo("subProcessStartEvent__endEvent");
    assertThat(ctx.getStrategy("subProcess")).isNull();
  }

  @Test
  public void testBuildMultiInstanceScopeNested() {
    ctx = of(TestPaths.advancedMultiInstance("scopeNested.bpmn"), 0);
    assertThat(ctx.getTestCase().getActivities()).hasSize(7);
    assertThat(ctx.getTestCase().getActivities().get(0).getId()).isEqualTo("startEvent");
    assertThat(ctx.getTestCase().getActivities().get(1).getId()).isEqualTo("subProcessStartEvent");
    assertThat(ctx.getTestCase().getActivities().get(2).getId()).isEqualTo("nestedSubProcessStartEvent");
    assertThat(ctx.getTestCase().getActivities().get(3).getId()).isEqualTo("userTask");
    assertThat(ctx.getTestCase().getActivities().get(4).getId()).isEqualTo("nestedSubProcessEndEvent");
    assertThat(ctx.getTestCase().getActivities().get(5).getId()).isEqualTo("subProcessEndEvent");
    assertThat(ctx.getTestCase().getActivities().get(6).getId()).isEqualTo("endEvent");
    assertThat(ctx.getMultiInstanceScopes()).hasSize(2);
    assertThat(ctx.getMultiInstanceScopes().get(0).getId()).isEqualTo("subProcess");
    assertThat(ctx.getMultiInstanceScopes().get(1).getId()).isEqualTo("nestedSubProcess");
    assertThat(ctx.getStrategy("subProcess")).isNotNull();
    assertThat(ctx.getStrategy("nestedSubProcess")).isNotNull();

    List<GeneratorStrategy> strategies = new GetStrategies().apply(ctx, ctx.getMultiInstanceScopes().get(0).getActivities());
    assertThat(strategies).hasSize(3);
    assertThat(strategies.get(0).getActivity().getId()).isEqualTo("subProcessStartEvent");
    assertThat(strategies.get(1).getActivity().getId()).isEqualTo("nestedSubProcess");
    assertThat(strategies.get(2).getActivity().getId()).isEqualTo("subProcessEndEvent");

    ctx = of(TestPaths.advancedMultiInstance("scopeNested.bpmn"), 1);
    assertThat(ctx.getTestCase().getActivities()).hasSize(6);
    assertThat(ctx.getTestCase().getActivities().get(0).getId()).isEqualTo("subProcessStartEvent");
    assertThat(ctx.getTestCase().getActivities().get(1).getId()).isEqualTo("nestedSubProcessStartEvent");
    assertThat(ctx.getTestCase().getActivities().get(2).getId()).isEqualTo("userTask");
    assertThat(ctx.getTestCase().getActivities().get(3).getId()).isEqualTo("nestedSubProcessEndEvent");
    assertThat(ctx.getTestCase().getActivities().get(4).getId()).isEqualTo("subProcessEndEvent");
    assertThat(ctx.getTestCase().getActivities().get(5).getId()).isEqualTo("endEvent");
    assertThat(ctx.getMultiInstanceScopes()).hasSize(1);
    assertThat(ctx.getMultiInstanceScopes().get(0).getId()).isEqualTo("nestedSubProcess");
    assertThat(ctx.getStrategy("subProcess")).isNull();
    assertThat(ctx.getStrategy("nestedSubProcess")).isNotNull();

    ctx = of(TestPaths.advancedMultiInstance("scopeNested.bpmn"), 2);
    assertThat(ctx.getTestCase().getActivities()).hasSize(5);
    assertThat(ctx.getTestCase().getActivities().get(0).getId()).isEqualTo("nestedSubProcessStartEvent");
    assertThat(ctx.getTestCase().getActivities().get(1).getId()).isEqualTo("userTask");
    assertThat(ctx.getTestCase().getActivities().get(2).getId()).isEqualTo("nestedSubProcessEndEvent");
    assertThat(ctx.getTestCase().getActivities().get(3).getId()).isEqualTo("subProcessEndEvent");
    assertThat(ctx.getTestCase().getActivities().get(4).getId()).isEqualTo("endEvent");
    assertThat(ctx.getMultiInstanceScopes()).isEmpty();
    assertThat(ctx.getStrategy("subProcess")).isNull();
    assertThat(ctx.getStrategy("nestedSubProcess")).isNull();
  }

  private TestCaseContext of(Path bpmnFile) {
    return of(bpmnFile, 0);
  }

  private TestCaseContext of(Path bpmnFile, int testCaseIndex) {
    List<TestCase> testCases = TestCases.of(bpmnFile).get();
    return new BuildTestCaseContext(gCtx, bpmnFile).apply(testCases.get(testCaseIndex));
  }
}
