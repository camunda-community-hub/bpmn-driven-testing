package org.camunda.community.bpmndt.strategy;

import javax.lang.model.element.Modifier;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.community.bpmndt.GeneratorStrategy;
import org.camunda.community.bpmndt.TestCaseContext;
import org.camunda.community.bpmndt.api.JobHandler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

/**
 * Special strategy for activities that are enclosed by the multi instance scope.
 */
public class MultiInstanceStrategy extends DefaultStrategy {

  private final GeneratorStrategy strategy;
  private final ClassName className;
  private final String scopeId;

  public MultiInstanceStrategy(GeneratorStrategy strategy, TestCaseContext ctx) {
    super(strategy.getActivity());
    this.strategy = strategy;

    String simpleName = String.format("%sHandler", StringUtils.capitalize(literal));
    className = ClassName.get(ctx.getPackageName(), simpleName);

    scopeId = String.format("%s#%s", activity.getId(), ActivityTypes.MULTI_INSTANCE_BODY);
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

  @Override
  public void applyHandler(Builder methodBuilder) {
    methodBuilder.addStatement("instance.apply($L)", literal);
  }

  @Override
  public void applyHandlerAfter(MethodSpec.Builder methodBuilder) {
    methodBuilder.addStatement("assertThat(pi).isWaitingAt($S)", scopeId);
    methodBuilder.addStatement("instance.apply($L)", getLiteralAfter());
  }

  @Override
  public void applyHandlerBefore(MethodSpec.Builder methodBuilder) {
    methodBuilder.addStatement("assertThat(pi).isWaitingAt($S)", scopeId);
    methodBuilder.addStatement("instance.apply($L)", getLiteralBefore());
  }

  protected CodeBlock buildHandlerMethodJavadoc() {
    return CodeBlock.builder().add("Returns the handler for multi instance $L: $L", activity.getTypeName(), activity.getId()).build();
  }

  /**
   * Gets the strategy of the activity that is enclosed by the multi instance scope.
   *
   * @return The enclosed (original) strategy.
   */
  public GeneratorStrategy getEnclosedStrategy() {
    return strategy;
  }

  @Override
  public TypeName getHandlerType() {
    return className;
  }

  @Override
  public void hasPassed(Builder methodBuilder) {
    methodBuilder.addStatement("assertThat(pi).hasPassed($S)", scopeId);
  }

  @Override
  public void initHandler(MethodSpec.Builder methodBuilder) {
    methodBuilder.addCode("\n// $L: $L\n", activity.getTypeName(), activity.getId());
    methodBuilder.addCode("$L = ", literal);
    methodBuilder.addStatement(initHandlerStatement(true));
  }

  @Override
  public void initHandlerAfter(MethodSpec.Builder methodBuilder) {
    methodBuilder.addCode("\n// $L: $L\n", activity.getTypeName(), activity.getId());
    methodBuilder.addStatement("$L = new $T(getProcessEngine(), $S)", getLiteralAfter(), JobHandler.class, scopeId);
  }

  @Override
  public void initHandlerBefore(MethodSpec.Builder methodBuilder) {
    methodBuilder.addCode("\n// $L: $L\n", activity.getTypeName(), activity.getId());
    methodBuilder.addStatement("$L = new $T(getProcessEngine(), $S)", getLiteralBefore(), JobHandler.class, scopeId);
  }

  @Override
  public CodeBlock initHandlerStatement(boolean isTestCase) {
    if (isTestCase) {
      return CodeBlock.of("new $T(this, $S)", getHandlerType(), activity.getId());
    } else {
      return CodeBlock.of("new $T(testCase, $S)", getHandlerType(), activity.getId());
    }
  }

  @Override
  public void isWaitingAt(Builder methodBuilder) {
    methodBuilder.addStatement("assertThat(pi).isWaitingAt($S)", scopeId);
  }
}
