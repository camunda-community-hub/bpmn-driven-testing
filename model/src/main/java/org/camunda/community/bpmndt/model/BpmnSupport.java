package org.camunda.community.bpmndt.model;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_BOUNDARY_EVENT;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_BUSINESS_RULE_TASK;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_CALL_ACTIVITY;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_END_EVENT;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_EVENT_BASED_GATEWAY;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_INTERMEDIATE_CATCH_EVENT;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_INTERMEDIATE_THROW_EVENT;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_PROCESS;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_RECEIVE_TASK;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_SEND_TASK;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_SERVICE_TASK;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_START_EVENT;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_USER_TASK;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.BusinessRuleTask;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.IntermediateThrowEvent;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.camunda.bpm.model.bpmn.instance.MultiInstanceLoopCharacteristics;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SendTask;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.SubProcess;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

/**
 * BPMN support, allows easier working with flow nodes of a
 * {@link org.camunda.bpm.model.bpmn.instance.Process}.
 */
public class BpmnSupport {

  private final Map<String, FlowNode> flowNodes;
  private final Process process;

  public BpmnSupport(Process process) {
    this.process = process;

    List<FlowNode> flowNodes = new LinkedList<>();

    // collect flow nodes of process and (embedded) sub processes
    collect(flowNodes, process.getFlowElements());
    collectSubProcesses(flowNodes, process.getChildElementsByType(SubProcess.class));

    this.flowNodes = flowNodes.stream().collect(Collectors.toMap(FlowNode::getId, Function.identity()));
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

  public FlowNode get(String flowNodeId) {
    return flowNodes.get(flowNodeId);
  }

  /**
   * Gets the multi instance loop characteristics from the flow node with the given ID.
   * 
   * @param flowNodeId A specific flow node ID.
   * 
   * @return The characteristics or {@code null}, if the flow node does not exist or does not have
   *         such loop characteristics.
   */
  public MultiInstanceLoopCharacteristics getMultiInstanceLoopCharacteristics(String flowNodeId) {
    if (!has(flowNodeId)) {
      return null;
    }

    FlowNode flowNode = flowNodes.get(flowNodeId);
    if (!(flowNode instanceof Activity)) {
      return null;
    }

    Activity activity = (Activity) flowNode;
    if (activity.getLoopCharacteristics() == null) {
      return null;
    }

    if (!(activity.getLoopCharacteristics() instanceof MultiInstanceLoopCharacteristics)) {
      return null;
    }

    return (MultiInstanceLoopCharacteristics) activity.getLoopCharacteristics();
  }

  /**
   * Returns the ID of the parent element, which can be the ID of the process, an embedded sub process
   * or a transaction.
   * 
   * @param flowNodeId A specific flow node ID.
   * 
   * @return The parent element ID or {@code null}, if the flow node with the given ID does not exist,
   *         has no parent element or the parent element is not of type {@link BaseElement}.
   */
  public String getParentElementId(String flowNodeId) {
    FlowNode flowNode = flowNodes.get(flowNodeId);
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

  public String getTopicName(String flowNodeId) {
    if (!isExternalTask(flowNodeId)) {
      return null;
    }

    if (is(flowNodeId, BPMN_ELEMENT_SERVICE_TASK)) {
      ServiceTask serviceTask = (ServiceTask) flowNodes.get(flowNodeId);
      return serviceTask.getCamundaTopic();
    } else if (is(flowNodeId, BPMN_ELEMENT_SEND_TASK)) {
      SendTask sendTask = (SendTask) flowNodes.get(flowNodeId);
      return sendTask.getCamundaTopic();
    } else if (is(flowNodeId, BPMN_ELEMENT_BUSINESS_RULE_TASK)) {
      BusinessRuleTask businessRuleTask = (BusinessRuleTask) flowNodes.get(flowNodeId);
      return businessRuleTask.getCamundaTopic();
    } else if (is(flowNodeId, BPMN_ELEMENT_INTERMEDIATE_THROW_EVENT)) {
      IntermediateThrowEvent event = (IntermediateThrowEvent) flowNodes.get(flowNodeId);

      BpmnEventSupport eventSupport = new BpmnEventSupport(event);
      MessageEventDefinition messageEventDefinition = eventSupport.isMessage() ? eventSupport.getMessageDefinition() : null;

      return messageEventDefinition != null ? messageEventDefinition.getCamundaTopic() : null;
    } else {
      return null;
    }
  }

  public boolean has(String flowNodeId) {
    return flowNodes.containsKey(flowNodeId);
  }

  private boolean is(String flowNodeId, String typeName) {
    FlowNode flowNode = flowNodes.get(flowNodeId);
    return flowNode != null && flowNode.getElementType().getTypeName().equals(typeName);
  }

  public boolean isBoundaryEvent(String flowNodeId) {
    return is(flowNodeId, BPMN_ELEMENT_BOUNDARY_EVENT);
  }

  public boolean isCallActivity(String flowNodeId) {
    return is(flowNodeId, BPMN_ELEMENT_CALL_ACTIVITY);
  }

  public boolean isEventBasedGateway(String flowNodeId) {
    return is(flowNodeId, BPMN_ELEMENT_EVENT_BASED_GATEWAY);
  }

  public boolean isExternalTask(String flowNodeId) {
    if (is(flowNodeId, BPMN_ELEMENT_SERVICE_TASK)) {
      ServiceTask serviceTask = (ServiceTask) flowNodes.get(flowNodeId);
      return "external".equals(serviceTask.getCamundaType());
    } else if (is(flowNodeId, BPMN_ELEMENT_SEND_TASK)) {
      SendTask sendTask = (SendTask) flowNodes.get(flowNodeId);
      return "external".equals(sendTask.getCamundaType());
    } else if (is(flowNodeId, BPMN_ELEMENT_BUSINESS_RULE_TASK)) {
      BusinessRuleTask businessRuleTask = (BusinessRuleTask) flowNodes.get(flowNodeId);
      return "external".equals(businessRuleTask.getCamundaType());
    } else if (is(flowNodeId, BPMN_ELEMENT_INTERMEDIATE_THROW_EVENT)) {
      IntermediateThrowEvent event = (IntermediateThrowEvent) flowNodes.get(flowNodeId);

      BpmnEventSupport eventSupport = new BpmnEventSupport(event);
      MessageEventDefinition messageEventDefinition = eventSupport.isMessage() ? eventSupport.getMessageDefinition() : null;

      return "external".equals(messageEventDefinition != null ? messageEventDefinition.getCamundaType() : null);
    } else {
      return false;
    }
  }

  private boolean isFlowNode(FlowElement element) {
    return FlowNode.class.isAssignableFrom(element.getClass());
  }

  public boolean isIntermediateCatchEvent(String flowNodeId) {
    return is(flowNodeId, BPMN_ELEMENT_INTERMEDIATE_CATCH_EVENT);
  }

  public boolean isIntermediateThrowEvent(String flowNodeId) {
    return is(flowNodeId, BPMN_ELEMENT_INTERMEDIATE_THROW_EVENT);
  }

  /**
   * Determines if the flow node with the given ID ends the process or not. This is the case if it
   * exists, if it is an end event and if the parent element is a process.
   * 
   * @param flowNodeId A specific flow node ID.
   * 
   * @return {@code true}, if the flow node ends the process. Otherwise {@code false}.
   */
  public boolean isProcessEnd(String flowNodeId) {
    if (!is(flowNodeId, BPMN_ELEMENT_END_EVENT)) {
      return false;
    }

    ModelElementInstance parent = flowNodes.get(flowNodeId).getParentElement();
    if (parent == null) {
      return false;
    }

    return parent.getElementType().getTypeName().equals(BPMN_ELEMENT_PROCESS);
  }

  public boolean isProcessStart(String flowNodeId) {
    if (!is(flowNodeId, BPMN_ELEMENT_START_EVENT)) {
      return false;
    }

    ModelElementInstance parent = flowNodes.get(flowNodeId).getParentElement();
    if (parent == null) {
      return false;
    }

    return parent.getElementType().getTypeName().equals(BPMN_ELEMENT_PROCESS);
  }

  public boolean isReceiveTask(String flowNodeId) {
    return is(flowNodeId, BPMN_ELEMENT_RECEIVE_TASK);
  }

  public boolean isUserTask(String flowNodeId) {
    return is(flowNodeId, BPMN_ELEMENT_USER_TASK);
  }
}
