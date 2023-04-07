package org.camunda.community.bpmndt.strategy;

import org.camunda.community.bpmndt.Generator;
import org.camunda.community.bpmndt.model.TestCaseActivity;

import com.squareup.javapoet.CodeBlock;

/**
 * Strategy for boundary timer events.
 */
public class BoundaryJobStrategy extends JobStrategy {

  public BoundaryJobStrategy(TestCaseActivity activity) {
    super(activity);
  }

  @Override
  protected CodeBlock buildHandlerMethodJavadoc() {
    if (!activity.hasPrevious()) {
      return super.buildHandlerMethodJavadoc();
    }

    TestCaseActivity previous = activity.getPrevious();
    if (!activity.isAttachedTo(previous)) {
      return super.buildHandlerMethodJavadoc();
    }

    CodeBlock.Builder builder = CodeBlock.builder();

    Object[] args = {activity.getTypeName(), activity.getId(), previous.getTypeName(), previous.getId()};
    builder.add("Returns the handler for $L: $L attached to $L: $L", args);

    String literal = Generator.toLiteral(previous.getId());
    builder.add("\n\n@see #$L", buildHandlerMethodName(literal));

    return builder.build();
  }
}
