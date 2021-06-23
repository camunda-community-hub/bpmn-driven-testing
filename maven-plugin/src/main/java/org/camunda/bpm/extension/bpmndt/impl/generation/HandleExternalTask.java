package org.camunda.bpm.extension.bpmndt.impl.generation;

import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.PROCESS_ENGINE_RULE;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.TASK;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.TOPIC_NAME;

import java.util.function.Function;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.extension.bpmndt.BpmnNode;

import com.squareup.javapoet.MethodSpec;

public class HandleExternalTask implements Function<BpmnNode, MethodSpec> {

  @Override
  public MethodSpec apply(BpmnNode node) {
    return MethodSpec.methodBuilder(node.getLiteral())
        .addJavadoc("Overwrite to handle external task $L.", node.getId())
        .addModifiers(Modifier.PROTECTED)
        .addParameter(String.class, TOPIC_NAME)
        .addCode("$T $L = $L.getExternalTaskService()\n", LockedExternalTask.class, TASK, PROCESS_ENGINE_RULE)
        .addCode("    .fetchAndLock(1, $S)\n", "bpmndt-worker")
        .addCode("    .topic($L, 1000L)\n", TOPIC_NAME)
        .addCode("    .execute().get(0);\n")
        .addCode("\n")
        .addStatement("$L.getExternalTaskService().complete($L.getId(), $S)", PROCESS_ENGINE_RULE, TASK, "bpmndt-worker")
        .build();
  }
}
