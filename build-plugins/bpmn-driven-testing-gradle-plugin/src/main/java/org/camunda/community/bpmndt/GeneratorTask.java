package org.camunda.community.bpmndt;

import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

public abstract class GeneratorTask extends DefaultTask {

	@Optional
	@Input
	abstract public Property<String> getJunit5enabled();

	@Optional
	@Input
	abstract public Property<String> getPackageName();

	@Optional
	@Input
	abstract public ListProperty<String> getProcessEnginePlugins();

	@Optional
	@Input
	abstract public Property<String> getSpringEnabled();

	@Optional
	@Input
	abstract public Property<String> getTestSourceDirectory();
	
	@TaskAction
	public void generateTests() {
		boolean junit5enabled = getJunit5enabled().isPresent() ? Boolean.valueOf(getJunit5enabled().get()) : false;
		String packageName = getPackageName().isPresent() ? getPackageName().get() : "generated";
		List<String> processEnginePlugins = getProcessEnginePlugins().isPresent() ? getProcessEnginePlugins().get() : null;
		boolean springEnabled = getSpringEnabled().isPresent() ? Boolean.valueOf(getSpringEnabled().get()) : false;
		String testSourceDirectory = getTestSourceDirectory().isPresent() ? getTestSourceDirectory().get() : "bpmndt";

		System.out.println("generate test code.");
	}
}
