package org.camunda.community.bpmndt;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.MultiInstanceLoopCharacteristics;

/**
 * Used to gather activities under their parent scope, if the scope is a multi instance.
 */
public class TestCaseActivityScope extends TestCaseActivity {

  private final List<TestCaseActivity> activities;

  public TestCaseActivityScope(FlowNode flowNode, List<TestCaseActivity> activities) {
    super(flowNode, null);
    this.activities = activities;
  }

  public TestCaseActivityScope(FlowNode flowNode, MultiInstanceLoopCharacteristics multiInstance) {
    super(flowNode, multiInstance);
    activities = new ArrayList<>();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof TestCaseActivityScope)) {
      return false;
    }

    TestCaseActivityScope scope = (TestCaseActivityScope) obj;
    return scope.getId().equals(getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }

  @Override
  public boolean isScope() {
    return true;
  }

  public void addActivity(TestCaseActivity next) {
    if (!activities.isEmpty()) {
      TestCaseActivity prev = activities.get(activities.size() - 1);

      prev.setNext(next);
      next.setPrev(prev);
    } else {
      next.setPrev(getPrev());
    }

    activities.add(next);
  }

  public List<TestCaseActivity> getActivities() {
    return activities;
  }
}
