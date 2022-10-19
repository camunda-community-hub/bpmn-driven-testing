package org.camunda.community.bpmndt.cmd;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.community.bpmndt.GeneratorContext;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

public class BuildGetProcessEnginePlugins implements Function<GeneratorContext, MethodSpec> {

  @Override
  public MethodSpec apply(GeneratorContext ctx) {
    TypeName returnType = ParameterizedTypeName.get(List.class, ProcessEnginePlugin.class);

    MethodSpec.Builder builder = MethodSpec.methodBuilder("getProcessEnginePlugins")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .returns(returnType);

    List<ClassName> classNames = ctx.getProcessEnginePluginNames().stream()
        .map(this::buildClassName)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    builder.addStatement("$T processEnginePlugins = new $T<>()", returnType, LinkedList.class);

    for (ClassName className : classNames) {
      builder.addStatement("processEnginePlugins.add(new $T())", className);
    }

    builder.addCode("\nreturn processEnginePlugins;");

    return builder.build();
  }

  protected ClassName buildClassName(String processEnginePluginName) {
    String s = processEnginePluginName.trim();

    int index = s.lastIndexOf('.');
    if (index == -1) {
      return null;
    }

    return ClassName.get(s.substring(0, index), s.substring(index + 1));
  }
}
