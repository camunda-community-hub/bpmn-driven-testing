package org.camunda.bpm.extension.bpmndt.impl.generation;

import java.util.function.Function;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.bpmndt.BpmnNode;

import com.squareup.javapoet.MethodSpec;

public class HandleCallActivityOutput implements Function<BpmnNode, MethodSpec> {

  @Override
  public MethodSpec apply(BpmnNode node) {
    return MethodSpec.methodBuilder(String.format("%s_output", node.getLiteral()))
        .addJavadoc("Overwrite to assert output mapping of call activity $L.", node.getId())
        .addModifiers(Modifier.PROTECTED)
        .addParameter(DelegateExecution.class, "execution")
        .build();
  }
}
