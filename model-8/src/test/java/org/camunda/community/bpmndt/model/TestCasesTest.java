package org.camunda.community.bpmndt.model;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Paths;

import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.Test;

import io.camunda.zeebe.model.bpmn.instance.SubProcess;

class TestCasesTest {

  @Test
  void shouldFailWhenFileIsDirectory() {
    var e = assertThrows(RuntimeException.class, () -> TestCasesImpl.of(Paths.get(".")));
    assertThat(e.getCause()).isNotNull();
    assertThat(e.getCause()).isInstanceOf(IOException.class);
  }

  @Test
  void shouldFailWhenFileNotExists() {
    var e = assertThrows(RuntimeException.class, () -> TestCasesImpl.of(Paths.get("./not-existing")));
    assertThat(e.getCause()).isNotNull();
    assertThat(e.getCause()).isInstanceOf(IOException.class);
  }

  @Test
  void testBusinessRuleTask() {
    var testCases = TestCases.of(TestPaths.simple("simpleBusinessRuleTask.bpmn"));
    assertThat(testCases.get()).hasSize(1);

    var testCase = testCases.get().get(0);
    assertThat(testCase.getElements()).hasSize(3);

    assertThat(testCase.getElements().get(1).getType()).isEqualTo(BpmnElementType.SERVICE_TASK);
  }

  @Test
  void testCollaboration() {
    var testCases = TestCases.of(TestPaths.advanced("collaboration.bpmn"));
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
  void testHappyPath() {
    var testCases = TestCases.of(TestPaths.simple("special/happyPath.bpmn"));
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
  void testIsEmpty() {
    var testCases = TestCases.of(TestPaths.simple("special/empty.bpmn"));
    assertThat(testCases.get()).hasSize(1);

    var testCase = testCases.get().get(0);
    assertThat(testCase.getInvalidElementIds()).isEmpty();
    assertThat(testCase.hasEmptyPath()).isTrue();
    assertThat(testCase.hasIncompletePath()).isFalse();
    assertThat(testCase.hasInvalidPath()).isFalse();
    assertThat(testCase.isValid()).isFalse();
  }

  @Test
  void testIsIncomplete() {
    var testCases = TestCases.of(TestPaths.simple("special/incomplete.bpmn"));
    assertThat(testCases.get()).hasSize(1);

    var testCase = testCases.get().get(0);
    assertThat(testCase.getInvalidElementIds()).isEmpty();
    assertThat(testCase.hasEmptyPath()).isFalse();
    assertThat(testCase.hasIncompletePath()).isTrue();
    assertThat(testCase.hasInvalidPath()).isFalse();
    assertThat(testCase.isValid()).isFalse();
  }

  @Test
  void testIsInvalid() {
    var testCases = TestCases.of(TestPaths.simple("special/invalid.bpmn"));
    assertThat(testCases.get()).hasSize(1);

    var testCase = testCases.get().get(0);
    assertThat(testCase.getInvalidElementIds()).containsExactly("a", "b").inOrder();
    assertThat(testCase.hasEmptyPath()).isFalse();
    assertThat(testCase.hasIncompletePath()).isFalse();
    assertThat(testCase.hasInvalidPath()).isTrue();
    assertThat(testCase.isValid()).isFalse();
  }

  @Test
  void testIsPlatform8() {
    var testCases = TestCases.of(TestPaths.simple("special/happyPath.bpmn"));
    assertThat(testCases.isPlatform8()).isTrue();
  }

  @Test
  void testIsNotPlatform8() {
    var testCases = TestCases.of(Paths.get("../integration-tests/simple/src/main/resources/special/happyPath.bpmn"));
    assertThat(testCases.isPlatform8()).isFalse();
  }

  @Test
  void testLinkEvent() {
    var testCases = TestCases.of(TestPaths.advanced("linkEvent.bpmn"));
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
  void testMessageEndEvent() {
    var testCases = TestCases.of(TestPaths.simple("simpleMessageEndEvent.bpmn"));
    assertThat(testCases.get()).hasSize(1);

    var testCase = testCases.get().get(0);
    assertThat(testCase.getElements()).hasSize(2);

    assertThat(testCase.getElements().get(1).getType()).isEqualTo(BpmnElementType.SERVICE_TASK);
  }

  @Test
  void testMessageThrowEvent() {
    var testCases = TestCases.of(TestPaths.simple("simpleMessageThrowEvent.bpmn"));
    assertThat(testCases.get()).hasSize(1);

    var testCase = testCases.get().get(0);
    assertThat(testCase.getElements()).hasSize(3);

    assertThat(testCase.getElements().get(1).getType()).isEqualTo(BpmnElementType.SERVICE_TASK);
  }

  @Test
  void testMultiInstanceScopeNested() {
    var testCases = TestCases.of(TestPaths.advancedMultiInstance("scopeNested.bpmn"));
    assertThat(testCases.get()).hasSize(1);

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
    assertThat(scope.isMultiInstanceSequential()).isTrue();
  }

  @Test
  void testNoTestCases() {
    var testCases = TestCases.of(TestPaths.simple("special/noTestCases.bpmn"));
    assertThat(testCases.get()).hasSize(0);
    assertThat(testCases.isEmpty()).isTrue();
  }

  @Test
  void testOutboundConnector() {
    var testCases = TestCases.of(TestPaths.simple("simpleOutboundConnector.bpmn"));
    assertThat(testCases.get()).hasSize(1);

    var testCase = testCases.get().get(0);
    assertThat(testCase.getElements()).hasSize(3);

    assertThat(testCase.getElements().get(1).getType()).isEqualTo(BpmnElementType.OUTBOUND_CONNECTOR);
  }

  @Test
  void testScriptTask() {
    var testCases = TestCases.of(TestPaths.simple("simpleScriptTask.bpmn"));
    assertThat(testCases.get()).hasSize(1);

    var testCase = testCases.get().get(0);
    assertThat(testCase.getElements()).hasSize(3);

    assertThat(testCase.getElements().get(1).getType()).isEqualTo(BpmnElementType.SERVICE_TASK);
  }

  @Test
  void testSendTask() {
    var testCases = TestCases.of(TestPaths.simple("simpleSendTask.bpmn"));
    assertThat(testCases.get()).hasSize(1);

    var testCase = testCases.get().get(0);
    assertThat(testCase.getElements()).hasSize(3);

    assertThat(testCase.getElements().get(1).getType()).isEqualTo(BpmnElementType.SERVICE_TASK);
  }

  @Test
  void testServiceTask() {
    var testCases = TestCases.of(TestPaths.simple("simpleServiceTask.bpmn"));
    assertThat(testCases.get()).hasSize(1);

    var testCase = testCases.get().get(0);
    assertThat(testCase.getElements()).hasSize(3);

    assertThat(testCase.getElements().get(1).getType()).isEqualTo(BpmnElementType.SERVICE_TASK);
  }

  @Test
  void testSubProcessNested() {
    var testCases = TestCases.of(TestPaths.simple("simpleSubProcessNested.bpmn"));
    assertThat(testCases.get()).hasSize(2);

    var testCase = testCases.get().get(0);
    assertThat(testCase.getElements()).hasSize(7);

    // all activities beside startEvent and endEvent
    for (int i = 1; i < testCase.getElements().size() - 1; i++) {
      var element = testCase.getElements().get(i);

      assertThat(element.hasParent()).isTrue();
      assertThat(element.getParent().isMultiInstance()).isFalse();
    }

    testCase = testCases.get().get(1);
    assertThat(testCase.getElements()).hasSize(4);

    assertThat(testCase.getElements().get(0).hasParent()).isTrue();
    assertThat(testCase.getElements().get(0).getParent().getId()).isEqualTo("nestedSubProcess");
    assertThat(testCase.getElements().get(1).hasParent()).isTrue();
    assertThat(testCase.getElements().get(1).getParent().getId()).isEqualTo("nestedSubProcess");
    assertThat(testCase.getElements().get(2).hasParent()).isTrue();
    assertThat(testCase.getElements().get(2).getParent().getId()).isEqualTo("subProcess");
    assertThat(testCase.getElements().get(3).hasParent()).isFalse();
  }
}
