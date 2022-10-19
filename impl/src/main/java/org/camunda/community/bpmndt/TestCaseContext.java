package org.camunda.community.bpmndt;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

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

  /**
   * Adds the ID of a flow node to the invalid node IDs, because it does not exists within the BPMN
   * model instance. If at least one flow node ID is added, the test case is considered to be invalid,
   * since its path is invalid.
   * 
   * @param flowNodeId A flow node ID of the test case.
   * 
   * @see #getInvalidFlowNodeIds()
   * @see #isPathInvalid()
   */
  public void addInvalidFlowNodeId(String flowNodeId) {
    invalidFlowNodeIds.add(flowNodeId);
  }

  private void filter(List<TestCaseActivity> activities, Set<TestCaseActivity> filtered, Predicate<TestCaseActivity> filter) {
    for (TestCaseActivity activity : activities) {
      if (filter.test(activity)) {
        filtered.add(activity);
      }

      if (activity.isScope()) {
        TestCaseActivityScope scope = (TestCaseActivityScope) activity;
        filter(scope.getActivities(), filtered, filter);
      }
    }
  }

  public List<TestCaseActivity> getActivities() {
    return activities;
  }

  /**
   * Gets all activities (including activities from scopes) that satisfy the given filter.
   * 
   * @param filter An activity filter.
   * 
   * @return A set of filtered activities.
   */
  public Set<TestCaseActivity> getActivities(Predicate<TestCaseActivity> filter) {
    Set<TestCaseActivity> filtered = new HashSet<>();
    filter(activities, filtered, filter);
    return filtered;
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
    if (activities.isEmpty()) {
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
    if (activities.isEmpty()) {
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
