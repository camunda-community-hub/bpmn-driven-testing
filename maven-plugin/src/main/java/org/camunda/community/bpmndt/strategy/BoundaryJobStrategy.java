package org.camunda.community.bpmndt.strategy;

import org.camunda.community.bpmndt.TestCaseActivity;

import com.squareup.javapoet.CodeBlock;

/**
 * Strategy for boundary timer events.
 */
public class BoundaryJobStrategy extends JobStrategy {

  @Override
  protected CodeBlock buildHandlerMethodJavadoc() {
    if (!activity.hasPrev()) {
      return super.buildHandlerMethodJavadoc();
    }

    TestCaseActivity prev = activity.getPrev();
    if (!activity.isAttachedTo(prev)) {
      return super.buildHandlerMethodJavadoc();
    }

    CodeBlock.Builder builder = CodeBlock.builder();

    Object[] args = {activity.getTypeName(), activity.getId(), prev.getTypeName(), prev.getId()};
    builder.add("Returns the handler for $L: $L attached to $L: $L", args);
    builder.add("\n\n@see #$L", buildHandlerMethodName(prev.getLiteral()));

    return builder.build();
  }
}
