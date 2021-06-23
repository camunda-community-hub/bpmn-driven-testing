package org.camunda.bpm.extension.bpmndt;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.extension.bpmndt.impl.BpmnSupportImpl;
import org.camunda.bpm.extension.bpmndt.type.TestCase;

/**
 * BPMN model instance support, allows easier working with the flow nodes of a
 * {@link org.camunda.bpm.model.bpmn.instance.Process} and the test cases, defined as BPMN extension
 * element.
 */
public interface BpmnSupport {

  static Collection<Path> collectFiles(Path start) {
    return BpmnSupportImpl.collectFiles(start);
  }

  /**
   * Converts the given BPMN element ID to a literal, which can be used within Java source code. The
   * convertion retains letters and digits. All other characters are converted into underscores.
   * 
   * @param id The ID of a specific flow node or process definition.
   * 
   * @return The converted ID.
   */
  static String convert(String id) {
    return BpmnSupportImpl.convert(id);
  }

  static BpmnSupport of(Path bpmnFile) {
    return BpmnSupportImpl.of(bpmnFile);
  }

  BpmnNode get(String flowNodeId);

  String getProcessId();

  List<TestCase> getTestCases();

  boolean has(String flowNodeId);

  boolean has(List<String> flowNodeIds);

  boolean hasJob(List<String> flowNodeIds);

  boolean hasUserTask(List<String> flowNodeIds);
}
