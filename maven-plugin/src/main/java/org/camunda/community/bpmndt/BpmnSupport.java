package org.camunda.community.bpmndt;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
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
   * Converts the given BPMN element ID to a Java literal, which can be used when generating source
   * code. The convertion retains letters and digits. All other characters are converted into
   * underscores.
   * 
   * @param id The ID of a specific flow node or process.
   * 
   * @return A Java conform literal.
   */
  public static String toJavaLiteral(String id) {
    String trimmedId = id.trim();

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

  public boolean has(String flowNodeId) {
    return flowNodes.containsKey(flowNodeId);
  }

  protected boolean is(String flowNodeId, String typeName) {
    FlowNode flowNode = flowNodes.get(flowNodeId);
    return flowNode != null && flowNode.getElementType().getTypeName().equals(typeName);
  }

  public boolean isCallActivity(String flowNodeId) {
    return is(flowNodeId, BpmnModelConstants.BPMN_ELEMENT_CALL_ACTIVITY);
  }

  public boolean isExternalTask(String flowNodeId) {
    if (is(flowNodeId, BpmnModelConstants.BPMN_ELEMENT_SERVICE_TASK)) {
      ServiceTask serviceTask = (ServiceTask) flowNodes.get(flowNodeId);
      return "external".equals(serviceTask.getCamundaType());
    } else {
      return false;
    }
  }

  public boolean isIntermediateCatchEvent(String flowNodeId) {
    return is(flowNodeId, BpmnModelConstants.BPMN_ELEMENT_INTERMEDIATE_CATCH_EVENT);
  }

  public boolean isUserTask(String flowNodeId) {
    return is(flowNodeId, BpmnModelConstants.BPMN_ELEMENT_USER_TASK);
  }
}
