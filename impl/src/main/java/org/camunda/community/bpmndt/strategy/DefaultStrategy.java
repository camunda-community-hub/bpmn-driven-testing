package org.camunda.community.bpmndt.strategy;

import javax.lang.model.element.Modifier;

import org.apache.commons.lang3.StringUtils;
import org.camunda.community.bpmndt.Generator;
import org.camunda.community.bpmndt.GeneratorStrategy;
import org.camunda.community.bpmndt.api.JobHandler;
import org.camunda.community.bpmndt.api.cfg.BpmndtParseListener;
import org.camunda.community.bpmndt.model.TestCaseActivity;
import org.camunda.community.bpmndt.model.TestCaseActivityType;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

/**
 * Default strategy, used for all unhandled activities (with type {@link TestCaseActivityType#OTHER}).
 */
public class DefaultStrategy implements GeneratorStrategy {

  protected final TestCaseActivity activity;
  protected final String literal;

  /**
   * Indicates if the strategy is applied on a multi instance scope handler or not - see {@link #setMultiInstanceParent(boolean)}
   */
  protected boolean multiInstanceParent = false;

  public DefaultStrategy(TestCaseActivity activity) {
    this.activity = activity;

    literal = Generator.toLiteral(activity.getId());
  }

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
        .addJavadoc("Returns the async after handler for $L: $L", activity.getTypeName(), activity.getId())
        .addModifiers(Modifier.PUBLIC)
        .returns(JobHandler.class)
        .addStatement("return $L", getLiteralAfter())
        .build();

    classBuilder.addMethod(method);
  }

  @Override
  public void addHandlerMethodBefore(TypeSpec.Builder classBuilder) {
    MethodSpec method = MethodSpec.methodBuilder(buildHandlerMethodName(getLiteralBefore()))
        .addJavadoc("Returns the async before handler for $L: $L", activity.getTypeName(), activity.getId())
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
    methodBuilder.addStatement("instance.apply($L)", getHandlerAfter());
  }

  @Override
  public void applyHandlerBefore(MethodSpec.Builder methodBuilder) {
    methodBuilder.addStatement("assertThat(pi).isWaitingAt($S)", activity.getId());
    methodBuilder.addStatement("instance.apply($L)", getHandlerBefore());
  }

  protected String buildHandlerMethodName(String literal) {
    return String.format("handle%s", StringUtils.capitalize(literal));
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof GeneratorStrategy)) {
      return false;
    }

    GeneratorStrategy strategy = (GeneratorStrategy) obj;
    return strategy.getActivity().getId().equals(activity.getId());
  }

  @Override
  public TestCaseActivity getActivity() {
    return activity;
  }

  @Override
  public CodeBlock getHandler() {
    // nothing to return
    return null;
  }

  @Override
  public CodeBlock getHandlerAfter() {
    if (multiInstanceParent) {
      return CodeBlock.of("get$LHandlerAfter(loopIndex)", StringUtils.capitalize(literal));
    } else {
      return CodeBlock.of(getLiteralAfter());
    }
  }

  @Override
  public CodeBlock getHandlerBefore() {
    if (multiInstanceParent) {
      return CodeBlock.of("get$LHandlerBefore(loopIndex)", StringUtils.capitalize(literal));
    } else {
      return CodeBlock.of(getLiteralBefore());
    }
  }

  @Override
  public TypeName getHandlerType() {
    return OTHER;
  }

  @Override
  public String getLiteral() {
    return literal;
  }

  protected String getLiteralAfter() {
    return String.format("%sAfter", literal);
  }

  protected String getLiteralBefore() {
    return String.format("%sBefore", literal);
  }

  @Override
  public int hashCode() {
    return activity.getId().hashCode();
  }

  @Override
  public void hasPassed(Builder methodBuilder) {
    methodBuilder.addStatement("assertThat(pi).hasPassed($S)", activity.getId());
  }

  @Override
  public void initHandler(MethodSpec.Builder methodBuilder) {
    // nothing to initialize
  }

  @Override
  public void initHandlerAfter(MethodSpec.Builder methodBuilder) {
    methodBuilder.addCode("\n// $L: $L\n", activity.getTypeName(), activity.getId());
    methodBuilder.addCode("$L = ", getLiteralAfter());
    methodBuilder.addStatement(initHandlerAfterStatement());
  }

  @Override
  public CodeBlock initHandlerAfterStatement() {
    return CodeBlock.of("new $T(getProcessEngine(), $S)", JobHandler.class, activity.getId());
  }

  @Override
  public void initHandlerBefore(MethodSpec.Builder methodBuilder) {
    methodBuilder.addCode("\n// $L: $L\n", activity.getTypeName(), activity.getId());
    methodBuilder.addCode("$L = ", getLiteralBefore());
    methodBuilder.addStatement(initHandlerBeforeStatement());
  }

  @Override
  public CodeBlock initHandlerBeforeStatement() {
    return CodeBlock.of("new $T(getProcessEngine(), $S)", JobHandler.class, activity.getId());
  }

  @Override
  public CodeBlock initHandlerStatement() {
    return null;
  }

  @Override
  public void isWaitingAt(Builder methodBuilder) {
    methodBuilder.addStatement("assertThat(pi).isWaitingAt($S)", activity.getId());
  }

  @Override
  public void setMultiInstanceParent(boolean multiInstanceParent) {
    this.multiInstanceParent = multiInstanceParent;
  }

  /**
   * If the activity has the {@code asyncAfter} flag set, it must be handled. If it is the last activity, and it does not end the process, the asynchronous
   * continuation after should not be handled - the execution must wait!
   */
  @Override
  public boolean shouldHandleAfter() {
    return activity.isAsyncAfter() && (activity.hasNext() || activity.isProcessEnd());
  }

  /**
   * If the activity has the {@code asyncBefore} flag set, or it is a call activity but not a multi instance.
   *
   * @see BpmndtParseListener#parseCallActivity(org.camunda.bpm.engine.impl.util.xml.Element, org.camunda.bpm.engine.impl.pvm.process.ScopeImpl,
   * org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)
   */
  @Override
  public boolean shouldHandleBefore() {
    return activity.isAsyncBefore() || (activity.getType() == TestCaseActivityType.CALL_ACTIVITY && !activity.isMultiInstance());
  }
}
