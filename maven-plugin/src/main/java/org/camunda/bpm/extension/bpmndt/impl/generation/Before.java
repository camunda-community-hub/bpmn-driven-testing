package org.camunda.bpm.extension.bpmndt.impl.generation;

import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.BEFORE;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.VARIABLES;

import java.util.function.Function;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.extension.bpmndt.GeneratorContext;

import com.squareup.javapoet.MethodSpec;

public class Before implements Function<GeneratorContext, MethodSpec> {

  @Override
  public MethodSpec apply(GeneratorContext context) {
    StringBuilder javadocBuiler = new StringBuilder();
    javadocBuiler.append("Overwrite to set initial process variables");

    if (!context.isSpringEnabled()) {
      javadocBuiler.append(" and register beans (Mocks#register)");
    }

    javadocBuiler.append('.');
    javadocBuiler.append("\n\n");
    javadocBuiler.append("@return The business key to use or {@code null}.");

    return MethodSpec.methodBuilder(BEFORE)
        .addJavadoc(javadocBuiler.toString())
        .addModifiers(Modifier.PROTECTED)
        .returns(String.class)
        .addParameter(VariableMap.class, VARIABLES)
        .addStatement("return null")
        .build();
  }
}
