package org.camunda.community.bpmndt.cmd;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.camunda.community.bpmndt.Constants;
import org.camunda.community.bpmndt.GeneratorContextBase;

/**
 * Collects the paths of all BPMN files within the project's resource directory ({@code src/main/resources}).
 */
public class CollectBpmnFiles extends SimpleFileVisitor<Path> implements Function<GeneratorContextBase, Collection<Path>> {

  private List<Path> bpmnFiles;

  @Override
  public Collection<Path> apply(GeneratorContextBase ctx) {
    bpmnFiles = new LinkedList<>();

    try {
      Files.walkFileTree(ctx.getMainResourcePath(), this);
    } catch (IOException e) {
      throw new RuntimeException(String.format("BPMN files under '%s' could not be collected", ctx.getMainResourcePath()), e);
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
