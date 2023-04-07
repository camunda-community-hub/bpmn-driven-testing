package org.camunda.community.bpmndt.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.MultiInstanceLoopCharacteristics;
import org.camunda.bpm.model.bpmn.instance.SubProcess;

class TestCaseActivityScopeImpl implements TestCaseActivityScope {

  protected FlowNode flowNode;
  protected MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics;

  protected TestCaseActivityScope parent;

  private final List<TestCaseActivity> activities = new LinkedList<>();

  protected void addActivity(TestCaseActivityImpl next) {
    activities.add(next);
    next.parent = this;
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
  public List<TestCaseActivity> getActivities() {
    return activities;
  }

  @Override
  public FlowNode getFlowNode() {
    return flowNode;
  }

  @Override
  public <T extends SubProcess> T getFlowNode(Class<T> subProcessType) {
    return subProcessType.cast(flowNode);
  }

  @Override
  public String getId() {
    return flowNode.getId();
  }

  @Override
  public String getName() {
    return flowNode.getName();
  }

  @Override
  public int getNestingLevel() {
    return parent != null ? parent.getNestingLevel() + 1 : 1;
  }

  @Override
  public TestCaseActivityScope getParent() {
    return parent;
  }

  @Override
  public String getTypeName() {
    return flowNode.getElementType().getTypeName();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }

  @Override
  public boolean hasParent() {
    return parent != null;
  }

  @Override
  public boolean isMultiInstance() {
    return multiInstanceLoopCharacteristics != null;
  }

  @Override
  public boolean isMultiInstanceParallel() {
    return !isMultiInstanceSequential();
  }

  @Override
  public boolean isMultiInstanceSequential() {
    if (!isMultiInstance()) {
      throw new IllegalStateException("scope is not a multi instance");
    }
    return multiInstanceLoopCharacteristics.isSequential();
  }
}
