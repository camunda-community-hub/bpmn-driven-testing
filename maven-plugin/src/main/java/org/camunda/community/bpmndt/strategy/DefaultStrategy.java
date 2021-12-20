package org.camunda.community.bpmndt.strategy;

import javax.lang.model.element.Modifier;

import org.apache.commons.lang3.StringUtils;
import org.camunda.community.bpmndt.GeneratorStrategy;
import org.camunda.community.bpmndt.TestCaseActivity;
import org.camunda.community.bpmndt.TestCaseActivityType;
import org.camunda.community.bpmndt.api.CallActivityHandler;
import org.camunda.community.bpmndt.api.EventHandler;
import org.camunda.community.bpmndt.api.ExternalTaskHandler;
import org.camunda.community.bpmndt.api.JobHandler;
import org.camunda.community.bpmndt.api.UserTaskHandler;
import org.camunda.community.bpmndt.api.cfg.BpmndtParseListener;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

/**
 * Default strategy, used for all unhandled activities (with type
 * {@link TestCaseActivityType#OTHER}).
 */
public class DefaultStrategy implements GeneratorStrategy {

  protected static TypeName CALL_ACTIVITY = TypeName.get(CallActivityHandler.class);
  protected static TypeName EVENT = TypeName.get(EventHandler.class);
  protected static TypeName EXTERNAL_TASK = TypeName.get(ExternalTaskHandler.class);
  protected static TypeName JOB = TypeName.get(JobHandler.class);
  protected static TypeName OTHER = TypeName.get(Void.class);
  protected static TypeName USER_TASK = TypeName.get(UserTaskHandler.class);

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
  public TypeName getHandlerType() {
    return OTHER;
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

  @Override
  public CodeBlock initHandlerStatement() {
    return null;
  }

  public void setActivity(TestCaseActivity activity) {
    this.activity = activity;
  }

  /**
   * If the activity has the {@code asyncAfter} flag set, it must be handled. If it is the last
   * activity and it does not end the process, the asynchronous continuation after should not be
   * handled - the execution must wait!
   */
  @Override
  public boolean shouldHandleAfter() {
    return activity.isAsyncAfter() && (activity.hasNext() || activity.isProcessEnd());
  }

  /**
   * If the activity has the {@code asyncBefore} flag set, or it is a call activity but not a multi
   * instance.
   * 
   * @see BpmndtParseListener#parseCallActivity(org.camunda.bpm.engine.impl.util.xml.Element,
   *      org.camunda.bpm.engine.impl.pvm.process.ScopeImpl,
   *      org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)
   */
  @Override
  public boolean shouldHandleBefore() {
    return activity.isAsyncBefore() || (activity.getType() == TestCaseActivityType.CALL_ACTIVITY && !activity.isMultiInstance());
  }
}
