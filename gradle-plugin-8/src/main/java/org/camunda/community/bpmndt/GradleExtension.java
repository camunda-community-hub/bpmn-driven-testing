package org.camunda.community.bpmndt;

import org.gradle.api.provider.Property;

/**
 * DSL extension, used to configure the Gradle plugin and it's tasks.
 */
public interface GradleExtension {

  /**
   * Returns the package name of the generated Java test case files.
   *
   * @return The package name for the generated test code.
   */
  Property<String> getPackageName();
}
