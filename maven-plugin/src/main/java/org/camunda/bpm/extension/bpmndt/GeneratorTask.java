package org.camunda.bpm.extension.bpmndt;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.Log;
import org.camunda.bpm.extension.bpmndt.type.TestCase;

import com.squareup.javapoet.JavaFile;

/**
 * Task, which is executed by the {@code GeneratorMojo} during Maven builds.
 */
public class GeneratorTask {

  private final Log log;

  protected Path basePath;
  protected Path mainResourcePath;
  protected Path testSourcePath;

  protected String packageName;
  protected boolean springEnabled;

  public GeneratorTask(Log log) {
    this.log = log;
  }

  protected Path buildJavaFilePath(JavaFile javaFile) {
    String javaFileName = javaFile.typeSpec.name + Constants.JAVA_EXTENSION;
    return testSourcePath.resolve(javaFile.packageName.replace('.', '/')).resolve(javaFileName);
  }

  protected void deleteJavaFiles() {
    if (!Files.isDirectory(testSourcePath)) {
      return;
    }

    try {
      Files.list(testSourcePath).map(Path::toFile).forEach(FileUtils::deleteQuietly);
    } catch (IOException e) {
      throw new RuntimeException("Generated Java files in '%s' could not be deleted", e);
    }
  }

  public void execute() {
    // delete old Java files, that has been generated previously
    deleteJavaFiles();

    // collect BPMN files
    Collection<Path> bpmnFiles = BpmnSupport.collectFiles(mainResourcePath);
    for (Path bpmnFile : bpmnFiles) {
      log.info(String.format("Found BPMN file: %s", relativize(mainResourcePath, bpmnFile)));
    }

    List<JavaFile> javaFiles = new LinkedList<>();

    // generate test cases for each BPMN file
    for (Path bpmnFile : bpmnFiles) {
      log.info("");
      generate(javaFiles, bpmnFile);
    }

    log.info("");

    // write test cases
    log.info("Writing test cases");
    javaFiles.forEach(this::writeJavaFile);

    log.info("");

    // generate and write framework classes
    log.info("Writing framework classes");
    generateFramework().forEach(this::writeJavaFile);
  }

  protected void generate(List<JavaFile> javaFiles, Path bpmnFile) {
    BpmnSupport bpmnSupport = BpmnSupport.of(bpmnFile);
    log.info(String.format("Process: %s", bpmnSupport.getProcessId()));

    // build generator
    Generator generator = Generator.builder()
        .bpmnResourceName(relativize(mainResourcePath, bpmnFile))
        .bpmnSupport(bpmnSupport)
        .packageName(packageName)
        .springEnabled(springEnabled)
        .build();

    // get test cases from BPMN model
    List<TestCase> testCases = bpmnSupport.getTestCases();
    if (testCases.isEmpty()) {
      log.info("No test cases defined");
      return;
    }

    Set<String> testCaseNames = new HashSet<>();

    // generate test cases
    for (TestCase testCase : testCases) {
      String testCaseName = testCase.getName();

      // check for duplicate test case names
      if (testCaseNames.contains(testCaseName)) {
        log.warn(String.format("Skipping test case '%s': Name must be unique", testCaseName));
        continue;
      }

      log.info(String.format("Generating test case '%s'", testCaseName));
      javaFiles.add(generator.generate(testCase));

      testCaseNames.add(testCaseName);
    }

    testCaseNames.clear();
  }

  protected List<JavaFile> generateFramework() {
    Generator generator = Generator.builder()
        .packageName(packageName)
        .springEnabled(springEnabled)
        .build();

    return generator.generateFramework();
  }

  private String relativize(Path parent, Path child) {
    return parent.relativize(child).toString().replace('\\', '/');
  }

  protected void writeJavaFile(JavaFile javaFile) {
    Path javaFilePath = buildJavaFilePath(javaFile);
    log.info(String.format("Writing file: %s", relativize(basePath, javaFilePath)));

    // create parent directories
    try {
      Files.createDirectories(javaFilePath.getParent());
    } catch (IOException e) {
      throw new RuntimeException("Parent directories could not be created", e);
    }

    // write Java file
    try (Writer w = Files.newBufferedWriter(javaFilePath, StandardCharsets.UTF_8)) {
      javaFile.writeTo(w);
    } catch (IOException e) {
      throw new RuntimeException("Test case could not be written", e);
    }
  }
}
