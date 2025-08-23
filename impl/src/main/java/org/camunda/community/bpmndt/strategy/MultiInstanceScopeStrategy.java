package org.camunda.community.bpmndt.strategy;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.community.bpmndt.TestCaseContext;
import org.camunda.community.bpmndt.model.TestCaseActivity;
import org.camunda.community.bpmndt.model.TestCaseActivityScope;
import org.camunda.community.bpmndt.model.TestCaseActivityType;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.TypeName;

/**
 * Special strategy for multi instance scopes.
 */
public class MultiInstanceScopeStrategy extends DefaultHandlerStrategy {

  private final ClassName className;
  private final String scopeId;

  public MultiInstanceScopeStrategy(TestCaseActivityScope scope, TestCaseContext ctx) {
    super(new TestCaseActivityWrapper(scope));

    String simpleName = String.format("%s__%sHandler", ctx.getClassName(), StringUtils.capitalize(literal));
    className = ClassName.get(ctx.getPackageName(), simpleName);

    scopeId = String.format("%s#%s", activity.getId(), ActivityTypes.MULTI_INSTANCE_BODY);
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
    return className;
  }

  @Override
  public void hasPassed(Builder methodBuilder) {
    methodBuilder.addStatement("assertThat(pi).hasPassed($S)", scopeId);
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

  /**
   * Wrapper class to use an {@link TestCaseActivityScope} as activity.
   */
  private static class TestCaseActivityWrapper implements TestCaseActivity {

    private final TestCaseActivityScope scope;

    private TestCaseActivityWrapper(TestCaseActivityScope scope) {
      this.scope = scope;
    }

    @Override
    public String getEventCode() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getEventName() {
      throw new UnsupportedOperationException();
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
    public TestCaseActivity getNext() {
      throw new UnsupportedOperationException();
    }

    @Override
    public TestCaseActivityScope getParent() {
      return scope.getParent();
    }

    @Override
    public TestCaseActivity getPrevious() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getTopicName() {
      throw new UnsupportedOperationException();
    }

    @Override
    public TestCaseActivityType getType() {
      return TestCaseActivityType.SCOPE;
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
    public boolean hasPrevious(TestCaseActivityType type) {
      return false;
    }

    @Override
    public boolean isAsyncAfter() {
      return false;
    }

    @Override
    public boolean isAsyncBefore() {
      return false;
    }

    @Override
    public boolean isAttachedTo(TestCaseActivity activity) {
      return false;
    }

    @Override
    public boolean isMultiInstance() {
      return scope.isMultiInstance();
    }

    @Override
    public boolean isMultiInstanceParallel() {
      return scope.isMultiInstanceParallel();
    }

    @Override
    public boolean isMultiInstanceSequential() {
      return scope.isMultiInstanceSequential();
    }

    @Override
    public boolean isProcessEnd() {
      return false;
    }

    @Override
    public boolean isProcessStart() {
      return false;
    }
  }
}
