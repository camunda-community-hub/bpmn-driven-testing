package org.camunda.community.bpmndt.model;

import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_BOUNDARY_EVENT;
import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_BUSINESS_RULE_TASK;
import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_CALL_ACTIVITY;
import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_END_EVENT;
import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_EVENT_BASED_GATEWAY;
import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_INTERMEDIATE_CATCH_EVENT;
import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_INTERMEDIATE_THROW_EVENT;
import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_PROCESS;
import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_RECEIVE_TASK;
import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_SCRIPT_TASK;
import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_SEND_TASK;
import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_SERVICE_TASK;
import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_START_EVENT;
import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_USER_TASK;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.camunda.zeebe.model.bpmn.instance.Activity;
import io.camunda.zeebe.model.bpmn.instance.BaseElement;
import io.camunda.zeebe.model.bpmn.instance.FlowElement;
import io.camunda.zeebe.model.bpmn.instance.FlowNode;
import io.camunda.zeebe.model.bpmn.instance.MultiInstanceLoopCharacteristics;
import io.camunda.zeebe.model.bpmn.instance.Process;
import io.camunda.zeebe.model.bpmn.instance.SubProcess;

/**
 * BPMN support, allows easier working with flow nodes of a {@link Process}.
 */
public class BpmnSupport {

  private final Map<String, FlowNode> flowNodes;
  private final Process process;

  public BpmnSupport(Process process) {
    this.process = process;

    var flowNodes = new LinkedList<FlowNode>();

    // collect flow nodes of process and (embedded) sub processes
    collect(flowNodes, process.getFlowElements());
    collectSubProcesses(flowNodes, process.getChildElementsByType(SubProcess.class));

    this.flowNodes = flowNodes.stream().collect(Collectors.toMap(FlowNode::getId, Function.identity()));
  }

  public FlowNode get(String flowNodeId) {
    return flowNodes.get(flowNodeId);
  }

  /**
   * Gets the multi instance loop characteristics from the flow node with the given ID.
   *
   * @param flowNodeId A specific flow node ID.
   * @return The characteristics or {@code null}, if the flow node does not exist or does not have such loop characteristics.
   */
  public MultiInstanceLoopCharacteristics getMultiInstanceLoopCharacteristics(String flowNodeId) {
    if (!has(flowNodeId)) {
      return null;
    }

    var flowNode = flowNodes.get(flowNodeId);
    if (!(flowNode instanceof Activity)) {
      return null;
    }

    var activity = (Activity) flowNode;
    if (activity.getLoopCharacteristics() == null) {
      return null;
    }

    if (!(activity.getLoopCharacteristics() instanceof MultiInstanceLoopCharacteristics)) {
      return null;
    }

    return (MultiInstanceLoopCharacteristics) activity.getLoopCharacteristics();
  }

  /**
   * Returns the ID of the parent element, which can be the ID of the process, an embedded sub process or a transaction.
   *
   * @param flowNodeId A specific flow node ID.
   * @return The parent element ID or {@code null}, if the flow node with the given ID does not exist, has no parent element or the parent element is not of
   * type {@link BaseElement}.
   */
  public String getParentElementId(String flowNodeId) {
    var flowNode = flowNodes.get(flowNodeId);
    if (flowNode == null) {
      return null;
    }
    if (flowNode.getParentElement() == null) {
      // should not be the case
      return null;
    }

    try {
      return ((BaseElement) flowNode.getParentElement()).getId();
    } catch (ClassCastException e) {
      return null;
    }
  }

  public Process getProcess() {
    return process;
  }

  public boolean has(String flowNodeId) {
    return flowNodes.containsKey(flowNodeId);
  }

  public boolean isBoundaryEvent(String flowNodeId) {
    return is(flowNodeId, BPMN_ELEMENT_BOUNDARY_EVENT);
  }

  public boolean isBusinessRuleTask(String flowNodeId) {
    return is(flowNodeId, BPMN_ELEMENT_BUSINESS_RULE_TASK);
  }

  public boolean isCallActivity(String flowNodeId) {
    return is(flowNodeId, BPMN_ELEMENT_CALL_ACTIVITY);
  }

  public boolean isEndEvent(String flowNodeId) {
    return is(flowNodeId, BPMN_ELEMENT_END_EVENT);
  }

  public boolean isEventBasedGateway(String flowNodeId) {
    return is(flowNodeId, BPMN_ELEMENT_EVENT_BASED_GATEWAY);
  }

  public boolean isIntermediateCatchEvent(String flowNodeId) {
    return is(flowNodeId, BPMN_ELEMENT_INTERMEDIATE_CATCH_EVENT);
  }

  public boolean isIntermediateThrowEvent(String flowNodeId) {
    return is(flowNodeId, BPMN_ELEMENT_INTERMEDIATE_THROW_EVENT);
  }

  /**
   * Determines if the flow node with the given ID ends the process or not. This is the case if it exists, if it is an end event and if the parent element is a
   * process.
   *
   * @param flowNodeId A specific flow node ID.
   * @return {@code true}, if the flow node ends the process. Otherwise {@code false}.
   */
  public boolean isProcessEnd(String flowNodeId) {
    if (!is(flowNodeId, BPMN_ELEMENT_END_EVENT)) {
      return false;
    }

    var parent = flowNodes.get(flowNodeId).getParentElement();
    if (parent == null) {
      return false;
    }

    return parent.getElementType().getTypeName().equals(BPMN_ELEMENT_PROCESS);
  }

  public boolean isProcessStart(String flowNodeId) {
    if (!is(flowNodeId, BPMN_ELEMENT_START_EVENT)) {
      return false;
    }

    var parent = flowNodes.get(flowNodeId).getParentElement();
    if (parent == null) {
      return false;
    }

    return parent.getElementType().getTypeName().equals(BPMN_ELEMENT_PROCESS);
  }

  public boolean isReceiveTask(String flowNodeId) {
    return is(flowNodeId, BPMN_ELEMENT_RECEIVE_TASK);
  }

  public boolean isScriptTask(String flowNodeId) {
    return is(flowNodeId, BPMN_ELEMENT_SCRIPT_TASK);
  }

  public boolean isSendTask(String flowNodeId) {
    return is(flowNodeId, BPMN_ELEMENT_SEND_TASK);
  }

  public boolean isServiceTask(String flowNodeId) {
    return is(flowNodeId, BPMN_ELEMENT_SERVICE_TASK);
  }

  public boolean isUserTask(String flowNodeId) {
    return is(flowNodeId, BPMN_ELEMENT_USER_TASK);
  }

  private void collect(List<FlowNode> flowNodes, Collection<FlowElement> elements) {
    elements.stream().filter(this::isFlowNode).map(FlowNode.class::cast).forEach(flowNodes::add);
  }

  private void collectSubProcesses(List<FlowNode> flowNodes, Collection<SubProcess> subProcesses) {
    for (SubProcess subProcess : subProcesses) {
      collect(flowNodes, subProcess.getFlowElements());
      collectSubProcesses(flowNodes, subProcess.getChildElementsByType(SubProcess.class));
    }
  }

  private boolean is(String flowNodeId, String typeName) {
    var flowNode = flowNodes.get(flowNodeId);
    return flowNode != null && flowNode.getElementType().getTypeName().equals(typeName);
  }

  private boolean isFlowNode(FlowElement element) {
    return FlowNode.class.isAssignableFrom(element.getClass());
  }
}
