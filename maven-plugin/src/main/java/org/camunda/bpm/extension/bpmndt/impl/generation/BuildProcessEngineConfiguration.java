package org.camunda.bpm.extension.bpmndt.impl.generation;

import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.BUILD_PROCESS_ENGINE_CONFIGURATION;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.PROCESS_ENGINE_CONFIGURATION;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.PROCESS_ENGINE_NAME;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.TYPE_BPMNDT_PLUGIN;

import java.util.Arrays;
import java.util.function.Function;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.extension.bpmndt.GeneratorContext;
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin;

import com.squareup.javapoet.MethodSpec;

public class BuildProcessEngineConfiguration implements Function<GeneratorContext, MethodSpec> {

  @Override
  public MethodSpec apply(GeneratorContext context) {
    Object[] newArgs = new Object[3];
    newArgs[0] = ProcessEngineConfigurationImpl.class;
    newArgs[1] = PROCESS_ENGINE_CONFIGURATION;
    newArgs[2] = StandaloneInMemProcessEngineConfiguration.class;

    Object[] pluginsArgs = new Object[4];
    pluginsArgs[0] = PROCESS_ENGINE_CONFIGURATION;
    pluginsArgs[1] = Arrays.class;
    pluginsArgs[2] = SpinProcessEnginePlugin.class;
    pluginsArgs[3] = context.getTypeName(TYPE_BPMNDT_PLUGIN);

    return MethodSpec.methodBuilder(BUILD_PROCESS_ENGINE_CONFIGURATION)
        .addModifiers(Modifier.PROTECTED)
        .returns(ProcessEngineConfigurationImpl.class)
        .addStatement("$T $L = new $T()", newArgs)
        .addStatement("$L.setProcessEngineName($S)", PROCESS_ENGINE_CONFIGURATION, PROCESS_ENGINE_NAME)
        .addStatement("$L.setProcessEnginePlugins($T.asList(new $T(), new $T()))", pluginsArgs)
        .addCode("\n")
        .addStatement("return $L", PROCESS_ENGINE_CONFIGURATION)
        .build();
  }
}
