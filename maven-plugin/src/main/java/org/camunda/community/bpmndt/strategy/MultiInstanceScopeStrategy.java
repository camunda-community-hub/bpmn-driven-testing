package org.camunda.community.bpmndt.strategy;

import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.community.bpmndt.TestCaseActivity;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.TypeName;

/**
 * Special strategy for multi instance scopes.
 */
public class MultiInstanceScopeStrategy extends DefaultHandlerStrategy {

  private final ClassName typeName;

  private String scopeId;

  public MultiInstanceScopeStrategy(ClassName typeName) {
    this.typeName = typeName;
  }

  @Override
  public void applyHandler(Builder methodBuilder) {
    methodBuilder.addStatement("instance.apply($L)", getHandler());
  }

  @Override
  protected CodeBlock buildHandlerMethodJavadoc() {
    return CodeBlock.builder().add("Returns the handler for multi instance $L: $L", activity.getTypeName(), activity.getId()).build();
  }

  @Override
  public TypeName getHandlerType() {
    return typeName;
  }

  @Override
  public void hasPassed(Builder methodBuilder) {
    methodBuilder.addStatement("assertThat(pi).hasPassed($S)", scopeId);
  }

  @Override
  public CodeBlock initHandlerStatement() {
    return CodeBlock.of("new $T(instance, $S)", getHandlerType(), activity.getId());
  }

  @Override
  public void isWaitingAt(Builder methodBuilder) {
    methodBuilder.addStatement("assertThat(pi).isWaitingAt($S)", scopeId);
  }

  @Override
  public void setActivity(TestCaseActivity activity) {
    super.setActivity(activity);

    scopeId = String.format("%s#%s", activity.getId(), ActivityTypes.MULTI_INSTANCE_BODY);
  }
}
