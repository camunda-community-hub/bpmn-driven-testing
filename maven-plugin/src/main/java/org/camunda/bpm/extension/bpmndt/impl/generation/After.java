package org.camunda.bpm.extension.bpmndt.impl.generation;

import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.AFTER;

import java.util.function.Function;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.extension.bpmndt.GeneratorContext;

import com.squareup.javapoet.MethodSpec;

public class After implements Function<GeneratorContext, MethodSpec> {

  @Override
  public MethodSpec apply(GeneratorContext context) {
    StringBuilder javadocBuiler = new StringBuilder();
    javadocBuiler.append("Overwrite to assert state after the test case.");

    return MethodSpec.methodBuilder(AFTER)
        .addJavadoc(javadocBuiler.toString())
        .addModifiers(Modifier.PROTECTED)
        .build();
  }
}
