package org.camunda.community.bpmndt.platform8.strategy;

import org.camunda.community.bpmndt.Literal;
import org.camunda.community.bpmndt.model.platform8.BpmnElement;
import org.camunda.community.bpmndt.platform8.GeneratorStrategy;
import org.camunda.community.bpmndt.platform8.api.JobHandler;
import org.camunda.community.bpmndt.platform8.api.UserTaskHandler;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec.Builder;

public class DefaultStrategy implements GeneratorStrategy {

  public static TypeName JOB = TypeName.get(JobHandler.class);
  public static TypeName OTHER = TypeName.get(Void.class);
  public static TypeName USER_TASK = TypeName.get(UserTaskHandler.class);

  protected final BpmnElement element;
  protected final String literal;

  public DefaultStrategy(BpmnElement element) {
    this.element = element;

    literal = Literal.toLiteral(element.getId());
  }

  @Override
  public void addHandlerField(Builder classBuilder) {
    // nothing to add
  }

  @Override
  public void addHandlerMethod(Builder classBuilder) {
    // nothing to add
  }

  @Override
  public void applyHandler(MethodSpec.Builder methodBuilder) {
    // nothing to apply
  }

  @Override
  public BpmnElement getElement() {
    return element;
  }

  @Override
  public CodeBlock getHandler() {
    // nothing to return
    return null;
  }

  @Override
  public TypeName getHandlerType() {
    return OTHER;
  }

  @Override
  public String getLiteral() {
    return literal;
  }

  @Override
  public void hasPassed(MethodSpec.Builder methodBuilder) {
    methodBuilder.addStatement("instance.hasPassed(processInstanceEvent, $S)", element.getId());
  }

  @Override
  public void initHandler(MethodSpec.Builder methodBuilder) {
    // nothing to initialize
  }

  @Override
  public void initHandlerElement(MethodSpec.Builder methodBuilder) {
    // nothing to initialize
  }

  @Override
  public CodeBlock initHandlerStatement() {
    // nothing to return
    return null;
  }

  @Override
  public void isWaitingAt(MethodSpec.Builder methodBuilder) {
    methodBuilder.addStatement("instance.isWaitingAt(processInstanceEvent, $S);", element.getId());
  }
}
