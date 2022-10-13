package org.camunda.community.bpmndt;

import java.nio.file.Paths;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public abstract class GeneratorTask extends DefaultTask {

	@Input
	abstract public Property<String> getJunit5enabled();

	@Input
	abstract public Property<String> getPackageName();

	@Input
	abstract public ListProperty<String> getProcessEnginePlugins();

	@Input
	abstract public Property<String> getSpringEnabled();

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
		GeneratorContext ctx = new GeneratorContext();
	    ctx.setBasePath(Paths.get(getProject().getPath()));
	    ctx.setJUnit5Enabled(junit5enabled);
	    //ctx.setMainResourcePath(Paths.get(project.getBuild().getResources().get(0).getDirectory()));
	    ctx.setMainResourcePath(Paths.get(getProject().getBuildDir().getPath()));
	    ctx.setPackageName(packageName);
	    ctx.setProcessEnginePluginNames(processEnginePlugins);
	    ctx.setSpringEnabled(springEnabled);
	    ctx.setTestSourcePath(Paths.get(testSourceDirectory));

	    // generate test code
	    try {
	      new Generator(getProject().getLogger()).generate(ctx);
	    } catch (RuntimeException e) {
	    	throw new GradleException(e.getMessage());
	    }
	}
}
