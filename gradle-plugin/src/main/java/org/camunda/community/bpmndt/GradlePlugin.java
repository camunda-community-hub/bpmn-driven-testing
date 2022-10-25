package org.camunda.community.bpmndt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom Gradle plugin, which adds a task to generate the test code and a new test source set.
 */
public class GradlePlugin implements Plugin<Project> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GradlePlugin.class);

  /**
   * Name of the test source set, which must contain "test" to indicate that the source set contains
   * test code. Otherwise Eclipse will not recognize it!
   */
  private static final String SOURCE_SET_NAME = "bpmndtTestCases";

  @Override
  public void apply(Project project) {
    // get properties via extension
    GradleExtension extension = project.getExtensions().create(Constants.EXTENSION_NAME, GradleExtension.class);

    // add test source directory
    Path testSourcePath = project.getBuildDir().toPath().resolve(Constants.EXTENSION_NAME);
    try {
      Files.createDirectories(testSourcePath);
    } catch (IOException e) {
      throw new RuntimeException(String.format("Test source directory '%s' could not be created", testSourcePath), e);
    }

    LOGGER.info(String.format("Adding test source directory: %s", testSourcePath));
    JavaPluginExtension javaExtension = project.getExtensions().getByType(JavaPluginExtension.class);

    SourceSet testSourceSet = javaExtension.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME);

    SourceSet additionalTestSourceSet = javaExtension.getSourceSets().create(SOURCE_SET_NAME);
    additionalTestSourceSet.getJava().setSrcDirs(Collections.singleton(testSourcePath.toFile()));
    additionalTestSourceSet.setAnnotationProcessorPath(testSourceSet.getAnnotationProcessorPath());
    additionalTestSourceSet.setCompileClasspath(testSourceSet.getCompileClasspath());
    additionalTestSourceSet.setRuntimeClasspath(testSourceSet.getRuntimeClasspath());

    testSourceSet.getJava().source(additionalTestSourceSet.getJava());

    // register generator task
    GeneratorTask generatorTask = project.getTasks().register(GeneratorTask.NAME, GeneratorTask.class, task -> {
      SourceSet mainSourceSet = javaExtension.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);

      task.setExtension(extension);
      task.setMainResourcePath(Paths.get(mainSourceSet.getResources().getSourceDirectories().getAsPath()));
      task.setTestSourcePath(testSourcePath);
    }).get();

    // add generator task as test compile dependency
    project.afterEvaluate(afterEvaluate -> {
      afterEvaluate.getTasksByName(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME, false).forEach(task -> task.dependsOn(generatorTask));
    });
  }
}
