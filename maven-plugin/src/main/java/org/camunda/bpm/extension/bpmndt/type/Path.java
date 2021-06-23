package org.camunda.bpm.extension.bpmndt.type;

import java.util.List;

import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;

public interface Path extends BpmnModelElementInstance {

  String getEnd();

  List<String> getFlowNodeIds();

  String getStart();
}
