package org.camunda.bpm.extension.bpmndt.impl.generation;

import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.EXECUTE;

import java.util.List;
import java.util.function.BiFunction;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.extension.bpmndt.BpmnNode;
import org.camunda.bpm.extension.bpmndt.BpmnSupport;
import org.camunda.bpm.extension.bpmndt.GeneratorContext;
import org.camunda.bpm.extension.bpmndt.type.Path;
import org.camunda.bpm.extension.bpmndt.type.TestCase;

import com.squareup.javapoet.MethodSpec;

/**
 * Function that builds the method, which executes the actual test case.
 */
public class Execute implements BiFunction<GeneratorContext, TestCase, MethodSpec> {

  @Override
  public MethodSpec apply(GeneratorContext context, TestCase testCase) {
    BpmnSupport bpmnSupport = context.getBpmnSupport();

    MethodSpec.Builder builder = MethodSpec.methodBuilder(EXECUTE)
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .addParameter(ProcessInstance.class, "pi");

    Path path = testCase.getPath();
    
    List<String> flowNodeIds = testCase.getPath().getFlowNodeIds();
    if (flowNodeIds.isEmpty()) {
      // path is empty
      return builder.addStatement("throw new $T($S)", RuntimeException.class, "Path is empty").build();
    }
    if (!bpmnSupport.has(flowNodeIds)) {
      // path is not valid
      builder.addCode("// Not existing flow nodes:\n");
      
      for (String flowNodeId : flowNodeIds) {
        if (!bpmnSupport.has(flowNodeId)) {
          builder.addCode("// $L\n", flowNodeId);
        }
      }
      
      builder.addStatement("throw new $T($S)", RuntimeException.class, "Path is not valid");

      return builder.build();
    }

    // build flow node specific code blocks
    for (String flowNodeId : path.getFlowNodeIds()) {
      BpmnNode node = bpmnSupport.get(flowNodeId);

      builder.addCode("\n// $L: $L\n", node.getType(), node.getId());

      if (node.isAsyncBefore()) {
        builder.addStatement("assertThat(pi).isWaitingAt($S)", node.getId());
        builder.addStatement("instance.apply($L)", String.format("%sBefore", node.getLiteral()));
      }
      if (node.isCallActivity()) {
        // nothing to do here, since a CallActivity has no waiting state
      }
      if (node.isExternalTask()) {
        builder.addStatement("assertThat(pi).isWaitingAt($S)", node.getId());
        builder.addStatement("instance.apply($L)", node.getLiteral());
      }
      if (node.isIntermediateCatchEvent()) {
        builder.addStatement("assertThat(pi).isWaitingAt($S)", node.getId());
        builder.addStatement("instance.apply($L)", node.getLiteral());
      }
      if (node.isUserTask()) {
        builder.addStatement("assertThat(pi).isWaitingAt($S)", node.getId());
        builder.addStatement("instance.apply($L)", node.getLiteral());
      }
      if (node.isAsyncAfter()) {
        builder.addStatement("assertThat(pi).isWaitingAt($S)", node.getId());
        builder.addStatement("instance.apply($L)", String.format("%sAfter", node.getLiteral()));
      }

      builder.addStatement("assertThat(pi).hasPassed($S)", flowNodeId);
    }

    return builder.build();
  }
}
