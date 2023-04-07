package org.camunda.community.bpmndt.model;

import static com.google.common.truth.Truth.assertThat;

import java.util.List;

import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.community.bpmndt.model.element.PathElement;
import org.camunda.community.bpmndt.model.element.TestCaseElement;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.Test;

public class TestCasesImplTest {

  private TestCasesImpl testCases;

  @Test
  public void testGetTestCaseElements() {
    testCases = TestCasesImpl.of(TestPaths.simple("simple.bpmn"));

    Process process = (Process) testCases.modelInstance.getDefinitions().getUniqueChildElementByType(Process.class);

    List<TestCaseElement> elements = testCases.getTestCaseElements(process);
    assertThat(elements).hasSize(1);

    assertThat(elements.get(0).getName()).isNull();
    assertThat(elements.get(0).getDescription()).isNull();
    assertThat(elements.get(0).getPath()).isNotNull();
  }

  @Test
  public void testGetTestCaseElementsIncludingNameAndDescription() {
    testCases = TestCasesImpl.of(TestPaths.simple("special/happyPath.bpmn"));

    Process process = (Process) testCases.modelInstance.getDefinitions().getUniqueChildElementByType(Process.class);

    List<TestCaseElement> elements = testCases.getTestCaseElements(process);
    assertThat(elements).hasSize(1);

    assertThat(elements.get(0).getName()).isEqualTo("Happy Path");
    assertThat(elements.get(0).getDescription()).isEqualTo("The happy path");

    PathElement path = elements.get(0).getPath();
    assertThat(path).isNotNull();
    assertThat(path.getFlowNodeIds()).hasSize(2);
    assertThat(path.getFlowNodeIds().get(0)).isEqualTo("startEvent");
    assertThat(path.getFlowNodeIds().get(1)).isEqualTo("endEvent");
  }

  @Test
  public void testGetTestCaseElementsWhenNotDefined() {
    testCases = TestCasesImpl.of(TestPaths.simple("special/noTestCases.bpmn"));

    Process process = (Process) testCases.modelInstance.getDefinitions().getUniqueChildElementByType(Process.class);

    List<TestCaseElement> elements = testCases.getTestCaseElements(process);
    assertThat(elements).isNotNull();
    assertThat(elements).isEmpty();
  }
}
