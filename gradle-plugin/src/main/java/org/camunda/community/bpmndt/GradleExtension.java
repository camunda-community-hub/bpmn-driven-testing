package org.camunda.community.bpmndt;

import org.gradle.api.provider.ListProperty;
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

  /**
   * Provides a list of process engine plugins to register at the process engine.
   * 
   * @return A list, containing process engine plugin class names.
   */
  ListProperty<String> getProcessEnginePlugins();

  /**
   * Provides the property, which determines if Spring based testing is enabled or not.
   * 
   * @return {@code true}, if Spring is enabled. Otherwise {@code false}.
   */
  Property<Boolean> getSpringEnabled();
}
