package org.camunda.community.bpmndt;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_BOUNDARY_EVENT;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_CALL_ACTIVITY;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_CONDITIONAL_EVENT_DEFINITION;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_ERROR_EVENT_DEFINITION;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_ESCALATION_EVENT_DEFINITION;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_INTERMEDIATE_CATCH_EVENT;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_INTERMEDIATE_THROW_EVENT;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_MESSAGE_EVENT_DEFINITION;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_RECEIVE_TASK;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_SERVICE_TASK;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_SIGNAL_EVENT_DEFINITION;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_TIMER_EVENT_DEFINITION;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_USER_TASK;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.lang.model.SourceVersion;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.ConditionalEventDefinition;
import org.camunda.bpm.model.bpmn.instance.ErrorEventDefinition;
import org.camunda.bpm.model.bpmn.instance.EscalationEventDefinition;
import org.camunda.bpm.model.bpmn.instance.EventDefinition;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.IntermediateThrowEvent;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.camunda.bpm.model.bpmn.instance.MultiInstanceLoopCharacteristics;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.SignalEventDefinition;
import org.camunda.bpm.model.bpmn.instance.TimerEventDefinition;
import org.camunda.community.bpmndt.cmd.CollectBpmnFlowNodes;
import org.camunda.community.bpmndt.model.TestCase;
import org.camunda.community.bpmndt.model.TestCases;

/**
 * BPMN model instance support, allows easier working with the flow nodes of a
 * {@link org.camunda.bpm.model.bpmn.instance.Process} and the test cases, defined as {@code bpmndt}
 * extension elements.
 */
public class BpmnSupport {

  static {
    // set extended BPMN instance to be able to use the custom extension elements
    Bpmn.INSTANCE = new BpmnExtension();
  }

  public static BpmnSupport of(Path bpmnFile) {
    try (FileInputStream fis = new FileInputStream(bpmnFile.toFile())) {
      return new BpmnSupport(bpmnFile, Bpmn.readModelFromStream(fis));
    } catch (FileNotFoundException e) {
      throw new RuntimeException("BPMN file could not be found", e);
    } catch (IOException e) {
      throw new RuntimeException("BPMN file could not be read", e);
    }
  }

  /**
   * Converts the given BPMN element ID into a Java literal, which can be used when generating source
   * code. The convertion lowers all characters and retains letters as well as digits. All other
   * characters are converted into underscores. If the literal starts with a digit, an additional
   * underscore is prepended.
   * 
   * @param id The ID of a specific flow node or process.
   * 
   * @return A Java conform literal.
   */
  public static String toJavaLiteral(String id) {
    String literal = toLiteral(id).toLowerCase(Locale.ENGLISH);

    if (Character.isDigit(literal.charAt(0))) {
      return String.format("_%s", literal);
    } else if (SourceVersion.isKeyword(literal)) {
      return String.format("_%s", literal);
    } else {
      return literal;
    }
  }

  /**
   * Converts the given BPMN element ID into a literal, which can be used when generating source code.
   * The convertion retains letters and digits. All other characters are converted into underscores.
   * Moreover upper case is also retained.
   * 
   * @param id The ID of a specific flow node or process.
   * 
   * @return A conform literal.
   */
  public static String toLiteral(String id) {
    if (id == null) {
      throw new IllegalArgumentException("id is null");
    }

    String trimmedId = id.trim();
    if (trimmedId.isEmpty()) {
      throw new IllegalArgumentException("id is empty");
    }

    StringBuilder sb = new StringBuilder(trimmedId.length());
    for (int i = 0; i < trimmedId.length(); i++) {
      char c = trimmedId.charAt(i);

      if (Character.isLetterOrDigit(c)) {
        sb.append(c);
      } else {
        sb.append('_');
      }
    }

    return sb.toString();
  }

  private final Path file;
  private final Map<String, FlowNode> flowNodes;
  private final Process process;

  BpmnSupport(Path file, BpmnModelInstance modelInstance) {
    this.file = file;

    // find process
    process = (Process) modelInstance.getDefinitions().getUniqueChildElementByType(Process.class);
    if (process == null) {
      throw new RuntimeException("Model instance has no process definition");
    }

    flowNodes = new HashMap<>();

    // collect flow nodes of process and (embedded) sub processes
    for (FlowNode flowNode : new CollectBpmnFlowNodes().apply(process)) {
      flowNodes.put(flowNode.getId(), flowNode);
    }
  }

  public FlowNode get(String flowNodeId) {
    return flowNodes.get(flowNodeId);
  }

  public Path getFile() {
    return file;
  }

  public ConditionalEventDefinition getConditionalEventDefinition(Collection<EventDefinition> eventDefinitions) {
    Optional<EventDefinition> eventDefinition = eventDefinitions.stream().findFirst();
    if (is(eventDefinition, BPMN_ELEMENT_CONDITIONAL_EVENT_DEFINITION)) {
      return (ConditionalEventDefinition) eventDefinition.get();
    } else {
      return null;
    }
  }

  public ErrorEventDefinition getErrorEventDefinition(Collection<EventDefinition> eventDefinitions) {
    Optional<EventDefinition> eventDefinition = eventDefinitions.stream().findFirst();
    if (is(eventDefinition, BPMN_ELEMENT_ERROR_EVENT_DEFINITION)) {
      return (ErrorEventDefinition) eventDefinition.get();
    } else {
      return null;
    }
  }

  public EscalationEventDefinition getEscalationEventDefinition(Collection<EventDefinition> eventDefinitions) {
    Optional<EventDefinition> eventDefinition = eventDefinitions.stream().findFirst();
    if (is(eventDefinition, BPMN_ELEMENT_ESCALATION_EVENT_DEFINITION)) {
      return (EscalationEventDefinition) eventDefinition.get();
    } else {
      return null;
    }
  }

  public MessageEventDefinition getMessageEventDefinition(Collection<EventDefinition> eventDefinitions) {
    Optional<EventDefinition> eventDefinition = eventDefinitions.stream().findFirst();
    if (is(eventDefinition, BPMN_ELEMENT_MESSAGE_EVENT_DEFINITION)) {
      return (MessageEventDefinition) eventDefinition.get();
    } else {
      return null;
    }
  }

  public SignalEventDefinition getSignalEventDefinition(Collection<EventDefinition> eventDefinitions) {
    Optional<EventDefinition> eventDefinition = eventDefinitions.stream().findFirst();
    if (is(eventDefinition, BPMN_ELEMENT_SIGNAL_EVENT_DEFINITION)) {
      return (SignalEventDefinition) eventDefinition.get();
    } else {
      return null;
    }
  }

  public TimerEventDefinition getTimerEventDefinition(Collection<EventDefinition> eventDefinitions) {
    Optional<EventDefinition> eventDefinition = eventDefinitions.stream().findFirst();
    if (is(eventDefinition, BPMN_ELEMENT_TIMER_EVENT_DEFINITION)) {
      return (TimerEventDefinition) eventDefinition.get();
    } else {
      return null;
    }
  }

  /**
   * Gets the multi instance loop characteristics from the flow node with the given ID.
   * 
   * @param flowNodeId A specific flow node ID.
   * 
   * @return The characteristics or {@code null}, if the flow node does not exist or does not have
   *         such loop characteristics.
   */
  public MultiInstanceLoopCharacteristics getMultiInstance(String flowNodeId) {
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

  public String getProcessId() {
    return process.getId();
  }

  public List<TestCase> getTestCases() {
    if (process.getExtensionElements() == null) {
      return Collections.emptyList();
    }

    TestCases testCases = (TestCases) process.getExtensionElements().getUniqueChildElementByType(TestCases.class);
    if (testCases == null) {
      return Collections.emptyList();
    }

    return testCases.getTestCases();
  }

  public String getTopicName(String flowNodeId) {
    if (!isExternalTask(flowNodeId)) {
      return null;
    }

    if (is(flowNodeId, BPMN_ELEMENT_SERVICE_TASK)) {
      ServiceTask serviceTask = (ServiceTask) flowNodes.get(flowNodeId);
      return serviceTask.getCamundaTopic();
    } else if (is(flowNodeId, BPMN_ELEMENT_INTERMEDIATE_THROW_EVENT)) {
      IntermediateThrowEvent event = (IntermediateThrowEvent) flowNodes.get(flowNodeId);

      MessageEventDefinition messageEventDefinition = getMessageEventDefinition(event.getEventDefinitions());
      return messageEventDefinition != null ? messageEventDefinition.getCamundaTopic() : null;
    } else {
      return null;
    }
  }

  public boolean has(String flowNodeId) {
    return flowNodes.containsKey(flowNodeId);
  }

  protected boolean is(Optional<EventDefinition> eventDefinition, String typeName) {
    if (eventDefinition.isPresent()) {
      return eventDefinition.get().getElementType().getTypeName().equals(typeName);
    } else {
      return false;
    }
  }

  protected boolean is(String flowNodeId, String typeName) {
    FlowNode flowNode = flowNodes.get(flowNodeId);
    return flowNode != null && flowNode.getElementType().getTypeName().equals(typeName);
  }

  public boolean isBoundaryEvent(String flowNodeId) {
    return is(flowNodeId, BPMN_ELEMENT_BOUNDARY_EVENT);
  }

  public boolean isCallActivity(String flowNodeId) {
    return is(flowNodeId, BPMN_ELEMENT_CALL_ACTIVITY);
  }

  public boolean isExternalTask(String flowNodeId) {
    if (is(flowNodeId, BPMN_ELEMENT_SERVICE_TASK)) {
      ServiceTask serviceTask = (ServiceTask) flowNodes.get(flowNodeId);
      return "external".equals(serviceTask.getCamundaType());
    } else if (is(flowNodeId, BPMN_ELEMENT_INTERMEDIATE_THROW_EVENT)) {
      IntermediateThrowEvent event = (IntermediateThrowEvent) flowNodes.get(flowNodeId);

      MessageEventDefinition messageEventDefinition = getMessageEventDefinition(event.getEventDefinitions());
      return "external".equals(messageEventDefinition != null ? messageEventDefinition.getCamundaType() : null);
    } else {
      return false;
    }
  }

  public boolean isIntermediateCatchEvent(String flowNodeId) {
    return is(flowNodeId, BPMN_ELEMENT_INTERMEDIATE_CATCH_EVENT);
  }

  public boolean isReceiveTask(String flowNodeId) {
    return is(flowNodeId, BPMN_ELEMENT_RECEIVE_TASK);
  }

  public boolean isUserTask(String flowNodeId) {
    return is(flowNodeId, BPMN_ELEMENT_USER_TASK);
  }
}
