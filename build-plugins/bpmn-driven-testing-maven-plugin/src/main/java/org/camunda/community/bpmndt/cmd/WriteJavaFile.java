package org.camunda.community.bpmndt.cmd;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.apache.maven.plugin.logging.Log;
import org.camunda.community.bpmndt.Constants;
import org.camunda.community.bpmndt.GeneratorContext;

import com.squareup.javapoet.JavaFile;

public class WriteJavaFile implements Consumer<JavaFile> {

  private final Log log;

  private final GeneratorContext ctx;

  public WriteJavaFile(Log log, GeneratorContext ctx) {
    this.log = log;
    this.ctx = ctx;
  }

  @Override
  public void accept(JavaFile javaFile) {
    String javaFileName = javaFile.typeSpec.name + Constants.JAVA_EXTENSION;
    Path javaFilePath = ctx.getTestSourcePath().resolve(javaFile.packageName.replace('.', '/')).resolve(javaFileName);

    String relativePath = ctx.getBasePath().relativize(javaFilePath).toString().replace('\\', '/');
    log.info(String.format("Writing file: %s", relativePath));

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
