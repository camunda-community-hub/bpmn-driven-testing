package org.camunda.community.bpmndt.cmd;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.camunda.community.bpmndt.GeneratorContext;

public class DeleteTestSources implements Function<GeneratorContext, Void> {

  @Override
  public Void apply(GeneratorContext ctx) {
    if (!Files.isDirectory(ctx.getTestSourcePath())) {
      return null;
    }

    try (Stream<Path> files = Files.list(ctx.getTestSourcePath())) {
      files.map(Path::toFile).forEach(FileUtils::deleteQuietly);
    } catch (IOException e) {
      throw new RuntimeException("failed to delete test sources", e);
    }

    return null;
  }
}
