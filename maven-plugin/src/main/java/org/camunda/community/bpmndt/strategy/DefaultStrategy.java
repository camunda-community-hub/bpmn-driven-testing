package org.camunda.community.bpmndt.strategy;

import java.lang.reflect.Type;

import javax.lang.model.element.Modifier;

import org.apache.commons.lang3.StringUtils;
import org.camunda.community.bpmndt.GeneratorStrategy;
import org.camunda.community.bpmndt.TestCaseActivity;
import org.camunda.community.bpmndt.TestCaseActivityType;
import org.camunda.community.bpmndt.api.JobHandler;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

/**
 * Default strategy, used for all unhandled activities (with type
 * {@link TestCaseActivityType#OTHER}).
 */
public class DefaultStrategy implements GeneratorStrategy {

  protected TestCaseActivity activity;

  @Override
  public void addHandlerField(TypeSpec.Builder classBuilder) {
    // nothing to add
  }

  @Override
  public void addHandlerFieldAfter(TypeSpec.Builder classBuilder) {
    classBuilder.addField(JobHandler.class, getLiteralAfter(), Modifier.PRIVATE);
  }

  @Override
  public void addHandlerFieldBefore(TypeSpec.Builder classBuilder) {
    classBuilder.addField(JobHandler.class, getLiteralBefore(), Modifier.PRIVATE);
  }

  @Override
  public void addHandlerMethod(TypeSpec.Builder classBuilder) {
    // nothing to add
  }

  @Override
  public void addHandlerMethodAfter(TypeSpec.Builder classBuilder) {
    MethodSpec method = MethodSpec.methodBuilder(buildHandlerMethodName(getLiteralAfter()))
        .addJavadoc(buildHandlerMethodJavadocAfter())
        .addModifiers(Modifier.PUBLIC)
        .returns(JobHandler.class)
        .addStatement("return $L", getLiteralAfter())
        .build();

    classBuilder.addMethod(method);
  }

  @Override
  public void addHandlerMethodBefore(TypeSpec.Builder classBuilder) {
    MethodSpec method = MethodSpec.methodBuilder(buildHandlerMethodName(getLiteralBefore()))
        .addJavadoc(buildHandlerMethodJavadocBefore())
        .addModifiers(Modifier.PUBLIC)
        .returns(JobHandler.class)
        .addStatement("return $L", getLiteralBefore())
        .build();

    classBuilder.addMethod(method);
  }

  @Override
  public void applyHandler(MethodSpec.Builder methodBuilder) {
    // nothing to apply
  }

  @Override
  public void applyHandlerAfter(MethodSpec.Builder methodBuilder) {
    methodBuilder.addStatement("assertThat(pi).isWaitingAt($S)", activity.getId());
    methodBuilder.addStatement("instance.apply($L)", getLiteralAfter());
  }

  @Override
  public void applyHandlerBefore(MethodSpec.Builder methodBuilder) {
    methodBuilder.addStatement("assertThat(pi).isWaitingAt($S)", activity.getId());
    methodBuilder.addStatement("instance.apply($L)", getLiteralBefore());
  }

  protected String buildHandlerMethodJavadocAfter() {
    return String.format("Returns the async after handler for %s: %s", activity.getTypeName(), activity.getId());
  }

  protected String buildHandlerMethodJavadocBefore() {
    return String.format("Returns the async before handler for %s: %s", activity.getTypeName(), activity.getId());
  }

  protected String buildHandlerMethodName(String literal) {
    return String.format("handle%s", StringUtils.capitalize(literal));
  }

  @Override
  public Type getHandlerType() {
    return null;
  }

  protected String getLiteralAfter() {
    return String.format("%sAfter", activity.getLiteral());
  }

  protected String getLiteralBefore() {
    return String.format("%sBefore", activity.getLiteral());
  }

  @Override
  public void initHandler(MethodSpec.Builder methodBuilder) {
    // nothing to initialize
  }

  @Override
  public void initHandlerAfter(MethodSpec.Builder methodBuilder) {
    methodBuilder.addCode("\n// $L: $L\n", activity.getTypeName(), activity.getId());
    methodBuilder.addStatement("$L = new $T(getProcessEngine(), $S)", getLiteralAfter(), JobHandler.class, activity.getId());
  }

  @Override
  public void initHandlerBefore(MethodSpec.Builder methodBuilder) {
    methodBuilder.addCode("\n// $L: $L\n", activity.getTypeName(), activity.getId());
    methodBuilder.addStatement("$L = new $T(getProcessEngine(), $S)", getLiteralBefore(), JobHandler.class, activity.getId());
  }

  public void setActivity(TestCaseActivity activity) {
    this.activity = activity;
  }
}
