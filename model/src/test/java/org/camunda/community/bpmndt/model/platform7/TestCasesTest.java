package org.camunda.community.bpmndt.model.platform7;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Paths;

import org.camunda.bpm.model.bpmn.instance.SubProcess;
import org.camunda.community.bpmndt.test.Platform7TestPaths;
import org.camunda.community.bpmndt.test.Platform8TestPaths;
import org.junit.jupiter.api.Test;

public class TestCasesTest {

  @Test
  public void shouldFailWhenFileIsDirectory() {
    RuntimeException e = assertThrows(RuntimeException.class, () -> TestCasesImpl.of(Paths.get(".")));
    assertThat(e.getCause()).isNotNull();
    assertThat(e.getCause()).isInstanceOf(IOException.class);
  }

  @Test
  public void shouldFailWhenFileNotExists() {
    RuntimeException e = assertThrows(RuntimeException.class, () -> TestCasesImpl.of(Paths.get("./not-existing")));
    assertThat(e.getCause()).isNotNull();
    assertThat(e.getCause()).isInstanceOf(IOException.class);
  }

  @Test
  public void testCollaboration() {
    TestCases testCases = TestCases.of(Platform7TestPaths.advanced("collaboration.bpmn"));
    assertThat(testCases.get()).hasSize(4);
    assertThat(testCases.get("processA")).hasSize(1);
    assertThat(testCases.get("processB")).hasSize(0);
    assertThat(testCases.get("processC")).hasSize(3);
    assertThat(testCases.getProcessIds()).containsExactly("processA", "processB", "processC").inOrder();

    TestCase testCase = testCases.get("processA").get(0);
    assertThat(testCase.getActivities()).hasSize(4);
    assertThat(testCase.getEndActivity().getId()).isEqualTo("endEventA");
    assertThat(testCase.getStartActivity().getId()).isEqualTo("startEventA");

    testCase = testCases.get("processC").get(1);
    assertThat(testCase.getActivities()).hasSize(3);
    assertThat(testCase.getEndActivity().getId()).isEqualTo("subProcessEndC");
    assertThat(testCase.getStartActivity().getId()).isEqualTo("startEventC");
  }

  @Test
  public void testHappyPath() {
    TestCases testCases = TestCases.of(Platform7TestPaths.simple("special/happyPath.bpmn"));
    assertThat(testCases.get()).hasSize(1);
    assertThat(testCases.get("happyPath")).hasSize(1);
    assertThat(testCases.getModelInstance()).isNotNull();
    assertThat(testCases.getProcessIds()).containsExactly("happyPath");
    assertThat(testCases.isEmpty()).isFalse();

    TestCase testCase = testCases.get().get(0);
    assertThat(testCase.getActivities()).hasSize(2);
    assertThat(testCase.getDescription()).isEqualTo("The happy path");
    assertThat(testCase.getEndActivity().getId()).isEqualTo("endEvent");
    assertThat(testCase.getFlowNodeIds()).containsExactly("startEvent", "endEvent").inOrder();
    assertThat(testCase.getInvalidFlowNodeIds()).isEmpty();
    assertThat(testCase.getName()).isEqualTo("Happy Path");
    assertThat(testCase.getProcess()).isNotNull();
    assertThat(testCase.getProcessId()).isEqualTo("happyPath");
    assertThat(testCase.getProcessName()).isEqualTo("Happy Path Process");
    assertThat(testCase.getStartActivity().getId()).isEqualTo("startEvent");
    assertThat(testCase.hasEmptyPath()).isFalse();
    assertThat(testCase.hasIncompletePath()).isFalse();
    assertThat(testCase.hasInvalidPath()).isFalse();
    assertThat(testCase.isValid()).isTrue();
  }

  @Test
  public void testIsEmpty() {
    TestCases testCases = TestCases.of(Platform7TestPaths.simple("special/empty.bpmn"));
    assertThat(testCases.get()).hasSize(1);

    TestCase testCase = testCases.get().get(0);
    assertThat(testCase.getInvalidFlowNodeIds()).isEmpty();
    assertThat(testCase.hasEmptyPath()).isTrue();
    assertThat(testCase.hasIncompletePath()).isFalse();
    assertThat(testCase.hasInvalidPath()).isFalse();
    assertThat(testCase.isValid()).isFalse();
  }

  @Test
  public void testIsIncomplete() {
    TestCases testCases = TestCases.of(Platform7TestPaths.simple("special/incomplete.bpmn"));
    assertThat(testCases.get()).hasSize(1);

    TestCase testCase = testCases.get().get(0);
    assertThat(testCase.getInvalidFlowNodeIds()).isEmpty();
    assertThat(testCase.hasEmptyPath()).isFalse();
    assertThat(testCase.hasIncompletePath()).isTrue();
    assertThat(testCase.hasInvalidPath()).isFalse();
    assertThat(testCase.isValid()).isFalse();
  }

  @Test
  public void testIsInvalid() {
    TestCases testCases = TestCases.of(Platform7TestPaths.simple("special/invalid.bpmn"));
    assertThat(testCases.get()).hasSize(1);

    TestCase testCase = testCases.get().get(0);
    assertThat(testCase.getInvalidFlowNodeIds()).containsExactly("a", "b").inOrder();
    assertThat(testCase.hasEmptyPath()).isFalse();
    assertThat(testCase.hasIncompletePath()).isFalse();
    assertThat(testCase.hasInvalidPath()).isTrue();
    assertThat(testCase.isValid()).isFalse();
  }

  @Test
  public void testIsPlatform7() {
    TestCases testCases = TestCases.of(Platform7TestPaths.simple("special/happyPath.bpmn"));
    assertThat(testCases.isPlatform7()).isTrue();
  }

  @Test
  public void testIsNotPlatform7() {
    TestCases testCases = TestCases.of(Platform8TestPaths.simple("special/happyPath.bpmn"));
    assertThat(testCases.isPlatform7()).isFalse();
  }

  @Test
  public void testLinkEvent() {
    TestCases testCases = TestCases.of(Platform7TestPaths.advanced("linkEvent.bpmn"));
    assertThat(testCases.get()).hasSize(2);

    TestCase testCase = testCases.get().get(0);
    assertThat(testCase.getActivities()).hasSize(3);

    TestCaseActivity activity = testCase.getActivities().get(1);
    assertThat(activity.getId()).isEqualTo("linkThrowEventA");
    assertThat(activity.getType()).isEqualTo(TestCaseActivityType.LINK_THROW);

    activity = testCase.getActivities().get(2);
    assertThat(activity.getId()).isEqualTo("linkCatchEventA");
    assertThat(activity.getType()).isEqualTo(TestCaseActivityType.OTHER);
  }

  @Test
  public void testMultiInstanceScopeNested() {
    TestCases testCases = TestCases.of(Platform7TestPaths.advancedMultiInstance("scopeNested.bpmn"));
    assertThat(testCases.get()).hasSize(3);

    TestCase testCase = testCases.get().get(0);
    assertThat(testCase.getActivities()).hasSize(7);
    assertThat(testCase.getActivities().get(0).getNestingLevel()).isEqualTo(0);
    assertThat(testCase.getActivities().get(1).getNestingLevel()).isEqualTo(1);
    assertThat(testCase.getActivities().get(2).getNestingLevel()).isEqualTo(2);
    assertThat(testCase.getActivities().get(3).getNestingLevel()).isEqualTo(2);
    assertThat(testCase.getActivities().get(4).getNestingLevel()).isEqualTo(2);
    assertThat(testCase.getActivities().get(5).getNestingLevel()).isEqualTo(1);
    assertThat(testCase.getActivities().get(6).getNestingLevel()).isEqualTo(0);

    TestCaseActivityScope scope = testCase.getActivities().get(2).getParent();
    assertThat(scope.getActivities()).hasSize(3);
    assertThat(scope.getActivities().get(0).getId()).isEqualTo("nestedSubProcessStartEvent");
    assertThat(scope.getActivities().get(1).getId()).isEqualTo("userTask");
    assertThat(scope.getActivities().get(2).getId()).isEqualTo("nestedSubProcessEndEvent");
    assertThat(scope.getFlowNode()).isNotNull();
    assertThat(scope.getFlowNode(SubProcess.class)).isNotNull();
    assertThat(scope.getId()).isEqualTo("nestedSubProcess");
    assertThat(scope.getName()).isNull();
    assertThat(scope.getNestingLevel()).isEqualTo(2);
    assertThat(scope.getParent()).isNotNull();
    assertThat(scope.hasParent()).isTrue();
    assertThat(scope.isMultiInstance()).isTrue();
    assertThat(scope.isMultiInstanceParallel()).isFalse();
    assertThat(scope.isMultiInstanceSequential()).isTrue();

    scope = scope.getParent();
    assertThat(scope.getActivities()).hasSize(2);
    assertThat(scope.getActivities().get(0).getId()).isEqualTo("subProcessStartEvent");
    assertThat(scope.getActivities().get(1).getId()).isEqualTo("subProcessEndEvent");
    assertThat(scope.getFlowNode()).isNotNull();
    assertThat(scope.getFlowNode(SubProcess.class)).isNotNull();
    assertThat(scope.getId()).isEqualTo("subProcess");
    assertThat(scope.getName()).isNull();
    assertThat(scope.getNestingLevel()).isEqualTo(1);
    assertThat(scope.getParent()).isNull();
    assertThat(scope.hasParent()).isFalse();
    assertThat(scope.isMultiInstance()).isTrue();
    assertThat(scope.isMultiInstanceParallel()).isFalse();
    assertThat(scope.isMultiInstanceSequential()).isTrue();

    testCase = testCases.get().get(1);
    assertThat(testCase.getActivities()).hasSize(6);
    assertThat(testCase.getActivities().get(0).getNestingLevel()).isEqualTo(1);
    assertThat(testCase.getActivities().get(1).getNestingLevel()).isEqualTo(2);
    assertThat(testCase.getActivities().get(2).getNestingLevel()).isEqualTo(2);
    assertThat(testCase.getActivities().get(3).getNestingLevel()).isEqualTo(2);
    assertThat(testCase.getActivities().get(4).getNestingLevel()).isEqualTo(1);
    assertThat(testCase.getActivities().get(5).getNestingLevel()).isEqualTo(0);

    testCase = testCases.get().get(2);
    assertThat(testCase.getActivities()).hasSize(5);
    assertThat(testCase.getActivities().get(0).getNestingLevel()).isEqualTo(2);
    assertThat(testCase.getActivities().get(1).getNestingLevel()).isEqualTo(2);
    assertThat(testCase.getActivities().get(2).getNestingLevel()).isEqualTo(2);
    assertThat(testCase.getActivities().get(3).getNestingLevel()).isEqualTo(1);
    assertThat(testCase.getActivities().get(4).getNestingLevel()).isEqualTo(0);
  }

  @Test
  public void testNoTestCases() {
    TestCases testCases = TestCases.of(Platform7TestPaths.simple("special/noTestCases.bpmn"));
    assertThat(testCases.get()).hasSize(0);
    assertThat(testCases.isEmpty()).isTrue();
  }

  @Test
  public void testSubProcessNested() {
    TestCases testCases = TestCases.of(Platform7TestPaths.simple("simpleSubProcessNested.bpmn"));
    assertThat(testCases.get()).hasSize(1);

    TestCase testCase = testCases.get().get(0);
    assertThat(testCase.getActivities()).hasSize(7);

    // all activities beside startEvent and endEvent
    for (int i = 1; i < testCase.getActivities().size() - 1; i++) {
      TestCaseActivity activity = testCase.getActivities().get(i);

      assertThat(activity.hasParent()).isTrue();
      assertThat(activity.getParent().isMultiInstance()).isFalse();
    }
  }
}
