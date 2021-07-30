package org.camunda.community.bpmndt.cmd.generation;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.community.bpmndt.GeneratorContext;
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;

public class GetProcessEnginePlugins implements Function<GeneratorContext, MethodSpec> {

  @Override
  public MethodSpec apply(GeneratorContext ctx) {
    return MethodSpec.methodBuilder("getProcessEnginePlugins")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .returns(ParameterizedTypeName.get(List.class, ProcessEnginePlugin.class))
        .addStatement("return $T.singletonList(new $T())", Collections.class, SpinProcessEnginePlugin.class)
        .build();
  }
}
