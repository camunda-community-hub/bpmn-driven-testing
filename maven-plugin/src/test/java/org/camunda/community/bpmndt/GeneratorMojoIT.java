package org.camunda.community.bpmndt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeTrue;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import org.camunda.community.bpmndt.test.MavenPluginRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.robotframework.RobotFramework;

/**
 * Robot Framework based integration tests.
 */
public class GeneratorMojoIT {

  @Rule
  public MavenPluginRule mavenPlugin = new MavenPluginRule();
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void testIntegration() {
    assumeTrue("Plugin has not been installed", mavenPlugin.isInstalled());

    // set console encoding
    System.setProperty("python.console.encoding", StandardCharsets.UTF_8.name());

    List<String> arguments = new LinkedList<>();
    arguments.add("run");
    arguments.add("--consolecolors");
    arguments.add("off");
    arguments.add("-v");
    arguments.add("TEMP:" + temporaryFolder.getRoot().getAbsolutePath());
    arguments.add("-v");
    arguments.add("VERSION:" + mavenPlugin.getVersion());
    arguments.add("--exclude");
    arguments.add("ignore");
    arguments.add("--outputdir");
    arguments.add("./target/robot");
    arguments.add("./src/test/it");

    int exitCode = RobotFramework.run(arguments.toArray(new String[arguments.size()]));
    assertThat(exitCode, is(0));
  }
}
