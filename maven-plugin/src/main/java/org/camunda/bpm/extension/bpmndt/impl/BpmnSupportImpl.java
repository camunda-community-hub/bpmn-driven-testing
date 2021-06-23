package org.camunda.bpm.extension.bpmndt.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.extension.bpmndt.BpmnNode;
import org.camunda.bpm.extension.bpmndt.BpmnSupport;
import org.camunda.bpm.extension.bpmndt.type.TestCase;
import org.camunda.bpm.extension.bpmndt.type.TestCases;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Process;

public class BpmnSupportImpl implements BpmnSupport {

  static {
    // set custom BPMN instance to be able to use the TestCases extension element
    Bpmn.INSTANCE = new BpmnExtension();
  }

  public static Collection<Path> collectFiles(Path start) {
    return new BpmnFileCollector(start).collect();
  }

  public static String convert(String id) {
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

  public static BpmnSupportImpl of(Path bpmnFile) {
    try (FileInputStream fis = new FileInputStream(bpmnFile.toFile())) {
      return of(Bpmn.readModelFromStream(fis));
    } catch (FileNotFoundException e) {
      throw new RuntimeException("BPMN file could not be found", e);
    } catch (IOException e) {
      throw new RuntimeException("BPMN file could not be read", e);
    }
  }

  public static BpmnSupportImpl of(BpmnModelInstance modelInstance) {
    return new BpmnSupportImpl(modelInstance);
  }

  private final Process process;
  private final Map<String, FlowNode> flowNodes;

  BpmnSupportImpl(BpmnModelInstance modelInstance) {
    // find process
    process = (Process) modelInstance.getDefinitions().getUniqueChildElementByType(Process.class);
    if (process == null) {
      throw new RuntimeException("Model instance has no process definition");
    }

    flowNodes = new HashMap<>();

    // collect flow nodes of process and sub processes
    for (FlowNode flowNode : new BpmnNodeCollector().collect(process)) {
      flowNodes.put(flowNode.getId(), flowNode);
    }
  }

  @Override
  public BpmnNode get(String flowNodeId) {
    if (!has(flowNodeId)) {
      throw new IllegalArgumentException(String.format("Flow node '%s' does not exist", flowNodeId));
    }
    return new BpmnNodeImpl(flowNodes.get(flowNodeId));
  }

  @Override
  public String getProcessId() {
    return process.getId();
  }

  @Override
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

  @Override
  public boolean has(String flowNodeId) {
    return flowNodes.containsKey(flowNodeId);
  }

  @Override
  public boolean has(List<String> flowNodeIds) {
    return flowNodeIds.stream().filter(this::has).count() == flowNodeIds.size();
  }

  @Override
  public boolean hasJob(List<String> flowNodeIds) {
    return flowNodeIds.stream().map(this::get).filter(BpmnNode::isJob).count() != 0L;
  }

  @Override
  public boolean hasUserTask(List<String> flowNodeIds) {
    return flowNodeIds.stream().map(this::get).filter(BpmnNode::isUserTask).count() != 0L;
  }
}
