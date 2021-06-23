package org.camunda.bpm.extension.bpmndt.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SubProcess;

/**
 * Class that collects all BPMN flow nodes under a given BPMN process element - this includes the
 * flow nodes of all (embedded) sub processes.
 */
class BpmnNodeCollector {

  private final List<FlowNode> flowNodes;
  
  BpmnNodeCollector() {
    flowNodes = new LinkedList<>();
  }

  protected Collection<FlowNode> collect(Process process) {
    flowNodes.clear();

    collect(process.getFlowElements());
    collectSubProcesses(process.getChildElementsByType(SubProcess.class));

    return flowNodes;
  }

  protected void collect(Collection<FlowElement> elements) {
    elements.stream().filter(this::isFlowNode).map(FlowNode.class::cast).forEach(flowNodes::add);
  }

  protected void collectSubProcesses(Collection<SubProcess> subProcesses) {
    for (SubProcess subProcess : subProcesses) {
      collect(subProcess.getFlowElements());
      collectSubProcesses(subProcess.getChildElementsByType(SubProcess.class));
    }
  }

  private boolean isFlowNode(FlowElement element) {
    return FlowNode.class.isAssignableFrom(element.getClass());
  }
}
