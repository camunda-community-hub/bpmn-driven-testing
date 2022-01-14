package org.camunda.community.bpmndt.cmd;

import java.util.function.Consumer;

import javax.lang.model.element.Modifier;

import org.camunda.community.bpmndt.GeneratorContext;
import org.camunda.community.bpmndt.GeneratorResult;
import org.camunda.community.bpmndt.api.cfg.SpringConfiguration;
import org.camunda.community.bpmndt.cmd.generation.GetProcessEnginePlugins;
import org.camunda.community.bpmndt.cmd.generation.IsH2Version2;
import org.springframework.context.annotation.Configuration;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

/**
 * Generates a Spring test configuration, which is required for Spring based test code.
 * 
 * @see SpringConfiguration
 */
public class GenerateSpringConfiguration implements Consumer<GeneratorContext> {

  private final GeneratorResult result;

  public GenerateSpringConfiguration(GeneratorResult result) {
    this.result = result;
  }

  @Override
  public void accept(GeneratorContext ctx) {
    TypeSpec.Builder builder = TypeSpec.classBuilder("BpmndtConfiguration")
        .superclass(SpringConfiguration.class)
        .addAnnotation(Configuration.class)
        .addModifiers(Modifier.PUBLIC);

    if (!ctx.getProcessEnginePluginNames().isEmpty()) {
      builder.addMethod(new GetProcessEnginePlugins().apply(ctx));
    }

    if (!ctx.isH2Version2()) {
      builder.addMethod(new IsH2Version2().apply(ctx));
    }

    JavaFile javaFile = JavaFile.builder(ctx.getPackageName(), builder.build())
        .skipJavaLangImports(true)
        .build();

    result.addAdditionalFile(javaFile);
  }
}
