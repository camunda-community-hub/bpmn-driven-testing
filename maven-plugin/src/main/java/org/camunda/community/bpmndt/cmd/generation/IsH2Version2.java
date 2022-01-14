package org.camunda.community.bpmndt.cmd.generation;

import java.util.function.Function;

import javax.lang.model.element.Modifier;

import org.camunda.community.bpmndt.GeneratorContext;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

/**
 * Function that builds the method, which determines if H2 is used in version 2.
 */
public class IsH2Version2 implements Function<GeneratorContext, MethodSpec> {

  @Override
  public MethodSpec apply(GeneratorContext ctx) {
    return MethodSpec.methodBuilder("isH2Version2")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .returns(TypeName.BOOLEAN)
        .addStatement("return $L", ctx.isH2Version2())
        .build();
  }
}
