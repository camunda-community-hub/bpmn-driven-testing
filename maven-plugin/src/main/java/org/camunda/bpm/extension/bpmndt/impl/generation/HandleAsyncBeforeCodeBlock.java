package org.camunda.bpm.extension.bpmndt.impl.generation;

import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.PROCESS_INSTANCE;

import java.util.function.Function;

import org.camunda.bpm.extension.bpmndt.BpmnNode;

import com.squareup.javapoet.CodeBlock;

public class HandleAsyncBeforeCodeBlock implements Function<BpmnNode, CodeBlock> {

  @Override
  public CodeBlock apply(BpmnNode node) {
    return CodeBlock.builder()
        .addStatement("assertThat($L).isWaitingAt($S)", PROCESS_INSTANCE, node.getId())
        .addStatement("$L_before(job($S, $L))", node.getLiteral(), node.getId(), PROCESS_INSTANCE)
        .build();
  }
}
