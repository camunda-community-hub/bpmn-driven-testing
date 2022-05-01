package org.camunda.community.bpmndt.api;

import java.util.ArrayList;
import java.util.List;

import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

/**
 * Abstract superclass for JUnit 4 based test cases.
 */
public abstract class AbstractJUnit4TestCase<T extends AbstractTestCase<?>> extends AbstractTestCase<T> implements TestRule {

  public Statement apply(final Statement base, final Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        List<Throwable> errors = new ArrayList<Throwable>();

        beforeEach(description, errors);
        try {
          base.evaluate();
        } catch (AssumptionViolatedException e) {
          errors.add(e);
        } catch (Throwable e) {
          errors.add(e);
        } finally {
          afterEach(description, errors);
        }

        MultipleFailureException.assertEmpty(errors);
      }
    };
  }

  private void beforeEach(Description description, List<Throwable> errors) {
    testClass = description.getTestClass();
    testMethodName = description.getMethodName();

    try {
      beforeEach();
    } catch (Throwable e) {
      errors.add(e);
    }
  }

  private void afterEach(Description description, List<Throwable> errors) {
    try {
      afterEach();
    } catch (Throwable e) {
      errors.add(e);
    }
  }
}
