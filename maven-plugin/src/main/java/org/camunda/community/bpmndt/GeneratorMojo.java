package org.camunda.community.bpmndt;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Maven plugin goal, which runs a {@link Generator}.
 */
@Mojo(name = "generator", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES, requiresProject = true)
public class GeneratorMojo extends AbstractMojo {

  protected static final String H2_GROUP_ID = "com.h2database";
  protected static final String H2_ARTIFACT_ID = "h2";

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  protected MavenProject project;

  /** Package name of the generated Java test case files. */
  @Parameter(defaultValue = "generated", required = true)
  protected String packageName;

  /** List of process engine plugins to register at the process engine. */
  @Parameter
  protected List<String> processEnginePlugins;

  /** Determines if Spring based testing is enabled or not. */
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

    GeneratorContext ctx = new GeneratorContext();
    ctx.setBasePath(project.getBasedir().toPath());
    ctx.setH2Version2(isH2Version2());
    ctx.setMainResourcePath(Paths.get(project.getBuild().getResources().get(0).getDirectory()));
    ctx.setPackageName(packageName);
    ctx.setProcessEnginePluginNames(processEnginePlugins);
    ctx.setSpringEnabled(springEnabled);
    ctx.setTestSourcePath(testSourcePath);

    // generate test code
    try {
      new Generator(getLog()).generate(ctx);
    } catch (RuntimeException e) {
      throw new MojoFailureException("Unexpected error occurred", e);
    }
  }

  private boolean isH2(Dependency dependency) {
    return H2_GROUP_ID.equals(dependency.getGroupId()) && H2_ARTIFACT_ID.equals(dependency.getArtifactId());
  }

  protected boolean isH2Version2() {
    // finds the version of the H2 artifact within the project's dependencies
    Optional<String> h2Version = project.getDependencies().stream()
        .filter(this::isH2)
        .map(Dependency::getVersion)
        .findFirst();

    if (!h2Version.isPresent()) {
      // guess: old version of H2 coming as transitive dependency
      return false;
    }

    String version = h2Version.get();
    int index = version.indexOf('.');

    if (index <= 0) {
      return false;
    }

    try {
      return Integer.parseInt(version.substring(0, index)) >= 2;
    } catch (NumberFormatException e) {
      // ignore exception, since the major version must be a number
      return false;
    }
  }
}
