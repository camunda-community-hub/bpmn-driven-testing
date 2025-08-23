package org.camunda.community.bpmndt.strategy;

import javax.lang.model.element.Modifier;

import org.apache.commons.lang3.StringUtils;
import org.camunda.community.bpmndt.Generator;
import org.camunda.community.bpmndt.model.TestCaseActivity;
import org.camunda.community.bpmndt.model.TestCaseActivityType;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

/**
 * Default strategy for all handled activities, that provide a fluent handler API.
 */
public class DefaultHandlerStrategy extends DefaultStrategy {

  public DefaultHandlerStrategy(TestCaseActivity activity) {
    super(activity);
  }

  @Override
  public void addHandlerField(TypeSpec.Builder classBuilder) {
    classBuilder.addField(getHandlerType(), literal, Modifier.PRIVATE);
  }

  @Override
  public void addHandlerMethod(TypeSpec.Builder classBuilder) {
    MethodSpec method = MethodSpec.methodBuilder(buildHandlerMethodName(literal))
        .addJavadoc(buildHandlerMethodJavadoc())
        .addModifiers(Modifier.PUBLIC)
        .returns(getHandlerType())
        .addStatement("return $L", literal)
        .build();

    classBuilder.addMethod(method);
  }

  protected CodeBlock buildHandlerMethodJavadoc() {
    return CodeBlock.builder().add("Returns the handler for $L: $L", activity.getTypeName(), activity.getId()).build();
  }

  @Override
  public void applyHandler(MethodSpec.Builder methodBuilder) {
    if (activity.hasPrevious(TestCaseActivityType.EVENT_BASED_GATEWAY)) {
      // if an event or job is part of an event based gateway
      // the process instance is waiting at the gateway and not at the event or job itself
      methodBuilder.addStatement("instance.apply($L)", getHandler());
    } else if (activity.getType().isWaitState()) {
      methodBuilder.addStatement("assertThat(pi).isWaitingAt($S)", activity.getId());
      methodBuilder.addStatement("instance.apply($L)", getHandler());
    } else if (activity.getType() == TestCaseActivityType.CALL_ACTIVITY) {
      methodBuilder.addStatement("instance.apply($L)", getHandler());
    }

    if (!activity.hasNext()) {
      return;
    }

    TestCaseActivity next = activity.getNext();
    if (!next.getType().isBoundaryEvent()) {
      return;
    }

    switch (next.getType()) {
      case CONDITIONAL_BOUNDARY:
      case MESSAGE_BOUNDARY:
      case SIGNAL_BOUNDARY:
      case TIMER_BOUNDARY:
        methodBuilder.addStatement("instance.apply($L)", Generator.toLiteral(next.getId()));
        break;
      default:
        break;
    }
  }

  @Override
  public CodeBlock getHandler() {
    if (multiInstanceParent) {
      return CodeBlock.of("get$LHandler(loopIndex)", StringUtils.capitalize(literal));
    } else {
      return CodeBlock.of(literal);
    }
  }

  @Override
  public void initHandler(MethodSpec.Builder methodBuilder) {
    methodBuilder.addCode("\n// $L: $L\n", activity.getTypeName(), activity.getId());
    methodBuilder.addCode("$L = ", literal);
    methodBuilder.addStatement(initHandlerStatement(true));
  }

  @Override
  public CodeBlock initHandlerStatement(boolean isTestCase) {
    return CodeBlock.of("new $T(getProcessEngine(), $S)", getHandlerType(), activity.getId());
  }
}
