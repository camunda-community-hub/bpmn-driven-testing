package org.camunda.community.bpmndt;

import java.nio.file.Paths;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Maven plugin goal, which runs a {@link Generator}.
 */
@Mojo(name = "generator", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES)
public class GeneratorMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  protected MavenProject project;

  /**
   * Package name of the generated Java test case files.
   */
  @Parameter(defaultValue = "generated", required = true)
  protected String packageName;

  @Override
  public void execute() throws MojoFailureException {
    var testSourcePath = Paths.get(project.getBuild().getDirectory()).resolve(Constants.EXTENSION_NAME);

    // add test source directory
    getLog().info(String.format("Adding test source directory: %s", testSourcePath));
    project.addTestCompileSourceRoot(testSourcePath.toAbsolutePath().toString());

    getLog().info("");

    var ctx = new GeneratorContext();
    ctx.setBasePath(project.getBasedir().toPath());
    ctx.setMainResourcePath(Paths.get(project.getBuild().getResources().get(0).getDirectory()));
    ctx.setPackageName(packageName);
    ctx.setTestSourcePath(testSourcePath);

    // generate test code
    try {
      new Generator().generate(ctx);
    } catch (RuntimeException e) {
      throw new MojoFailureException("Unexpected error occurred", e);
    }
  }
}
