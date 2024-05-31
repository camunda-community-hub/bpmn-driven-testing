package org.camunda.community.bpmndt.api;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Abstract superclass for JUnit 5 based test cases.
 */
public abstract class AbstractJUnit5TestCase extends AbstractTestCase implements BeforeEachCallback {

  @Override
  public void beforeEach(ExtensionContext context) {
    testClass = context.getRequiredTestClass();
    testMethodName = context.getRequiredTestMethod().getName();

    beforeEach();
  }
}
