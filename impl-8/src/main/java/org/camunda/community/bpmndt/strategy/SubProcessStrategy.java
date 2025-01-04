package org.camunda.community.bpmndt.strategy;

import org.apache.commons.lang3.StringUtils;
import org.camunda.community.bpmndt.GeneratorStrategy;
import org.camunda.community.bpmndt.Literal;
import org.camunda.community.bpmndt.model.BpmnElement;
import org.camunda.community.bpmndt.model.BpmnElementScope;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec.Builder;

public class SubProcessStrategy implements GeneratorStrategy {

  private final BpmnElementScope scope;
  private final String literal;

  public SubProcessStrategy(BpmnElementScope scope) {
    this.scope = scope;

    literal = Literal.toLiteral(scope.getId());
  }

  @Override
  public void addHandlerField(Builder classBuilder) {
    // nothing to do here
  }

  @Override
  public void addHandlerMethod(Builder classBuilder) {
    // nothing to do here
  }

  @Override
  public void applyHandler(MethodSpec.Builder methodBuilder) {
    methodBuilder.addStatement("execute$L(instance, flowScopeKey)", StringUtils.capitalize(literal));
  }

  @Override
  public BpmnElement getElement() {
    return scope;
  }

  @Override
  public CodeBlock getHandler() {
    throw new UnsupportedOperationException();
  }

  @Override
  public TypeName getHandlerType() {
    return DefaultStrategy.OTHER;
  }

  @Override
  public String getLiteral() {
    return literal;
  }

  @Override
  public void hasPassed(MethodSpec.Builder methodBuilder) {
    // nothing to do here
  }

  @Override
  public void initHandler(MethodSpec.Builder methodBuilder) {
    // nothing to do here
  }

  @Override
  public void initHandlerElement(MethodSpec.Builder methodBuilder) {
    // nothing to do here
  }

  @Override
  public CodeBlock initHandlerStatement() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void isWaitingAt(MethodSpec.Builder methodBuilder) {
    // nothing to do here
  }
}
