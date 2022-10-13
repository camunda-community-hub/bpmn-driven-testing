package org.camunda.community.bpmndt;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

abstract public class BpmndtExtension {

	abstract public Property<String> getJunit5enabled();

	abstract public Property<String> getPackageName();

	abstract public ListProperty<String> getProcessEnginePlugins();

	abstract public Property<String> getSpringEnabled();

	abstract public Property<String> getTestSourceDirectory();
}