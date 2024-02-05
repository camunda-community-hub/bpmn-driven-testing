package org.camunda.community.bpmndt.model.platform8;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Paths;

import org.camunda.community.bpmndt.test.Platform7TestPaths;
import org.camunda.community.bpmndt.test.Platform8TestPaths;
import org.junit.jupiter.api.Test;

import io.camunda.zeebe.model.bpmn.instance.SubProcess;

public class TestCasesTest {

  @Test
  public void shouldFailWhenFileIsDirectory() {
    var e = assertThrows(RuntimeException.class, () -> TestCasesImpl.of(Paths.get(".")));
    assertThat(e.getCause()).isNotNull();
    assertThat(e.getCause()).isInstanceOf(IOException.class);
  }

  @Test
  public void shouldFailWhenFileNotExists() {
    var e = assertThrows(RuntimeException.class, () -> TestCasesImpl.of(Paths.get("./not-existing")));
    assertThat(e.getCause()).isNotNull();
    assertThat(e.getCause()).isInstanceOf(IOException.class);
  }

  @Test
  public void testCollaboration() {
    var testCases = TestCases.of(Platform8TestPaths.advanced("collaboration.bpmn"));
    assertThat(testCases.get()).hasSize(4);
    assertThat(testCases.get("processA")).hasSize(1);
    assertThat(testCases.get("processB")).hasSize(0);
    assertThat(testCases.get("processC")).hasSize(3);
    assertThat(testCases.getProcessIds()).containsExactly("processA", "processB", "processC").inOrder();

    var testCase = testCases.get("processA").get(0);
    assertThat(testCase.getElements()).hasSize(4);
    assertThat(testCase.getEndElement().getId()).isEqualTo("endEventA");
    assertThat(testCase.getStartElement().getId()).isEqualTo("startEventA");

    testCase = testCases.get("processC").get(1);
    assertThat(testCase.getElements()).hasSize(3);
    assertThat(testCase.getEndElement().getId()).isEqualTo("subProcessEndC");
    assertThat(testCase.getStartElement().getId()).isEqualTo("startEventC");
  }

  @Test
  public void testHappyPath() {
    var testCases = TestCases.of(Platform8TestPaths.simple("special/happyPath.bpmn"));
    assertThat(testCases.get()).hasSize(1);
    assertThat(testCases.get("happyPath")).hasSize(1);
    assertThat(testCases.getModelInstance()).isNotNull();
    assertThat(testCases.getProcessIds()).containsExactly("happyPath");
    assertThat(testCases.isEmpty()).isFalse();

    var testCase = testCases.get().get(0);
    assertThat(testCase.getElements()).hasSize(2);
    assertThat(testCase.getDescription()).isEqualTo("The happy path");
    assertThat(testCase.getEndElement().getId()).isEqualTo("endEvent");
    assertThat(testCase.getElementIds()).containsExactly("startEvent", "endEvent").inOrder();
    assertThat(testCase.getInvalidElementIds()).isEmpty();
    assertThat(testCase.getName()).isEqualTo("Happy Path");
    assertThat(testCase.getProcess()).isNotNull();
    assertThat(testCase.getProcessId()).isEqualTo("happyPath");
    assertThat(testCase.getProcessName()).isEqualTo("Happy Path Process");
    assertThat(testCase.getStartElement().getId()).isEqualTo("startEvent");
    assertThat(testCase.hasEmptyPath()).isFalse();
    assertThat(testCase.hasIncompletePath()).isFalse();
    assertThat(testCase.hasInvalidPath()).isFalse();
    assertThat(testCase.isValid()).isTrue();
  }

  @Test
  public void testIsEmpty() {
    var testCases = TestCases.of(Platform8TestPaths.simple("special/empty.bpmn"));
    assertThat(testCases.get()).hasSize(1);

    var testCase = testCases.get().get(0);
    assertThat(testCase.getInvalidElementIds()).isEmpty();
    assertThat(testCase.hasEmptyPath()).isTrue();
    assertThat(testCase.hasIncompletePath()).isFalse();
    assertThat(testCase.hasInvalidPath()).isFalse();
    assertThat(testCase.isValid()).isFalse();
  }

  @Test
  public void testIsIncomplete() {
    var testCases = TestCases.of(Platform8TestPaths.simple("special/incomplete.bpmn"));
    assertThat(testCases.get()).hasSize(1);

    var testCase = testCases.get().get(0);
    assertThat(testCase.getInvalidElementIds()).isEmpty();
    assertThat(testCase.hasEmptyPath()).isFalse();
    assertThat(testCase.hasIncompletePath()).isTrue();
    assertThat(testCase.hasInvalidPath()).isFalse();
    assertThat(testCase.isValid()).isFalse();
  }

  @Test
  public void testIsInvalid() {
    var testCases = TestCases.of(Platform8TestPaths.simple("special/invalid.bpmn"));
    assertThat(testCases.get()).hasSize(1);

    var testCase = testCases.get().get(0);
    assertThat(testCase.getInvalidElementIds()).containsExactly("a", "b").inOrder();
    assertThat(testCase.hasEmptyPath()).isFalse();
    assertThat(testCase.hasIncompletePath()).isFalse();
    assertThat(testCase.hasInvalidPath()).isTrue();
    assertThat(testCase.isValid()).isFalse();
  }

  @Test
  public void testIsPlatform8() {
    var testCases = TestCases.of(Platform8TestPaths.simple("special/happyPath.bpmn"));
    assertThat(testCases.isPlatform8()).isTrue();
  }

  @Test
  public void testIsNotPlatform8() {
    var testCases = TestCases.of(Platform7TestPaths.simple("special/happyPath.bpmn"));
    assertThat(testCases.isPlatform8()).isFalse();
  }

  @Test
  public void testLinkEvent() {
    var testCases = TestCases.of(Platform8TestPaths.advanced("linkEvent.bpmn"));
    assertThat(testCases.get()).hasSize(2);

    var testCase = testCases.get().get(0);
    assertThat(testCase.getElements()).hasSize(3);

    var element = testCase.getElements().get(1);
    assertThat(element.getId()).isEqualTo("linkThrowEventA");
    assertThat(element.getType()).isEqualTo(BpmnElementType.LINK_THROW);

    element = testCase.getElements().get(2);
    assertThat(element.getId()).isEqualTo("linkCatchEventA");
    assertThat(element.getType()).isEqualTo(BpmnElementType.OTHER);
  }

  @Test
  public void testMultiInstanceScopeNested() {
    var testCases = TestCases.of(Platform8TestPaths.advancedMultiInstance("scopeNested.bpmn"));
    assertThat(testCases.get()).hasSize(3);

    var testCase = testCases.get().get(0);
    assertThat(testCase.getElements()).hasSize(7);
    assertThat(testCase.getElements().get(0).getNestingLevel()).isEqualTo(0);
    assertThat(testCase.getElements().get(1).getNestingLevel()).isEqualTo(1);
    assertThat(testCase.getElements().get(2).getNestingLevel()).isEqualTo(2);
    assertThat(testCase.getElements().get(3).getNestingLevel()).isEqualTo(2);
    assertThat(testCase.getElements().get(4).getNestingLevel()).isEqualTo(2);
    assertThat(testCase.getElements().get(5).getNestingLevel()).isEqualTo(1);
    assertThat(testCase.getElements().get(6).getNestingLevel()).isEqualTo(0);

    var scope = testCase.getElements().get(2).getParent();
    assertThat(scope.getElements()).hasSize(3);
    assertThat(scope.getElements().get(0).getId()).isEqualTo("nestedSubProcessStartEvent");
    assertThat(scope.getElements().get(1).getId()).isEqualTo("userTask");
    assertThat(scope.getElements().get(2).getId()).isEqualTo("nestedSubProcessEndEvent");
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
    assertThat(scope.getElements()).hasSize(2);
    assertThat(scope.getElements().get(0).getId()).isEqualTo("subProcessStartEvent");
    assertThat(scope.getElements().get(1).getId()).isEqualTo("subProcessEndEvent");
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
    assertThat(testCase.getElements()).hasSize(6);
    assertThat(testCase.getElements().get(0).getNestingLevel()).isEqualTo(1);
    assertThat(testCase.getElements().get(1).getNestingLevel()).isEqualTo(2);
    assertThat(testCase.getElements().get(2).getNestingLevel()).isEqualTo(2);
    assertThat(testCase.getElements().get(3).getNestingLevel()).isEqualTo(2);
    assertThat(testCase.getElements().get(4).getNestingLevel()).isEqualTo(1);
    assertThat(testCase.getElements().get(5).getNestingLevel()).isEqualTo(0);

    testCase = testCases.get().get(2);
    assertThat(testCase.getElements()).hasSize(5);
    assertThat(testCase.getElements().get(0).getNestingLevel()).isEqualTo(2);
    assertThat(testCase.getElements().get(1).getNestingLevel()).isEqualTo(2);
    assertThat(testCase.getElements().get(2).getNestingLevel()).isEqualTo(2);
    assertThat(testCase.getElements().get(3).getNestingLevel()).isEqualTo(1);
    assertThat(testCase.getElements().get(4).getNestingLevel()).isEqualTo(0);
  }

  @Test
  public void testNoTestCases() {
    var testCases = TestCases.of(Platform8TestPaths.simple("special/noTestCases.bpmn"));
    assertThat(testCases.get()).hasSize(0);
    assertThat(testCases.isEmpty()).isTrue();
  }

  @Test
  public void testSubProcessNested() {
    var testCases = TestCases.of(Platform8TestPaths.simple("simpleSubProcessNested.bpmn"));
    assertThat(testCases.get()).hasSize(1);

    var testCase = testCases.get().get(0);
    assertThat(testCase.getElements()).hasSize(7);

    // all activities beside startEvent and endEvent
    for (int i = 1; i < testCase.getElements().size() - 1; i++) {
      var element = testCase.getElements().get(i);

      assertThat(element.hasParent()).isTrue();
      assertThat(element.getParent().isMultiInstance()).isFalse();
    }
  }
}
