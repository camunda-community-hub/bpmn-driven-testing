package org.camunda.bpm.extension.bpmndt.type;

import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;

/**
 * Node of a path, that is represented by the ID of the related flow node.
 */
public interface PathNode extends BpmnModelElementInstance {

  String getId();
}
