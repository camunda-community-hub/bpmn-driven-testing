package org.camunda.bpm.extension.bpmndt.type;

import java.util.List;

import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;

/**
 * Custom BPMN extension element.
 */
public interface TestCases extends BpmnModelElementInstance {

  List<TestCase> getTestCases();
}
