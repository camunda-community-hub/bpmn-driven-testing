package org.camunda.bpm.extension.bpmndt.test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * JUnit test rule, which installs the packaged Maven plugin from the target directory into the
 * local repository. This rule requires to have the "mvn" executable within the {@code PATH}
 * environment variable.
 */
public class MavenPluginRule extends TestWatcher {

  /** Path to the plugin JAR. */
  private Path path;
  /** Name of the plugin JAR file. */
  private String fileName;

  private String groupId;
  private String artifactId;
  private String version;

  protected String extractArtifactId(String fileName) {
    String artifactId = fileName.substring(0, fileName.lastIndexOf('-'));

    if (fileName.endsWith("-SNAPSHOT.jar")) {
      artifactId = artifactId.substring(0, artifactId.lastIndexOf('-'));
    }

    return artifactId;
  }

  protected String extractVersion(String fileName) {
    if (fileName.endsWith("-SNAPSHOT.jar")) {
      fileName = fileName.substring(0, fileName.lastIndexOf('-'));
      return fileName.substring(fileName.lastIndexOf('-') + 1, fileName.length()) + "-SNAPSHOT";
    } else {
      return fileName.substring(fileName.lastIndexOf('-') + 1, fileName.length() - 4);
    }
  }

  /**
   * Returns the version of the installed Maven plugin jar.
   * 
   * @return The plugin version e.g. "0.1".
   */
  public String getVersion() {
    return version;
  }

  /**
   * Determines if the Maven plugin jar has been installed into local plugin repository or not.
   * 
   * @return {@code true}, if the plugin is installed. Otherwise {@code false}.
   */
  public boolean isInstalled() {
    return path != null;
  }

  protected void installPluginJar() {
    boolean windows = System.getProperty("os.name").toLowerCase().startsWith("windows");

    List<String> command = new LinkedList<>();

    if (windows) {
      command.add("cmd");
      command.add("/c");
    } else {
      command.add("/bin/sh");
      command.add("-c");
    }

    command.add("mvn");
    command.add("install:install-file");
    command.add("-Dfile=" + path.toString());
    command.add("-DpomFile=./pom.xml");
    command.add("-DgroupId=" + groupId);
    command.add("-DartifactId=" + artifactId);
    command.add("-Dversion=" + version);
    command.add("-Dpackaging=maven-plugin");

    try {
      Process process = new ProcessBuilder(command).redirectErrorStream(true).start();

      InputStream is = process.getInputStream();
      while (is.read() != -1);

      process.waitFor();
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException("Plugin jar could not be installed into local plugin repository", e);
    }
  }

  @Override
  protected void starting(Description description) {
    if (path != null) {
      // Maven plugin already installed
      return;
    }

    // find plugin JAR
    Optional<Path> pluginJar;
    try {
      pluginJar = Files.list(Paths.get("./target"))
          .filter(p -> p.getFileName().toString().endsWith(".jar"))
          .findFirst();
    } catch (IOException e) {
      throw new RuntimeException("Could not find plugin JAR file", e);
    }

    if (!pluginJar.isPresent()) {
      // plugin has not been packaged
      return;
    }

    // e.g.: ./target/bpmn-driven-testing-maven-plugin-0.1.jar
    path = pluginJar.get();
    // e.g.: bpmn-driven-testing-maven-plugin-0.1.jar
    fileName = path.getFileName().toString();

    groupId = "org.camunda.bpm.extension";
    // e.g.: bpmn-driven-testing-maven-plugin
    artifactId = extractArtifactId(fileName);
    // e.g.: 0.1
    version = extractVersion(fileName);

    // install Maven plugin
    installPluginJar();
  }
}
