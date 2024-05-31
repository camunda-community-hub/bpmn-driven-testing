package org.camunda.community.bpmndt.model;

import static com.google.common.truth.Truth.assertThat;

import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.Test;

import io.camunda.zeebe.model.bpmn.instance.Process;

class TestCasesImplTest {

  @Test
  void testGetTestCaseElements() {
    var testCases = TestCasesImpl.of(TestPaths.simple("simple.bpmn"));

    var process = (Process) testCases.modelInstance.getDefinitions().getUniqueChildElementByType(Process.class);

    var elements = testCases.getTestCaseElements(process);
    assertThat(elements).hasSize(1);

    assertThat(elements.get(0).getName()).isNull();
    assertThat(elements.get(0).getDescription()).isNull();
    assertThat(elements.get(0).getPath()).isNotNull();
  }

  @Test
  void testGetTestCaseElementsIncludingNameAndDescription() {
    var testCases = TestCasesImpl.of(TestPaths.simple("special/happyPath.bpmn"));

    var process = (Process) testCases.modelInstance.getDefinitions().getUniqueChildElementByType(Process.class);

    var elements = testCases.getTestCaseElements(process);
    assertThat(elements).hasSize(1);

    assertThat(elements.get(0).getName()).isEqualTo("Happy Path");
    assertThat(elements.get(0).getDescription()).isEqualTo("The happy path");

    var path = elements.get(0).getPath();
    assertThat(path).isNotNull();
    assertThat(path.getFlowNodeIds()).hasSize(2);
    assertThat(path.getFlowNodeIds().get(0)).isEqualTo("startEvent");
    assertThat(path.getFlowNodeIds().get(1)).isEqualTo("endEvent");
  }

  @Test
  void testGetTestCaseElementsWhenNotDefined() {
    var testCases = TestCasesImpl.of(TestPaths.simple("special/noTestCases.bpmn"));

    var process = (Process) testCases.modelInstance.getDefinitions().getUniqueChildElementByType(Process.class);

    var elements = testCases.getTestCaseElements(process);
    assertThat(elements).isNotNull();
    assertThat(elements).isEmpty();
  }
}
