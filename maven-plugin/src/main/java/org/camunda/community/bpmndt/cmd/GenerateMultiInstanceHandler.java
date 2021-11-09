package org.camunda.community.bpmndt.cmd;

import java.util.function.Consumer;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.community.bpmndt.GeneratorContext;
import org.camunda.community.bpmndt.GeneratorResult;
import org.camunda.community.bpmndt.GeneratorStrategy;
import org.camunda.community.bpmndt.TestCaseActivity;
import org.camunda.community.bpmndt.TestCaseActivityType;
import org.camunda.community.bpmndt.TestCaseContext;
import org.camunda.community.bpmndt.api.MultiInstanceHandler;
import org.camunda.community.bpmndt.api.TestCaseInstance;
import org.camunda.community.bpmndt.strategy.MultiInstanceStrategy;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class GenerateMultiInstanceHandler implements Consumer<TestCaseActivity> {

  private final GeneratorContext gCtx;
  private final GeneratorResult result;

  private final TestCaseContext ctx;

  public GenerateMultiInstanceHandler(GeneratorContext gCtx, GeneratorResult result, TestCaseContext ctx) {
    this.gCtx = gCtx;
    this.result = result;
    this.ctx = ctx;
  }

  @Override
  public void accept(TestCaseActivity activity) {
    ClassName className = (ClassName) activity.getStrategy().getHandlerType();

    TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
        .addJavadoc("Multi instance handler for $L: $L", activity.getTypeName(), activity.getId())
        .superclass(getSuperClass(activity))
        .addModifiers(Modifier.PUBLIC);

    if (hasSupportedBoundaryEventAttached(activity)) {
      classBuilder.addField(activity.getNext().getStrategy().getHandlerType(), "boundaryEventHandler", Modifier.PRIVATE);
    }

    classBuilder.addMethod(build(activity));

    if (activity.getType() != TestCaseActivityType.OTHER) {
      classBuilder.addMethod(buildApply(activity));
      classBuilder.addMethod(buildCreateHandler(activity));
    }

    if (hasSupportedBoundaryEventAttached(activity)) {
      classBuilder.addMethod(buildHandleBoundaryEvent(activity));
    }

    if (!activity.getMultiInstance().isSequential()) {
      // override to return false, because it is parallel
      classBuilder.addMethod(buildIsSequential());
    }

    String packageName = String.format("%s.%s", gCtx.getPackageName(), ctx.getPackageName());

    JavaFile javaFile = JavaFile.builder(packageName, classBuilder.build())
        .skipJavaLangImports(true)
        .build();

    result.addFile(javaFile);
  }

  protected MethodSpec build(TestCaseActivity activity) {
    MethodSpec.Builder builder = MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addParameter(TestCaseInstance.class, "instance")
        .addParameter(String.class, "activityId")
        .addStatement("super(instance, activityId)");

    if (hasSupportedBoundaryEventAttached(activity)) {
      TestCaseActivity next = activity.getNext();

      builder.addCode("\n// $L: $L\n", next.getTypeName(), next.getId());
      builder.addStatement("boundaryEventHandler = $L", next.getStrategy().initHandlerStatement());
    }

    return builder.build();
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

    if (activity.getType() != TestCaseActivityType.CALL_ACTIVITY) {
      builder.addStatement("instance.apply(handler)");
    }

    builder.addStatement("instance.apply(getHandlerAfter(loopIndex))");
    builder.addCode("\n");
    builder.addStatement("return true");

    return builder.build();
  }

  protected MethodSpec buildCreateHandler(TestCaseActivity activity) {
    GeneratorStrategy strategy = getEnclosedStrategy(activity);

    return MethodSpec.methodBuilder("createHandler")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .returns(strategy.getHandlerType())
        .addParameter(TypeName.INT, "loopIndex")
        .addStatement("return $L", strategy.initHandlerStatement())
        .build();
  }

  protected MethodSpec buildHandleBoundaryEvent(TestCaseActivity activity) {
    TestCaseActivity next = activity.getNext();

    return MethodSpec.methodBuilder("handleBoundaryEvent")
        .addJavadoc("Returns the handler for $L: $L", next.getTypeName(), next.getId())
        .addModifiers(Modifier.PUBLIC)
        .returns(next.getStrategy().getHandlerType())
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
    return ((MultiInstanceStrategy) activity.getStrategy()).getEnclosedStrategy();
  }

  protected TypeName getSuperClass(TestCaseActivity activity) {
    TypeName multiInstanceType = activity.getStrategy().getHandlerType();
    TypeName enclosedType = getEnclosedStrategy(activity).getHandlerType();

    return ParameterizedTypeName.get(ClassName.get(MultiInstanceHandler.class), multiInstanceType, enclosedType);
  }

  protected boolean hasSupportedBoundaryEventAttached(TestCaseActivity activity) {
    if (activity.getNext() == null || !activity.getNext().getType().isBoundaryEvent()) {
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
