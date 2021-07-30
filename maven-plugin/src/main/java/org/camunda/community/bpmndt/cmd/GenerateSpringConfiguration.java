package org.camunda.community.bpmndt.cmd;

import java.util.function.Consumer;

import javax.lang.model.element.Modifier;

import org.camunda.community.bpmndt.GeneratorContext;
import org.camunda.community.bpmndt.GeneratorResult;
import org.camunda.community.bpmndt.api.cfg.AbstractConfiguration;
import org.camunda.community.bpmndt.cmd.generation.GetProcessEnginePlugins;
import org.springframework.context.annotation.Configuration;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

/**
 * Generates a Spring test configuration, which is required for Spring based test code.
 * 
 * @see AbstractConfiguration
 */
public class GenerateSpringConfiguration implements Consumer<GeneratorContext> {

  private final GeneratorResult result;

  public GenerateSpringConfiguration(GeneratorResult result) {
    this.result = result;
  }

  @Override
  public void accept(GeneratorContext ctx) {
    TypeSpec typeSpec = TypeSpec.classBuilder("BpmndtConfiguration")
        .superclass(AbstractConfiguration.class)
        .addAnnotation(Configuration.class)
        .addModifiers(Modifier.PUBLIC)
        .addMethod(new GetProcessEnginePlugins().apply(ctx))
        .build();

    JavaFile javaFile = JavaFile.builder(ctx.getPackageName(), typeSpec)
        .skipJavaLangImports(true)
        .build();

    result.addAdditionalFile(javaFile);
  }
}
