package org.camunda.community.bpmndt;

import java.util.ArrayList;
import java.util.List;

import org.camunda.community.bpmndt.model.TestCase;

public class TestCaseContext {

  private final List<GeneratorStrategy> strategies = new ArrayList<>();

  private String className;
  private boolean duplicateName;
  private String name;
  private String packageName;
  private String resourceName;
  private TestCase testCase;

  public void addStrategy(GeneratorStrategy strategy) {
    strategies.add(strategy);
  }

  /**
   * Gets the name of the test case's class, starting with "TC_".
   *
   * @return The class name.
   */
  public String getClassName() {
    return className;
  }

  public String getName() {
    return name;
  }

  /**
   * Gets the name of the test case's package, which is the lowered process ID as Java literal.
   *
   * @return The package name.
   */
  public String getPackageName() {
    return packageName;
  }

  /**
   * Gets the name of the BPMN resources within the classpath.
   *
   * @return The BPMN classpath resource name.
   */
  public String getResourceName() {
    return resourceName;
  }

  /**
   * Returns the strategies of all relevant BPMN elements and scopes.
   *
   * @return A list of strategies.
   */
  public List<GeneratorStrategy> getStrategies() {
    return strategies;
  }

  /**
   * Returns the underlying test case.
   *
   * @return The test case.
   */
  public TestCase getTestCase() {
    return testCase;
  }

  /**
   * Determines whether the related test case has a duplicate name or not.
   *
   * @return {@code true}, if the test case's name is not unique. Otherwise {@code false}.
   */
  public boolean hasDuplicateName() {
    return duplicateName;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public void setDuplicateName(boolean duplicateName) {
    this.duplicateName = duplicateName;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

  public void setTestCase(TestCase testCase) {
    this.testCase = testCase;
  }
}
