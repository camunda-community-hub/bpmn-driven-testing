package org.camunda.community.bpmndt;

import java.nio.file.Path;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

/**
 * Custom Gradle task, which generates the test code.
 */
public class GeneratorTask extends DefaultTask {

  /**
   * Name of the task, used for registration.
   */
  protected static final String NAME = "generateTestCases";

  @Internal
  private GradleExtension extension;

  @Internal
  private Path mainResourcePath;

  @Internal
  private Path testSourcePath;

  @TaskAction
  public void generate() {
    var packageName = extension.getPackageName().getOrElse("generated");

    var ctx = new GeneratorContext();
    ctx.setBasePath(getProject().getProjectDir().toPath());
    ctx.setMainResourcePath(mainResourcePath);
    ctx.setPackageName(packageName);
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
}
