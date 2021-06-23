package org.camunda.bpm.extension.bpmndt.impl.generation;

import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.BUILD_PROCESS_ENGINE;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.BUILD_PROCESS_ENGINE_CONFIGURATION;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.PROCESS_ENGINE;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.PROCESS_ENGINE_CONFIGURATION;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.PROCESS_ENGINE_NAME;

import java.util.function.Function;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.extension.bpmndt.GeneratorContext;

import com.squareup.javapoet.MethodSpec;

public class BuildProcessEngine implements Function<GeneratorContext, MethodSpec> {

  @Override
  public MethodSpec apply(GeneratorContext context) {
    Object[] args = new Object[3];
    args[0] = ProcessEngineConfigurationImpl.class;
    args[1] = PROCESS_ENGINE_CONFIGURATION;
    args[2] = BUILD_PROCESS_ENGINE_CONFIGURATION;

    return MethodSpec.methodBuilder(BUILD_PROCESS_ENGINE)
        .addModifiers(Modifier.PROTECTED)
        .returns(ProcessEngine.class)
        .addStatement("$T $L = $T.getProcessEngine($S)", ProcessEngine.class, PROCESS_ENGINE, ProcessEngines.class, PROCESS_ENGINE_NAME)
        .beginControlFlow("if ($L == null)", PROCESS_ENGINE)
        .addStatement("$T $L = $L()", args)
        .addCode("\n")
        .addStatement("$L = $L.buildProcessEngine()", PROCESS_ENGINE, PROCESS_ENGINE_CONFIGURATION)
        .endControlFlow()
        .addCode("\n")
        .addStatement("return $L", PROCESS_ENGINE)
        .build();
  }
}
