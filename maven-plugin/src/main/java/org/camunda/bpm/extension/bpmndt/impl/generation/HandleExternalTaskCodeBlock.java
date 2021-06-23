package org.camunda.bpm.extension.bpmndt.impl.generation;

import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.PROCESS_INSTANCE;

import java.util.function.Function;

import org.camunda.bpm.extension.bpmndt.BpmnNode;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;

import com.squareup.javapoet.CodeBlock;

public class HandleExternalTaskCodeBlock implements Function<BpmnNode, CodeBlock> {

  @Override
  public CodeBlock apply(BpmnNode node) {
    ServiceTask serviceTask = node.as(ServiceTask.class);

    return CodeBlock.builder()
        .addStatement("assertThat($L).isWaitingAt($S)", PROCESS_INSTANCE, node.getId())
        .addStatement("$L($S)", node.getLiteral(), serviceTask.getCamundaTopic())
        .build();
  }
}
