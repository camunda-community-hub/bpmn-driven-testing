package org.camunda.bpm.extension.bpmndt.impl.generation;

import java.util.function.BiFunction;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.extension.bpmndt.GeneratorContext;
import org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants;
import org.camunda.bpm.extension.bpmndt.type.TestCase;
import org.junit.Test;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;

public class TestMethodPathEmpty implements BiFunction<GeneratorContext, TestCase, MethodSpec> {

  @Override
  public MethodSpec apply(GeneratorContext context, TestCase testCase) {
    AnnotationSpec deploymentAnnotation = AnnotationSpec.builder(Deployment.class)
        .addMember("resources", "{$S}", context.getBpmnResourceName())
        .build();

    return MethodSpec.methodBuilder(GeneratorConstants.TEST_PATH)
        .addAnnotation(Test.class)
        .addAnnotation(deploymentAnnotation)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addStatement("throw new $T($S)", RuntimeException.class, "Path is empty")
        .build();
  }
}
