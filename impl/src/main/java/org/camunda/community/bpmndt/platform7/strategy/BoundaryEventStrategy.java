package org.camunda.community.bpmndt.platform7.strategy;

import org.camunda.community.bpmndt.Literal;
import org.camunda.community.bpmndt.model.platform7.TestCaseActivity;

import com.squareup.javapoet.CodeBlock;

/**
 * Strategy for boundary conditional, message and signal events.
 */
public class BoundaryEventStrategy extends EventStrategy {

  public BoundaryEventStrategy(TestCaseActivity activity) {
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

    String literal = Literal.toLiteral(previous.getId());
    builder.add("\n\n@see #$L", buildHandlerMethodName(literal));

    return builder.build();
  }
}
