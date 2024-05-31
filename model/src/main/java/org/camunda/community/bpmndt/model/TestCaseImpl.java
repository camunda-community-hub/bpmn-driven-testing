package org.camunda.community.bpmndt.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.community.bpmndt.model.element.TestCaseElement;

class TestCaseImpl implements TestCase {

  protected TestCaseElement element;
  protected Process process;

  private final LinkedList<TestCaseActivity> activities = new LinkedList<>();
  private final List<String> invalidFlowNodeIds = new LinkedList<>();
  private final Map<String, TestCaseActivityScopeImpl> scopes = new LinkedHashMap<>();

  private TestCaseActivityImpl prev;

  protected void addActivity(TestCaseActivityImpl next) {
    if (prev != null) {
      prev.next = next;
      next.prev = prev;
    }

    activities.add(next);
    prev = next;
  }

  protected void addActivityScope(TestCaseActivityScopeImpl scope) {
    scopes.put(scope.getId(), scope);
  }

  /**
   * Adds the ID of a flow node to the list of invalid node IDs, because it does not exist within the BPMN model instance. If at least one flow node ID is
   * added, the test case is considered to be invalid, since its path is invalid.
   *
   * @param flowNodeId A flow node ID of the test case.
   * @see #getInvalidFlowNodeIds()
   * @see #hasInvalidPath()
   */
  protected void addInvalidFlowNodeId(String flowNodeId) {
    invalidFlowNodeIds.add(flowNodeId);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof TestCase)) {
      return false;
    }

    TestCase testCase = (TestCase) obj;
    return testCase.getProcessId().equals(getProcessId()) && testCase.getName().equals(getName());
  }

  @Override
  public List<TestCaseActivity> getActivities() {
    return activities;
  }

  protected TestCaseActivityScopeImpl getActivityScope(String scopeId) {
    return scopes.get(scopeId);
  }

  @Override
  public List<TestCaseActivityScope> getActivityScopes() {
    return new ArrayList<>(scopes.values());
  }

  @Override
  public String getDescription() {
    return element.getDescription();
  }

  @Override
  public TestCaseActivity getEndActivity() {
    if (activities.isEmpty()) {
      throw new IllegalStateException("path is empty");
    }
    return activities.getLast();
  }

  @Override
  public List<String> getFlowNodeIds() {
    return element.getPath().getFlowNodeIds();
  }

  @Override
  public List<String> getInvalidFlowNodeIds() {
    return invalidFlowNodeIds;
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
  public TestCaseActivity getStartActivity() {
    if (activities.isEmpty()) {
      throw new IllegalStateException("path is empty");
    }
    return activities.getFirst();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getProcessId(), getName());
  }

  @Override
  public boolean hasEmptyPath() {
    return getFlowNodeIds().isEmpty();
  }

  @Override
  public boolean hasIncompletePath() {
    return getFlowNodeIds().size() == 1;
  }

  @Override
  public boolean hasInvalidPath() {
    return !invalidFlowNodeIds.isEmpty();
  }

  @Override
  public boolean isValid() {
    return !hasEmptyPath() && !hasIncompletePath() && !hasInvalidPath();
  }
}
