package org.camunda.community.bpmndt.model.platform8;

import static com.google.common.truth.Truth.assertThat;

import java.util.List;

import org.camunda.community.bpmndt.model.platform8.element.PathElement;
import org.camunda.community.bpmndt.model.platform8.element.TestCaseElement;
import org.camunda.community.bpmndt.test.Platform8TestPaths;
import org.junit.jupiter.api.Test;

import io.camunda.zeebe.model.bpmn.instance.Process;

public class TestCasesImplTest {

  @Test
  public void testGetTestCaseElements() {
    TestCasesImpl testCases = TestCasesImpl.of(Platform8TestPaths.simple("simple.bpmn"));

    Process process = (Process) testCases.modelInstance.getDefinitions().getUniqueChildElementByType(Process.class);

    List<TestCaseElement> elements = testCases.getTestCaseElements(process);
    assertThat(elements).hasSize(1);

    assertThat(elements.get(0).getName()).isNull();
    assertThat(elements.get(0).getDescription()).isNull();
    assertThat(elements.get(0).getPath()).isNotNull();
  }

  @Test
  public void testGetTestCaseElementsIncludingNameAndDescription() {
    TestCasesImpl testCases = TestCasesImpl.of(Platform8TestPaths.simple("special/happyPath.bpmn"));

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
    TestCasesImpl testCases = TestCasesImpl.of(Platform8TestPaths.simple("special/noTestCases.bpmn"));

    Process process = (Process) testCases.modelInstance.getDefinitions().getUniqueChildElementByType(Process.class);

    List<TestCaseElement> elements = testCases.getTestCaseElements(process);
    assertThat(elements).isNotNull();
    assertThat(elements).isEmpty();
  }
}
