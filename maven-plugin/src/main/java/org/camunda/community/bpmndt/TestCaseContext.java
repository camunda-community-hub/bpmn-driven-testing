package org.camunda.community.bpmndt;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.camunda.community.bpmndt.model.TestCase;

public class TestCaseContext {

  private final List<TestCaseActivity> activities;
  private final Path bpmnFile;
  private final List<String> invalidFlowNodeIds;
  private final String processId;
  private final TestCase testCase;
  private final String testCaseName;

  private boolean duplicateName;

  public TestCaseContext(BpmnSupport bpmnSupport, TestCase testCase) {
    this.bpmnFile = bpmnSupport.getFile();
    this.processId = bpmnSupport.getProcessId();
    this.testCase = testCase;

    activities = new ArrayList<>(testCase.getPath().length());
    invalidFlowNodeIds = new LinkedList<>();

    // build test case name
    if (testCase.getName() != null) {
      testCaseName = BpmnSupport.toJavaLiteral(testCase.getName());
    } else if (testCase.getPath().length() >= 2) {
      List<String> flowNodeIds = testCase.getPath().getFlowNodeIds();

      String a = BpmnSupport.toJavaLiteral(flowNodeIds.get(0));
      String b = BpmnSupport.toJavaLiteral(flowNodeIds.get(flowNodeIds.size() - 1));

      testCaseName = String.format("%s__%s", a, b);
    } else {
      testCaseName = testCase.getPath().length() == 0 ? "empty" : "incomplete";
    }
  }

  public void addActivity(TestCaseActivity next) {
    if (!activities.isEmpty()) {
      TestCaseActivity prev = activities.get(activities.size() - 1);

      prev.setNext(next);
      next.setPrev(prev);
    }

    activities.add(next);
  }

  public void addInvalidFlowNodeId(String flowNodeId) {
    invalidFlowNodeIds.add(flowNodeId);
  }

  public List<TestCaseActivity> getActivities() {
    return activities;
  }

  public String getClassName() {
    return String.format("TC_%s__%s", BpmnSupport.toJavaLiteral(processId), getName());
  }

  public String getDescription() {
    return testCase.getDescription();
  }

  public TestCaseActivity getEndActivity() {
    if (activities.size() < 2) {
      return null;
    } else {
      return activities.get(activities.size() - 1);
    }
  }

  public List<String> getInvalidFlowNodeIds() {
    return invalidFlowNodeIds;
  }

  public String getName() {
    return testCaseName;
  }

  public String getProcessId() {
    return processId;
  }

  public String getResourceName(Path resourcePath) {
    return resourcePath.relativize(bpmnFile).toString().replace('\\', '/');
  }

  public TestCaseActivity getStartActivity() {
    if (activities.size() < 2) {
      return null;
    } else {
      return activities.get(0);
    }
  }

  /**
   * Determines whether the related test case has a duplicate name or not.
   * 
   * @return {@code true}, if the test case's name is not unique. Otherwise {@code false}.
   */
  public boolean hasDuplicateName() {
    return duplicateName;
  }

  public boolean isPathEmpty() {
    return testCase.getPath().length() == 0;
  }

  public boolean isPathIncomplete() {
    return testCase.getPath().length() == 1;
  }

  public boolean isPathInvalid() {
    return !invalidFlowNodeIds.isEmpty();
  }

  public boolean isValid() {
    return !isPathEmpty() && !isPathIncomplete() && !isPathInvalid();
  }

  public void setDuplicateName(boolean duplicateName) {
    this.duplicateName = duplicateName;
  }
}
