package org.camunda.community.bpmndt.cmd;

import java.util.function.Consumer;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.community.bpmndt.GeneratorResult;
import org.camunda.community.bpmndt.GeneratorStrategy;
import org.camunda.community.bpmndt.TestCaseContext;
import org.camunda.community.bpmndt.api.AbstractTestCase;
import org.camunda.community.bpmndt.api.MultiInstanceHandler;
import org.camunda.community.bpmndt.model.TestCaseActivity;
import org.camunda.community.bpmndt.model.TestCaseActivityType;
import org.camunda.community.bpmndt.strategy.MultiInstanceStrategy;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

/**
 * Generates the handler for a given multi instance activity.
 *
 * @see MultiInstanceHandler
 */
public class GenerateMultiInstanceHandler implements Consumer<TestCaseActivity> {

  private final TestCaseContext ctx;
  private final GeneratorResult result;

  public GenerateMultiInstanceHandler(TestCaseContext ctx, GeneratorResult result) {
    this.ctx = ctx;
    this.result = result;
  }

  @Override
  public void accept(TestCaseActivity activity) {
    ClassName className = (ClassName) ctx.getStrategy(activity.getId()).getHandlerType();

    // e.g. MyUserTaskHandler extends MultiInstanceHandler<MyUserTaskHandler, UserTaskHandler>
    TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
        .addJavadoc("Multi instance handler for $L: $L", activity.getTypeName(), activity.getId())
        .superclass(getSuperClass(activity))
        .addModifiers(Modifier.PUBLIC);

    if (hasSupportedBoundaryEventAttached(activity)) {
      classBuilder.addField(ctx.getStrategy(activity.getNext().getId()).getHandlerType(), "boundaryEventHandler", Modifier.PRIVATE);
    }

    classBuilder.addMethod(buildConstructor(activity));

    if (activity.getType() != TestCaseActivityType.OTHER) {
      classBuilder.addMethod(buildApply(activity));
      classBuilder.addMethod(buildCreateHandler(activity));
    }

    if (hasSupportedBoundaryEventAttached(activity)) {
      classBuilder.addMethod(buildHandleBoundaryEvent(activity));
    }

    if (activity.isMultiInstanceParallel()) {
      // override to return false, because it is parallel
      classBuilder.addMethod(buildIsSequential());
    }

    JavaFile javaFile = JavaFile.builder(className.packageName(), classBuilder.build())
        .skipJavaLangImports(true)
        .build();

    result.addFile(javaFile);
  }

  protected MethodSpec buildApply(TestCaseActivity activity) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("apply")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .returns(TypeName.BOOLEAN)
        .addParameter(ProcessInstance.class, "pi")
        .addParameter(TypeName.INT, "loopIndex");

    builder.addStatement("$T handler = getHandler(loopIndex)", getEnclosedStrategy(activity).getHandlerType());

    if (activity.getType() == TestCaseActivityType.CALL_ACTIVITY) {
      builder.addStatement("registerCallActivityHandler(handler)");
    }

    builder.addCode("\n");
    builder.addStatement("instance.apply(getHandlerBefore(loopIndex))");

    if (hasSupportedBoundaryEventAttached(activity)) {
      TestCaseActivity next = activity.getNext();

      builder.addCode("\n");
      builder.beginControlFlow("if (handler.isWaitingForBoundaryEvent())");
      builder.addCode("// $L: $L\n", next.getTypeName(), next.getId());
      builder.addStatement("instance.apply(boundaryEventHandler)");

      builder.addCode("\n");
      builder.addStatement("return false");
      builder.endControlFlow();
      builder.addCode("\n");
    }

    builder.addStatement("instance.apply(handler)");

    builder.addStatement("instance.apply(getHandlerAfter(loopIndex))");
    builder.addCode("\n");
    builder.addStatement("return true");

    return builder.build();
  }

  protected MethodSpec buildConstructor(TestCaseActivity activity) {
    MethodSpec.Builder builder = MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterizedTypeName.get(ClassName.get(AbstractTestCase.class), WildcardTypeName.subtypeOf(Object.class)), "testCase")
        .addParameter(String.class, "activityId")
        .addStatement("super(testCase, activityId)");

    if (hasSupportedBoundaryEventAttached(activity)) {
      TestCaseActivity next = activity.getNext();

      builder.addCode("\n// $L: $L\n", next.getTypeName(), next.getId());
      builder.addStatement("boundaryEventHandler = $L", ctx.getStrategy(next.getId()).initHandlerStatement(false));
    }

    return builder.build();
  }

  protected MethodSpec buildCreateHandler(TestCaseActivity activity) {
    GeneratorStrategy strategy = getEnclosedStrategy(activity);

    return MethodSpec.methodBuilder("createHandler")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .returns(strategy.getHandlerType())
        .addParameter(TypeName.INT, "loopIndex")
        .addStatement("return $L", strategy.initHandlerStatement(false))
        .build();
  }

  protected MethodSpec buildHandleBoundaryEvent(TestCaseActivity activity) {
    TestCaseActivity next = activity.getNext();

    return MethodSpec.methodBuilder("handleBoundaryEvent")
        .addJavadoc("Returns the handler for $L: $L", next.getTypeName(), next.getId())
        .addModifiers(Modifier.PUBLIC)
        .returns(ctx.getStrategy(next.getId()).getHandlerType())
        .addStatement("return boundaryEventHandler")
        .build();
  }

  protected MethodSpec buildIsSequential() {
    return MethodSpec.methodBuilder("isSequential")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .returns(TypeName.BOOLEAN)
        .addStatement("return false")
        .build();
  }

  private GeneratorStrategy getEnclosedStrategy(TestCaseActivity activity) {
    return ((MultiInstanceStrategy) ctx.getStrategy(activity.getId())).getEnclosedStrategy();
  }

  protected TypeName getSuperClass(TestCaseActivity activity) {
    TypeName multiInstanceType = ctx.getStrategy(activity.getId()).getHandlerType();
    TypeName enclosedType = getEnclosedStrategy(activity).getHandlerType();

    // e.g. MultiInstanceHandler<MyUserTaskHandler, UserTaskHandler>
    return ParameterizedTypeName.get(ClassName.get(MultiInstanceHandler.class), multiInstanceType, enclosedType);
  }

  protected boolean hasSupportedBoundaryEventAttached(TestCaseActivity activity) {
    if (!activity.hasNext() || !activity.getNext().getType().isBoundaryEvent()) {
      return false;
    }

    if (!activity.getType().isWaitState() && activity.getType() != TestCaseActivityType.CALL_ACTIVITY) {
      // not external task, user task or call activity
      return false;
    }

    switch (activity.getNext().getType()) {
      case CONDITIONAL_BOUNDARY:
      case MESSAGE_BOUNDARY:
      case SIGNAL_BOUNDARY:
      case TIMER_BOUNDARY:
        return true;
      default:
        return false;
    }
  }
}
