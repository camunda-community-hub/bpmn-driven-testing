package org.camunda.bpm.extension.bpmndt.impl.generation;

import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.JOB;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.PROCESS_ENGINE_RULE;

import java.util.function.Function;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.extension.bpmndt.BpmnNode;
import org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants;

import com.squareup.javapoet.MethodSpec;

public class HandleAsyncBefore implements Function<BpmnNode, MethodSpec> {

  @Override
  public MethodSpec apply(BpmnNode node) {
    return MethodSpec.methodBuilder(String.format("%s_before", node.getLiteral()))
        .addJavadoc("Overwrite to assert the state before $L $L.", node.getType(), node.getId())
        .addModifiers(Modifier.PROTECTED)
        .addParameter(Job.class, GeneratorConstants.JOB)
        .addStatement("$L.getManagementService().executeJob($L.getId())", PROCESS_ENGINE_RULE, JOB)
        .build();
  }
}
