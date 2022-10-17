package org.camunda.community.bpmndt.cmd;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SubProcess;

/**
 * Collects all BPMN flow nodes under a given BPMN process element - this includes the flow nodes of
 * all (embedded) sub processes.
 */
public class CollectBpmnFlowNodes implements Function<Process, Collection<FlowNode>> {

  @Override
  public Collection<FlowNode> apply(Process process) {
    List<FlowNode> flowNodes = new LinkedList<>();

    collect(flowNodes, process.getFlowElements());
    collectSubProcesses(flowNodes, process.getChildElementsByType(SubProcess.class));

    return flowNodes;
  }

  protected void collect(List<FlowNode> flowNodes, Collection<FlowElement> elements) {
    elements.stream().filter(this::isFlowNode).map(FlowNode.class::cast).forEach(flowNodes::add);
  }

  protected void collectSubProcesses(List<FlowNode> flowNodes, Collection<SubProcess> subProcesses) {
    for (SubProcess subProcess : subProcesses) {
      collect(flowNodes, subProcess.getFlowElements());
      collectSubProcesses(flowNodes, subProcess.getChildElementsByType(SubProcess.class));
    }
  }

  private boolean isFlowNode(FlowElement element) {
    return FlowNode.class.isAssignableFrom(element.getClass());
  }
}
