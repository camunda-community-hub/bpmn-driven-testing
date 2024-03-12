package org.camunda.community.bpmndt.platform8.strategy;

import javax.lang.model.element.Modifier;

import org.apache.commons.lang3.StringUtils;
import org.camunda.community.bpmndt.Literal;
import org.camunda.community.bpmndt.model.platform8.BpmnElement;
import org.camunda.community.bpmndt.model.platform8.BpmnElementType;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.TypeSpec;

/**
 * Default strategy for all handled BPMN element, that provide a fluent handler API.
 */
public class DefaultHandlerStrategy extends DefaultStrategy {

  public DefaultHandlerStrategy(BpmnElement element) {
    super(element);
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
    return CodeBlock.builder().add("Returns the handler for $L: $L", element.getTypeName(), element.getId()).build();
  }

  protected String buildHandlerMethodName(String literal) {
    return String.format("handle%s", StringUtils.capitalize(literal));
  }

  @Override
  public void applyHandler(MethodSpec.Builder methodBuilder) {
    if (element.hasPrevious(BpmnElementType.EVENT_BASED_GATEWAY)) {
      // if an event or job is part of an event based gateway
      // the process instance is waiting at the gateway and not at the event or job itself
      methodBuilder.addStatement("instance.apply(processInstanceEvent, $L)", getHandler());
    } else {
      methodBuilder.addStatement("instance.isWaitingAt(processInstanceEvent, $S)", element.getId());
      methodBuilder.addStatement("instance.apply($L)", getHandler());
    }

    if (!element.hasNext()) {
      return;
    }

    BpmnElement next = element.getNext();
    if (!next.getType().isBoundaryEvent()) {
      return;
    }

    switch (next.getType()) {
      case MESSAGE_BOUNDARY:
      case SIGNAL_BOUNDARY:
      case TIMER_BOUNDARY:
        methodBuilder.addStatement("instance.apply(processInstanceEvent, $L)", Literal.toLiteral(next.getId()));
    }
  }

  @Override
  public CodeBlock getHandler() {
    return CodeBlock.of(literal);
  }

  @Override
  public void hasPassed(Builder methodBuilder) {
    BpmnElement next = element.getNext();
    if (next.getType().isBoundaryEvent() && next.isAttachedTo(element)) {
      methodBuilder.addStatement("instance.hasTerminated(processInstanceEvent, $S)", element.getId());
    } else {
      super.hasPassed(methodBuilder);
    }
  }

  @Override
  public void initHandler(MethodSpec.Builder methodBuilder) {
    methodBuilder.addCode("\n// $L: $L\n", element.getTypeName(), element.getId());
    methodBuilder.addCode("$L = ", literal);
    methodBuilder.addStatement(initHandlerStatement());
  }

  @Override
  public CodeBlock initHandlerStatement() {
    return CodeBlock.of("new $T($LElement)", getHandlerType(), element.getId());
  }
}
