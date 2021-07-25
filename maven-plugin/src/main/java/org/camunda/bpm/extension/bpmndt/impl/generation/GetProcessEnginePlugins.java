package org.camunda.bpm.extension.bpmndt.impl.generation;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.extension.bpmndt.GeneratorContext;
import org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants;
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;

public class GetProcessEnginePlugins implements Function<GeneratorContext, MethodSpec> {

  @Override
  public MethodSpec apply(GeneratorContext context) {
    return MethodSpec.methodBuilder(GeneratorConstants.GET_PROCESS_ENGINE_PLUGINS)
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .returns(ParameterizedTypeName.get(List.class, ProcessEnginePlugin.class))
        .addStatement("return $T.singletonList(new $T())", Collections.class, SpinProcessEnginePlugin.class)
        .build();
  }
}
