package org.camunda.bpm.extension.bpmndt.impl;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.camunda.bpm.extension.bpmndt.Constants;

/**
 * Collects the paths of all BPMN files within a directory tree.
 */
class BpmnFileCollector extends SimpleFileVisitor<Path> {

  private final Path start;

  private List<Path> bpmnFiles;

  BpmnFileCollector(Path start) {
    this.start = start;
  }

  protected Collection<Path> collect() {
    bpmnFiles = new LinkedList<>();

    try {
      Files.walkFileTree(start, this);
    } catch (IOException e) {
      throw new RuntimeException(String.format("BPMN files under '%s' could not be collected", start), e);
    }

    return bpmnFiles;
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    if (file.getFileName().toString().endsWith(Constants.BPMN_EXTENSION)) {
      bpmnFiles.add(file);
    }

    return FileVisitResult.CONTINUE;
  }
}
