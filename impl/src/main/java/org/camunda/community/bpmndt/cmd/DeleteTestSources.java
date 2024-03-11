package org.camunda.community.bpmndt.cmd;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.camunda.community.bpmndt.GeneratorContextBase;

public class DeleteTestSources implements Function<GeneratorContextBase, Void> {

  @Override
  public Void apply(GeneratorContextBase ctx) {
    if (!Files.isDirectory(ctx.getTestSourcePath())) {
      return null;
    }

    try (Stream<Path> stream = Files.list(ctx.getTestSourcePath())) {
      stream.map(Path::toFile).forEach(FileUtils::deleteQuietly);
    } catch (IOException e) {
      throw new RuntimeException("Test sources could not be deleted", e);
    }

    return null;
  }
}
