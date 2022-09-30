package org.camunda.community.bpmndt;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.camunda.community.bpmndt.model.TestCase;

public class TestCaseContext {

  private final BpmnSupport bpmnSupport;

  private final List<TestCaseActivity> activities;
  private final List<String> invalidFlowNodeIds;
  private final TestCase testCase;
  private final String testCaseName;

  private boolean duplicateName;

  public TestCaseContext(BpmnSupport bpmnSupport, TestCase testCase) {
    this.bpmnSupport = bpmnSupport;
    this.testCase = testCase;

    activities = new ArrayList<>(testCase.getPath().length());
    invalidFlowNodeIds = new LinkedList<>();

    // build test case name
    if (testCase.getName() != null) {
      testCaseName = BpmnSupport.toLiteral(testCase.getName());
    } else if (testCase.getPath().length() >= 2) {
      List<String> flowNodeIds = testCase.getPath().getFlowNodeIds();

      String a = BpmnSupport.toLiteral(flowNodeIds.get(0));
      String b = BpmnSupport.toLiteral(flowNodeIds.get(flowNodeIds.size() - 1));

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

  /**
   * Gets the name of the test case's class.
   * 
   * @return The class name.
   */
  public String getClassName() {
    return String.format("TC_%s", getName());
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

  /**
   * Gets the name of the test case's package.
   * 
   * @return The package name.
   */
  public String getPackageName() {
    return BpmnSupport.toJavaLiteral(getProcessId().toLowerCase(Locale.ENGLISH));
  }

  public String getProcessId() {
    return bpmnSupport.getProcessId();
  }

  public String getResourceName(Path resourcePath) {
    return resourcePath.relativize(bpmnSupport.getFile()).toString().replace('\\', '/');
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
