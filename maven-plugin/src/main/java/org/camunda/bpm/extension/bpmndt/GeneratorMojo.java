package org.camunda.bpm.extension.bpmndt;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Maven plugin goal, which runs a {@link GeneratorTask}.
 */
@Mojo(name = "generator", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES, requiresProject = true)
public class GeneratorMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  protected MavenProject project;

  /** Package name of the generated Java test case files. */
  @Parameter(defaultValue = "generated", required = true)
  protected String packageName;

  /** Enables Spring based testing. */
  @Parameter(defaultValue = "false", required = true)
  protected boolean springEnabled;

  /** Name of the test source directory, with the build directory (target). */
  @Parameter(defaultValue = "bpmndt", required = true)
  protected String testSourceDirectory;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    Path testSourcePath = Paths.get(project.getBuild().getDirectory()).resolve(testSourceDirectory);

    // add test source directory
    getLog().info(String.format("Adding test source directory: %s", testSourcePath));
    project.addTestCompileSourceRoot(testSourcePath.toAbsolutePath().toString());

    getLog().info("");

    GeneratorTask task = new GeneratorTask(getLog());
    task.basePath = project.getBasedir().toPath();
    task.mainResourcePath = Paths.get(project.getBuild().getResources().get(0).getDirectory());
    task.testSourcePath = testSourcePath;

    task.packageName = packageName;
    task.springEnabled = springEnabled;

    // run generator
    try {
      task.execute();
    } catch (RuntimeException e) {
      throw new MojoFailureException("Unexpected error occurred", e);
    }
  }
}
