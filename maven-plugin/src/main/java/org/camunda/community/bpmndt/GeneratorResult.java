package org.camunda.community.bpmndt;

import java.util.LinkedList;
import java.util.List;

import com.squareup.javapoet.JavaFile;

/**
 * Class, used to collect the test code generation results.
 */
public class GeneratorResult {

  private final List<JavaFile> additionalFiles;
  private final List<JavaFile> files;

  public GeneratorResult() {
    files = new LinkedList<>();
    additionalFiles = new LinkedList<>();
  }

  public void addAdditionalFile(JavaFile javaFile) {
    additionalFiles.add(javaFile);
  }

  public void addFile(JavaFile javaFile) {
    files.add(javaFile);
  }

  public List<JavaFile> getAdditionalFiles() {
    return additionalFiles;
  }

  public List<JavaFile> getFiles() {
    return files;
  }
}
