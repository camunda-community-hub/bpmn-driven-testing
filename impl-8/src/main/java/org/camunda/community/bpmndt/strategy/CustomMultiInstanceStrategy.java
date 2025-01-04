package org.camunda.community.bpmndt.strategy;

import org.camunda.community.bpmndt.GeneratorStrategy;
import org.camunda.community.bpmndt.api.TestCaseInstanceElement.MultiInstanceElement;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

public class CustomMultiInstanceStrategy extends DefaultHandlerStrategy {

  public CustomMultiInstanceStrategy(GeneratorStrategy strategy) {
    super(strategy.getElement());
  }

  public void applyHandler(MethodSpec.Builder methodBuilder) {
    methodBuilder.addStatement("instance.apply(flowScopeKey, $L)", getHandler());
  }

  @Override
  public TypeName getHandlerType() {
    return CUSTOM_MULTI_INSTANCE;
  }

  @Override
  public void hasPassed(MethodSpec.Builder methodBuilder) {
    if (element.hasNext() && element.getNext().getType().isBoundaryEvent()) {
      methodBuilder.addStatement("instance.hasTerminatedMultiInstance(flowScopeKey, $S)", element.getId());
    } else {
      methodBuilder.addStatement("instance.hasPassedMultiInstance(flowScopeKey, $S)", element.getId());
    }
  }

  @Override
  public void initHandlerElement(MethodSpec.Builder methodBuilder) {
    methodBuilder.addCode("\n// $L: $L\n", element.getTypeName(), element.getId());
    methodBuilder.addStatement("$T $LElement = new $T()", MultiInstanceElement.class, literal, MultiInstanceElement.class);
    methodBuilder.addStatement("$LElement.id = $S", literal, element.getId());

    methodBuilder.addStatement("$LElement.sequential = $L", literal, element.isMultiInstanceSequential());
  }
}
