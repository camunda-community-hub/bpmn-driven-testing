package org.camunda.bpm.extension.bpmndt.type;

import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;

/**
 * Test case definition.
 */
public interface TestCase extends BpmnModelElementInstance {

  String getDescription();

  String getName();

  Path getPath();
}
