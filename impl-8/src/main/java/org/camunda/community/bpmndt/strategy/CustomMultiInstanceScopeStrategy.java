package org.camunda.community.bpmndt.strategy;

import javax.lang.model.element.Modifier;

import org.apache.commons.lang3.StringUtils;
import org.camunda.community.bpmndt.GeneratorStrategy;
import org.camunda.community.bpmndt.Literal;
import org.camunda.community.bpmndt.api.TestCaseInstanceElement.MultiInstanceElement;
import org.camunda.community.bpmndt.model.BpmnElement;
import org.camunda.community.bpmndt.model.BpmnElementScope;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec.Builder;

public class CustomMultiInstanceScopeStrategy implements GeneratorStrategy {

  private final BpmnElementScope scope;
  private final String literal;

  public CustomMultiInstanceScopeStrategy(BpmnElementScope scope) {
    this.scope = scope;

    literal = Literal.toLiteral(scope.getId());
  }

  @Override
  public void addHandlerField(Builder classBuilder) {
    classBuilder.addField(getHandlerType(), literal, Modifier.PRIVATE);
  }

  @Override
  public void addHandlerMethod(Builder classBuilder) {
    var name = String.format("handle%s", StringUtils.capitalize(literal));
    var javadoc = CodeBlock.builder().add("Returns the handler for $L: $L", scope.getTypeName(), scope.getId()).build();

    var method = MethodSpec.methodBuilder(name)
        .addJavadoc(javadoc)
        .addModifiers(Modifier.PUBLIC)
        .returns(getHandlerType())
        .addStatement("return $L", literal)
        .build();

    classBuilder.addMethod(method);
  }

  @Override
  public void applyHandler(MethodSpec.Builder methodBuilder) {
    methodBuilder.addStatement("instance.apply(flowScopeKey, $L)", getHandler());
  }

  @Override
  public BpmnElement getElement() {
    return scope;
  }

  @Override
  public CodeBlock getHandler() {
    return CodeBlock.of(literal);
  }

  @Override
  public TypeName getHandlerType() {
    return DefaultStrategy.CUSTOM_MULTI_INSTANCE;
  }

  @Override
  public String getLiteral() {
    return literal;
  }

  @Override
  public void hasPassed(MethodSpec.Builder methodBuilder) {
    if (scope.getElements().isEmpty()) {
      return;
    }

    var element = scope.getElements().get(scope.getElements().size() - 1);
    if (element.hasNext() && element.getNext().getType().isBoundaryEvent()) {
      methodBuilder.addStatement("instance.hasTerminatedMultiInstance(flowScopeKey, $S)", scope.getId());
    } else {
      methodBuilder.addStatement("instance.hasPassedMultiInstance(flowScopeKey, $S)", scope.getId());
    }
  }

  @Override
  public void initHandler(MethodSpec.Builder methodBuilder) {
    methodBuilder.addCode("\n// $L: $L\n", scope.getTypeName(), scope.getId());
    methodBuilder.addCode("$L = ", literal);
    methodBuilder.addStatement(initHandlerStatement());
  }

  @Override
  public void initHandlerElement(MethodSpec.Builder methodBuilder) {
    methodBuilder.addCode("\n// $L: $L\n", scope.getTypeName(), scope.getId());
    methodBuilder.addStatement("$T $LElement = new $T()", MultiInstanceElement.class, literal, MultiInstanceElement.class);
    methodBuilder.addStatement("$LElement.id = $S", literal, scope.getId());

    methodBuilder.addStatement("$LElement.sequential = $L", literal, scope.isMultiInstanceSequential());
  }

  @Override
  public CodeBlock initHandlerStatement() {
    return CodeBlock.of("new $T($LElement)", getHandlerType(), scope.getId());
  }

  @Override
  public void isWaitingAt(MethodSpec.Builder methodBuilder) {
    // nothing to do here
  }
}
