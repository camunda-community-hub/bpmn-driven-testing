package org.camunda.bpm.extension.bpmndt.impl.generation;

import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.PROCESS_ENGINE_RULE;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.TASK;

import java.util.function.Function;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.extension.bpmndt.BpmnNode;

import com.squareup.javapoet.MethodSpec;

public class HandleUserTask implements Function<BpmnNode, MethodSpec> {

  @Override
  public MethodSpec apply(BpmnNode node) {
    return MethodSpec.methodBuilder(node.getLiteral())
        .addJavadoc("Overwrite to handle user task $L.", node.getId())
        .addModifiers(Modifier.PROTECTED)
        .addParameter(Task.class, TASK)
        .addStatement("$L.getTaskService().complete($L.getId())", PROCESS_ENGINE_RULE, TASK)
        .build();
  }
}
