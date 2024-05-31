package org.camunda.community.bpmndt.cmd;

import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.camunda.community.bpmndt.GeneratorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteJavaType implements Consumer<Class<?>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WriteJavaType.class);

  private final GeneratorContext ctx;

  public WriteJavaType(GeneratorContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public void accept(Class<?> type) {
    var resourceName = String.format("%s.java", type.getName().replace('.', '/'));
    var javaTypePath = ctx.getTestSourcePath().resolve(resourceName);

    var relativePath = ctx.getBasePath().relativize(javaTypePath).toString().replace('\\', '/');
    LOGGER.info(String.format("Writing file: %s", relativePath));

    // create parent directories
    try {
      Files.createDirectories(javaTypePath.getParent());
    } catch (IOException e) {
      throw new RuntimeException("failed to create parent directories", e);
    }

    var resource = this.getClass().getClassLoader().getResourceAsStream(resourceName);
    if (resource == null) {
      throw new RuntimeException(String.format("Java type resource '%s' could not be found", resourceName));
    }

    // write Java type
    try {
      FileUtils.copyInputStreamToFile(resource, javaTypePath.toFile());
    } catch (IOException e) {
      throw new RuntimeException(String.format("Java type '%s' could not be written", type.getName()), e);
    }
  }
}
