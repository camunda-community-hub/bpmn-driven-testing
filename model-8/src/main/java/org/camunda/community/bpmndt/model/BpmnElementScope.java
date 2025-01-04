package org.camunda.community.bpmndt.model;

import java.util.List;

/**
 * Scope (embedded sub process) of one or more BPMN elements.
 */
public interface BpmnElementScope extends BpmnElement {

  /**
   * Gets the scope's BPMN elements.
   *
   * @return A list of elements that have the scope as parent.
   */
  List<BpmnElement> getElements();
}
