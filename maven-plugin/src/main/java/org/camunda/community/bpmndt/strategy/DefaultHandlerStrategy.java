package org.camunda.community.bpmndt.strategy;

import javax.lang.model.element.Modifier;

import org.camunda.community.bpmndt.TestCaseActivity;
import org.camunda.community.bpmndt.TestCaseActivityType;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

/**
 * Default strategy for all handled activities, that provide a fluent handler API.
 */
public class DefaultHandlerStrategy extends DefaultStrategy {

  @Override
  public void addHandlerField(TypeSpec.Builder classBuilder) {
    classBuilder.addField(getHandlerType(), activity.getLiteral(), Modifier.PRIVATE);
  }

  @Override
  public void addHandlerMethod(TypeSpec.Builder classBuilder) {
    MethodSpec method = MethodSpec.methodBuilder(buildHandlerMethodName(activity.getLiteral()))
        .addJavadoc(buildHandlerMethodJavadoc())
        .addModifiers(Modifier.PUBLIC)
        .returns(getHandlerType())
        .addStatement("return $L", activity.getLiteral())
        .build();

    classBuilder.addMethod(method);
  }

  protected CodeBlock buildHandlerMethodJavadoc() {
    return CodeBlock.builder().add("Returns the handler for $L: $L", activity.getTypeName(), activity.getId()).build();
  }

  @Override
  public void applyHandler(MethodSpec.Builder methodBuilder) {
    if (activity.hasPrev() && activity.getPrev().getType() == TestCaseActivityType.EVENT_BASED_GATEWAY) {
      // if an event or job is part of an event based gateway
      // the process instance is waiting at the gateway and not at the event or job
      methodBuilder.addStatement("instance.apply($L)", activity.getLiteral());
    } else if (activity.getType().isWaitState()) {
      methodBuilder.addStatement("assertThat(pi).isWaitingAt($S)", activity.getId());
      methodBuilder.addStatement("instance.apply($L)", activity.getLiteral());
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
        methodBuilder.addStatement("instance.apply($L)", next.getLiteral());
        break;
      default:
        break;
    }
  }

  @Override
  public void initHandler(MethodSpec.Builder methodBuilder) {
    methodBuilder.addCode("\n// $L: $L\n", activity.getTypeName(), activity.getId());
    methodBuilder.addCode("$L = ", activity.getLiteral());
    methodBuilder.addStatement(initHandlerStatement());
  }

  @Override
  public CodeBlock initHandlerStatement() {
    return CodeBlock.of("new $T(getProcessEngine(), $S)", getHandlerType(), activity.getId());
  }
}
