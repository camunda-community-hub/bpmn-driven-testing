package org.camunda.bpm.extension.bpmndt;

import java.util.List;

import org.camunda.bpm.extension.bpmndt.impl.GeneratorBuilderImpl;
import org.camunda.bpm.extension.bpmndt.type.TestCase;

import com.squareup.javapoet.JavaFile;

/**
 * Test case and framework classes generator.
 */
public interface Generator {

  /**
   * Creates a new builder.
   * 
   * @return The generator builder.
   */
  static Generator.Builder builder() {
    return new GeneratorBuilderImpl();
  }

  /**
   * Generates the Java file for the given test case.
   * 
   * @param testCase A specific test case from the extension element of a BPMN process.
   * 
   * @return The generated Java file, containing the JUnit test.
   */
  JavaFile generate(TestCase testCase);

  /**
   * Generates all framework classes like {@code AbstractTestCase}.
   * 
   * @return A list with framework classes.
   */
  List<JavaFile> generateFramework();

  /**
   * Generator builder.
   */
  public interface Builder {

    Builder bpmnResourceName(String bpmnResourceName);

    Builder bpmnSupport(BpmnSupport bpmnSupport);

    Generator build();

    Builder packageName(String packageName);

    Builder springEnabled(boolean springEnabled);
  }
}
