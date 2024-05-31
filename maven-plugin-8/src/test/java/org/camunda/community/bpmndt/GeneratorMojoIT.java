package org.camunda.community.bpmndt;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedList;

import org.camunda.community.bpmndt.test.MavenPluginExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.robotframework.RobotFramework;

/**
 * Robot Framework based integration tests.
 */
public class GeneratorMojoIT {

  @RegisterExtension
  public MavenPluginExtension mavenPlugin = new MavenPluginExtension();

  @Test
  public void testIntegration(@TempDir Path temporaryDirectory) {
    assumeTrue(mavenPlugin.isInstalled(), "Maven plugin has not been installed");

    // set console encoding
    System.setProperty("python.console.encoding", StandardCharsets.UTF_8.name());

    var arguments = new LinkedList<String>();
    arguments.add("run");
    arguments.add("--consolecolors");
    arguments.add("off");
    arguments.add("-v");
    arguments.add("TEMP:" + temporaryDirectory.toAbsolutePath());
    arguments.add("-v");
    arguments.add("VERSION:" + mavenPlugin.getVersion());
    arguments.add("--include");
    arguments.add("maven");
    arguments.add("--exclude");
    arguments.add("ignore");
    arguments.add("--outputdir");
    arguments.add("./target/robot");
    arguments.add("../integration-tests-8");

    int exitCode = RobotFramework.run(arguments.toArray(new String[0]));
    assertThat(exitCode).isEqualTo(0);
  }
}
