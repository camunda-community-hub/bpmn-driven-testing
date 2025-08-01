package org.camunda.community.bpmndt;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.artifacts.UnknownConfigurationException;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolutionResult;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom Gradle task, which generates the test code.
 */
public class GeneratorTask extends DefaultTask {

  /**
   * Name of the task, used for registration.
   */
  protected static final String NAME = "generateTestCases";

  private static final Logger LOGGER = LoggerFactory.getLogger(GeneratorTask.class);

  @Internal
  private GradleExtension extension;

  @Internal
  private Path mainResourcePath;

  @Internal
  private Path testSourcePath;

  @TaskAction
  public void generate() {
    String packageName = extension.getPackageName().getOrElse("generated");
    List<String> processEnginePlugins = extension.getProcessEnginePlugins().getOrElse(Collections.emptyList());
    boolean springEnabled = extension.getSpringEnabled().getOrElse(Boolean.FALSE);

    // check if external task client is used
    boolean externalTaskClientUsed = hasExternalTaskClientDependency(getProject());
    if (externalTaskClientUsed) {
      LOGGER.info("Found external task client");
      LOGGER.info("");
    }

    GeneratorContext ctx = new GeneratorContext();
    ctx.setBasePath(getProject().getProjectDir().toPath());
    ctx.setExternalTaskClientUsed(externalTaskClientUsed);
    ctx.setMainResourcePath(mainResourcePath);
    ctx.setPackageName(packageName);
    ctx.setProcessEnginePluginNames(processEnginePlugins);
    ctx.setSpringEnabled(springEnabled);
    ctx.setTestSourcePath(testSourcePath);

    new Generator().generate(ctx);
  }

  public GradleExtension getExtension() {
    return extension;
  }

  public Path getMainResourcePath() {
    return mainResourcePath;
  }

  public Path getTestSourcePath() {
    return testSourcePath;
  }

  public void setExtension(GradleExtension extension) {
    this.extension = extension;
  }

  public void setMainResourcePath(Path mainResourcePath) {
    this.mainResourcePath = mainResourcePath;
  }

  public void setTestSourcePath(Path testSourcePath) {
    this.testSourcePath = testSourcePath;
  }

  /**
   * Determines if the project has the Camunda external task client as compile or runtime dependency.
   *
   * @param project The project.
   * @return {@code true}, if the dependency exists. Otherwise {@code false}.
   */
  private boolean hasExternalTaskClientDependency(Project project) {
    return hasExternalTaskClientDependency(project, JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME) ||
        hasExternalTaskClientDependency(project, JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME);
  }

  private boolean hasExternalTaskClientDependency(Project project, String configurationName) {
    Configuration compileConfiguration;
    try {
      compileConfiguration = project.getConfigurations().getByName(configurationName);
    } catch (UnknownConfigurationException e) {
      return false;
    }

    ResolvableDependencies incoming = compileConfiguration.getIncoming();
    if (incoming == null) {
      return false;
    }

    ResolutionResult resolutionResult = incoming.getResolutionResult();
    if (resolutionResult == null) {
      return false;
    }

    Set<? extends DependencyResult> allDependencies = resolutionResult.getAllDependencies();
    if (allDependencies == null) {
      return false;
    }

    return allDependencies.stream().anyMatch(dependencyResult ->
        dependencyResult.toString().startsWith("org.camunda.bpm:camunda-external-task-client:")
    );
  }
}
