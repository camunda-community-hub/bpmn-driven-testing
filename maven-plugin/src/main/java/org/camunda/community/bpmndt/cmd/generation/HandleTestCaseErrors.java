package org.camunda.community.bpmndt.cmd.generation;

import java.util.function.BiConsumer;

import org.camunda.community.bpmndt.TestCaseContext;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;

public class HandleTestCaseErrors implements BiConsumer<TestCaseContext, MethodSpec.Builder> {

  @Override
  public void accept(TestCaseContext ctx, Builder builder) {
    if (ctx.isPathEmpty()) {
      builder.addStatement("throw new $T($S)", RuntimeException.class, "Path is empty").build();
    } else if (ctx.isPathIncomplete()) {
      builder.addStatement("throw new $T($S)", RuntimeException.class, "Path is incomplete").build();
    } else if (ctx.isPathInvalid()) {
      builder.addCode("\n// Not existing flow nodes:\n");

      for (String flowNodeId : ctx.getInvalidFlowNodeIds()) {
        builder.addCode("// $L\n", flowNodeId);
      }

      builder.addStatement("throw new $T($S)", RuntimeException.class, "Path is invalid").build();
    }
  }
}
