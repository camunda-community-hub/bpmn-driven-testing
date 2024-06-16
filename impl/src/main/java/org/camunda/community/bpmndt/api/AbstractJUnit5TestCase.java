package org.camunda.community.bpmndt.api;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Abstract superclass for JUnit 5 based test cases.
 */
public abstract class AbstractJUnit5TestCase<T extends AbstractTestCase<?>> extends AbstractTestCase<T> implements BeforeEachCallback, AfterEachCallback {

  @Override
  public void beforeEach(ExtensionContext context) {
    testClass = context.getRequiredTestClass();
    testMethod = context.getRequiredTestMethod();

    beforeEach();
  }

  @Override
  public void afterEach(ExtensionContext context) {
    afterEach();
  }
}
