package org.camunda.community.bpmndt.model;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.camunda.community.bpmndt.model.element.TestCaseElement;

import io.camunda.zeebe.model.bpmn.instance.Process;

class TestCaseImpl implements TestCase {

  TestCaseElement element;
  Process process;

  private final LinkedList<BpmnElement> elements = new LinkedList<>();
  private final List<String> invalidElementIds = new LinkedList<>();
  private final Map<String, BpmnElementScopeImpl> scopes = new LinkedHashMap<>();

  private BpmnElementImpl prev;

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof TestCase)) {
      return false;
    }

    var testCase = (TestCase) obj;
    return testCase.getProcessId().equals(getProcessId()) && testCase.getName().equals(getName());
  }

  @Override
  public String getDescription() {
    return element.getDescription();
  }

  @Override
  public BpmnElement getEndElement() {
    if (elements.isEmpty()) {
      throw new IllegalStateException("path is empty");
    }
    return elements.get(elements.size() - 1);
  }

  @Override
  public List<BpmnElement> getElements() {
    return elements;
  }

  @Override
  public List<String> getElementIds() {
    return element.getPath().getFlowNodeIds();
  }

  @Override
  public List<String> getInvalidElementIds() {
    return invalidElementIds;
  }

  @Override
  public String getName() {
    return element.getName();
  }

  @Override
  public Process getProcess() {
    return process;
  }

  @Override
  public String getProcessId() {
    return process.getId();
  }

  @Override
  public String getProcessName() {
    return process.getName();
  }

  @Override
  public BpmnElement getStartElement() {
    if (elements.isEmpty()) {
      throw new IllegalStateException("path is empty");
    }
    return elements.get(0);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getProcessId(), getName());
  }

  @Override
  public boolean hasEmptyPath() {
    return getElementIds().isEmpty();
  }

  @Override
  public boolean hasIncompletePath() {
    return getElementIds().size() == 1;
  }

  @Override
  public boolean hasInvalidPath() {
    return !invalidElementIds.isEmpty();
  }

  @Override
  public boolean isValid() {
    return !hasEmptyPath() && !hasIncompletePath() && !hasInvalidPath();
  }

  void addElement(BpmnElementImpl next) {
    if (prev != null) {
      prev.next = next;
      next.prev = prev;
    }

    elements.add(next);
    prev = next;
  }

  void addScope(BpmnElementScopeImpl scope) {
    scopes.put(scope.getId(), scope);
  }

  /**
   * Adds the ID of a BPMN element to the list of invalid element IDs, because it does not exist within the BPMN model instance. If at least one BPMN element ID
   * is added, the test case is considered to be invalid, since its path is invalid.
   *
   * @param elementId A BPMN element ID of the test case.
   * @see #getInvalidElementIds()
   * @see #hasInvalidPath()
   */
  void addInvalidElementId(String elementId) {
    invalidElementIds.add(elementId);
  }

  BpmnElementScopeImpl getScope(String scopeId) {
    return scopes.get(scopeId);
  }
}
