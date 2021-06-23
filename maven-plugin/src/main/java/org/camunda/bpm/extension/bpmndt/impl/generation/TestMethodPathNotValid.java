package org.camunda.bpm.extension.bpmndt.impl.generation;

import java.util.List;
import java.util.function.BiFunction;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.extension.bpmndt.BpmnSupport;
import org.camunda.bpm.extension.bpmndt.GeneratorContext;
import org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants;
import org.camunda.bpm.extension.bpmndt.type.TestCase;
import org.junit.Test;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;

public class TestMethodPathNotValid implements BiFunction<GeneratorContext, TestCase, MethodSpec> {

  @Override
  public MethodSpec apply(GeneratorContext context, TestCase testCase) {
    AnnotationSpec deploymentAnnotation = AnnotationSpec.builder(Deployment.class)
        .addMember("resources", "{$S}", context.getBpmnResourceName())
        .build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder(GeneratorConstants.TEST_PATH)
        .addAnnotation(Test.class)
        .addAnnotation(deploymentAnnotation)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addCode("// Not existing flow nodes:\n");

    BpmnSupport bpmnSupport = context.getBpmnSupport();

    List<String> flowNodeIds = testCase.getPath().getFlowNodeIds();
    for (String flowNodeId : flowNodeIds) {
      if (!bpmnSupport.has(flowNodeId)) {
        builder.addCode("// $L\n", flowNodeId);
      }
    }

    return builder.addStatement("throw new $T($S)", RuntimeException.class, "Path is not valid")
        .build();
  }
}
