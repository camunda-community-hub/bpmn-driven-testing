package org.camunda.community.bpmndt.strategy;

import javax.lang.model.element.Modifier;

import org.apache.commons.lang3.StringUtils;
import org.camunda.community.bpmndt.GeneratorStrategy;
import org.camunda.community.bpmndt.Literal;
import org.camunda.community.bpmndt.api.TestCaseInstanceElement.MultiInstanceElement;
import org.camunda.community.bpmndt.model.BpmnElement;
import org.camunda.community.bpmndt.model.BpmnElementScope;
import org.camunda.community.bpmndt.model.BpmnElementType;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec.Builder;

import io.camunda.zeebe.model.bpmn.instance.FlowNode;

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
    methodBuilder.addStatement("instance.apply(processInstanceKey, $L)", getHandler());
  }

  @Override
  public BpmnElement getElement() {
    return new BpmnElementWrapper(scope);
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
      methodBuilder.addStatement("instance.hasTerminatedMultiInstance(processInstanceKey, $S)", scope.getId());
    } else {
      methodBuilder.addStatement("instance.hasPassedMultiInstance(processInstanceKey, $S)", scope.getId());
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

  /**
   * Wrapper class to use an {@link BpmnElementScope} as BPMN element.
   */
  private static class BpmnElementWrapper implements BpmnElement {

    private final BpmnElementScope scope;

    private BpmnElementWrapper(BpmnElementScope scope) {
      this.scope = scope;
    }

    @Override
    public FlowNode getFlowNode() {
      return scope.getFlowNode();
    }

    @Override
    public <T extends FlowNode> T getFlowNode(Class<T> flowNodeType) {
      return flowNodeType.cast(scope.getFlowNode());
    }

    @Override
    public String getId() {
      return scope.getId();
    }

    @Override
    public String getName() {
      return scope.getName();
    }

    @Override
    public int getNestingLevel() {
      return scope.getNestingLevel();
    }

    @Override
    public BpmnElement getNext() {
      throw new UnsupportedOperationException();
    }

    @Override
    public BpmnElementScope getParent() {
      return scope.getParent();
    }

    @Override
    public BpmnElement getPrevious() {
      throw new UnsupportedOperationException();
    }

    @Override
    public BpmnElementType getType() {
      return BpmnElementType.SCOPE;
    }

    @Override
    public String getTypeName() {
      return scope.getTypeName();
    }

    @Override
    public boolean hasMultiInstanceParent() {
      return scope.hasParent() && scope.getParent().isMultiInstance();
    }

    @Override
    public boolean hasNext() {
      return false;
    }

    @Override
    public boolean hasParent() {
      return scope.hasParent();
    }

    @Override
    public boolean hasPrevious() {
      return false;
    }

    @Override
    public boolean hasPrevious(BpmnElementType type) {
      return false;
    }

    @Override
    public boolean isAttachedTo(BpmnElement element) {
      return false;
    }

    @Override
    public boolean isMultiInstance() {
      return scope.isMultiInstance();
    }

    @Override
    public boolean isMultiInstanceSequential() {
      return scope.isMultiInstanceSequential();
    }

    @Override
    public boolean isProcessStart() {
      return false;
    }
  }
}
