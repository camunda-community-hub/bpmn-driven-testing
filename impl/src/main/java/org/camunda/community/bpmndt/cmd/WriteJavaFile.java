package org.camunda.community.bpmndt.cmd;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.camunda.community.bpmndt.Constants;
import org.camunda.community.bpmndt.GeneratorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.javapoet.JavaFile;

public class WriteJavaFile implements Consumer<JavaFile> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WriteJavaFile.class);

  private final GeneratorContext ctx;

  public WriteJavaFile(GeneratorContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public void accept(JavaFile javaFile) {
    String javaFileName = javaFile.typeSpec.name + Constants.JAVA_EXTENSION;
    Path javaFilePath = ctx.getTestSourcePath().resolve(javaFile.packageName.replace('.', '/')).resolve(javaFileName);

    String relativePath = ctx.getBasePath().relativize(javaFilePath).toString().replace('\\', '/');
    LOGGER.info(String.format("Writing file: %s", relativePath));

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
      throw new RuntimeException("Java file could not be written", e);
    }
  }
}
