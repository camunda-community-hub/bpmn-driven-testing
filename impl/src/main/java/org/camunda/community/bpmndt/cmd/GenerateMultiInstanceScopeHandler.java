package org.camunda.community.bpmndt.cmd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.lang.model.element.Modifier;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.ThrowEvent;
import org.camunda.community.bpmndt.GeneratorResult;
import org.camunda.community.bpmndt.GeneratorStrategy;
import org.camunda.community.bpmndt.TestCaseContext;
import org.camunda.community.bpmndt.api.JobHandler;
import org.camunda.community.bpmndt.api.MultiInstanceScopeHandler;
import org.camunda.community.bpmndt.api.TestCaseInstance;
import org.camunda.community.bpmndt.model.BpmnEventSupport;
import org.camunda.community.bpmndt.model.TestCaseActivity;
import org.camunda.community.bpmndt.model.TestCaseActivityScope;
import org.camunda.community.bpmndt.model.TestCaseActivityType;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class GenerateMultiInstanceScopeHandler implements Consumer<TestCaseActivityScope> {

  private static final String SUFFIX_AFTER = "After";
  private static final String SUFFIX_BEFORE = "Before";

  private final TestCaseContext ctx;
  private final GeneratorResult result;

  public GenerateMultiInstanceScopeHandler(TestCaseContext ctx, GeneratorResult result) {
    this.ctx = ctx;
    this.result = result;
  }

  @Override
  public void accept(TestCaseActivityScope scope) {
    List<GeneratorStrategy> strategies = new GetStrategies().apply(ctx, scope.getActivities());
    for (GeneratorStrategy strategy : strategies) {
      strategy.setMultiInstanceParent(true);
    }

    ClassName className = (ClassName) ctx.getStrategy(scope.getId()).getHandlerType();

    // e.g. TC_HappyPath_MyScopeHandler extends MultiInstanceScopeHandler<TC_HappyPath_MyScopeHandler>
    TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
        .addJavadoc("Multi instance scope handler for $L: $L", scope.getTypeName(), scope.getId())
        .superclass(getSuperClass(scope))
        .addModifiers(Modifier.PUBLIC);

    addHandlerFields(strategies, classBuilder);

    classBuilder.addMethod(buildConstructor(strategies));
    classBuilder.addMethod(buildApply(scope, strategies));

    addHandlerMethods(strategies, classBuilder);

    if (scope.isMultiInstanceParallel()) {
      // override to return false, because it is parallel
      classBuilder.addMethod(buildIsSequential());
    }

    JavaFile javaFile = JavaFile.builder(className.packageName(), classBuilder.build())
        .addStaticImport(ProcessEngineTests.class, "assertThat")
        .skipJavaLangImports(true)
        .build();

    result.addFile(javaFile);
  }

  protected void addHandlerFields(List<GeneratorStrategy> strategies, TypeSpec.Builder classBuilder) {
    for (GeneratorStrategy strategy : strategies) {
      TestCaseActivity activity = strategy.getActivity();

      if (strategy.shouldHandleBefore()) {
        addHandlerField(classBuilder, String.format("%sHandlersBefore", strategy.getLiteral()), TypeName.get(JobHandler.class));
      }

      if (activity.getType() != TestCaseActivityType.OTHER) {
        addHandlerField(classBuilder, String.format("%sHandlers", strategy.getLiteral()), strategy.getHandlerType());
      }

      if (strategy.shouldHandleAfter()) {
        addHandlerField(classBuilder, String.format("%sHandlersAfter", strategy.getLiteral()), TypeName.get(JobHandler.class));
      }
    }
  }

  private void addHandlerField(TypeSpec.Builder classBuilder, String name, TypeName typeName) {
    classBuilder.addField(ParameterizedTypeName.get(ClassName.get(Map.class), TypeName.get(Integer.class), typeName), name, Modifier.PRIVATE, Modifier.FINAL);
  }

  protected void addHandlerMethods(List<GeneratorStrategy> strategies, TypeSpec.Builder classBuilder) {
    // createHandler
    for (GeneratorStrategy strategy : strategies) {
      TestCaseActivity activity = strategy.getActivity();

      if (strategy.shouldHandleBefore()) {
        classBuilder.addMethod(buildCreateHandler(activity, SUFFIX_BEFORE));
      }

      if (activity.getType() != TestCaseActivityType.OTHER) {
        classBuilder.addMethod(buildCreateHandler(activity, StringUtils.EMPTY));
      }

      if (strategy.shouldHandleAfter()) {
        classBuilder.addMethod(buildCreateHandler(activity, SUFFIX_AFTER));
      }
    }

    // getHandler
    for (GeneratorStrategy strategy : strategies) {
      TestCaseActivity activity = strategy.getActivity();

      if (strategy.shouldHandleBefore()) {
        classBuilder.addMethod(buildGetHandler(activity, SUFFIX_BEFORE));
      }

      if (activity.getType() != TestCaseActivityType.OTHER) {
        classBuilder.addMethod(buildGetHandler(activity, StringUtils.EMPTY));
      }

      if (strategy.shouldHandleAfter()) {
        classBuilder.addMethod(buildGetHandler(activity, SUFFIX_AFTER));
      }
    }

    // handle
    for (GeneratorStrategy strategy : strategies) {
      TestCaseActivity activity = strategy.getActivity();

      if (strategy.shouldHandleBefore()) {
        classBuilder.addMethod(buildHandleDefault(activity, SUFFIX_BEFORE));
        classBuilder.addMethod(buildHandle(activity, SUFFIX_BEFORE));
      }

      if (activity.getType() != TestCaseActivityType.OTHER) {
        classBuilder.addMethod(buildHandleDefault(activity, StringUtils.EMPTY));
        classBuilder.addMethod(buildHandle(activity, StringUtils.EMPTY));
      }

      if (strategy.shouldHandleAfter()) {
        classBuilder.addMethod(buildHandleDefault(activity, SUFFIX_AFTER));
        classBuilder.addMethod(buildHandle(activity, SUFFIX_AFTER));
      }
    }
  }

  protected MethodSpec buildApply(TestCaseActivityScope scope, List<GeneratorStrategy> strategies) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("apply")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .returns(TypeName.BOOLEAN)
        .addParameter(ProcessInstance.class, "pi")
        .addParameter(TypeName.INT, "loopIndex");

    for (GeneratorStrategy strategy : strategies) {
      TestCaseActivity activity = strategy.getActivity();
      if (activity.getType() == TestCaseActivityType.CALL_ACTIVITY) {
        builder.addCode("// $L: $L\n", activity.getTypeName(), activity.getId());
        builder.addStatement("registerCallActivityHandler($S, get$LHandler(loopIndex))", activity.getId(), StringUtils.capitalize(strategy.getLiteral()));
        builder.addCode("\n");
      }
    }

    new BuildTestCaseExecution(ctx).accept(strategies, builder);

    builder.addCode("\n");
    builder.addStatement("return $L", hasNoneEndEvent(scope));

    return builder.build();
  }

  protected MethodSpec buildCreateHandler(TestCaseActivity activity, String suffix) {
    GeneratorStrategy strategy = ctx.getStrategy(activity.getId());

    CodeBlock initHandlerStatement;
    if (suffix.isEmpty()) {
      initHandlerStatement = strategy.initHandlerStatement();
    } else if (SUFFIX_AFTER.equals(suffix)) {
      initHandlerStatement = strategy.initHandlerAfterStatement();
    } else {
      initHandlerStatement = strategy.initHandlerBeforeStatement();
    }

    return MethodSpec.methodBuilder(String.format("create%sHandler%s", StringUtils.capitalize(strategy.getLiteral()), suffix))
        .addModifiers(Modifier.PROTECTED)
        .returns(suffix.isEmpty() ? strategy.getHandlerType() : TypeName.get(JobHandler.class))
        .addParameter(TypeName.INT, "loopIndex")
        .addStatement("return $L", initHandlerStatement)
        .build();
  }

  protected MethodSpec buildConstructor(List<GeneratorStrategy> strategies) {
    MethodSpec.Builder builder = MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addParameter(TestCaseInstance.class, "instance")
        .addParameter(String.class, "activityId")
        .addStatement("super(instance, activityId)");

    for (GeneratorStrategy strategy : strategies) {
      TestCaseActivity activity = strategy.getActivity();

      if (strategy.shouldHandleBefore() || activity.getType() != TestCaseActivityType.OTHER || strategy.shouldHandleAfter()) {
        builder.addCode("\n// $L: $L\n", activity.getTypeName(), activity.getId());
      }

      if (strategy.shouldHandleBefore()) {
        builder.addStatement("$LHandlersBefore = new $T<>()", strategy.getLiteral(), HashMap.class);
      }

      if (activity.getType() != TestCaseActivityType.OTHER) {
        builder.addStatement("$LHandlers = new $T<>()", strategy.getLiteral(), HashMap.class);
      }

      if (strategy.shouldHandleAfter()) {
        builder.addStatement("$LHandlersAfter = new $T<>()", strategy.getLiteral(), HashMap.class);
      }
    }

    return builder.build();
  }

  protected MethodSpec buildGetHandler(TestCaseActivity activity, String suffix) {
    GeneratorStrategy strategy = ctx.getStrategy(activity.getId());

    String capitalizedLiteral = StringUtils.capitalize(strategy.getLiteral());

    String handleMethodName;
    if (suffix.isEmpty()) {
      handleMethodName = String.format("handle%s", capitalizedLiteral);
    } else {
      handleMethodName = String.format("handle%s%s", capitalizedLiteral, suffix);
    }

    return MethodSpec.methodBuilder(String.format("get%sHandler%s", capitalizedLiteral, suffix))
        .addModifiers(Modifier.PROTECTED)
        .returns(suffix.isEmpty() ? strategy.getHandlerType() : TypeName.get(JobHandler.class))
        .addParameter(TypeName.INT, "loopIndex")
        .addStatement("return $LHandlers$L.getOrDefault(loopIndex, $L())", strategy.getLiteral(), suffix, handleMethodName)
        .build();
  }

  protected MethodSpec buildHandle(TestCaseActivity activity, String suffix) {
    GeneratorStrategy strategy = ctx.getStrategy(activity.getId());

    String capitalizedLiteral = StringUtils.capitalize(strategy.getLiteral());

    String createHandlerMethodName;
    String handleMethodName;
    if (suffix.isEmpty()) {
      createHandlerMethodName = String.format("create%sHandler", capitalizedLiteral);
      handleMethodName = String.format("handle%s", capitalizedLiteral);
    } else {
      createHandlerMethodName = String.format("create%sHandler%s", capitalizedLiteral, suffix);
      handleMethodName = String.format("handle%s%s", capitalizedLiteral, suffix);
    }

    return MethodSpec.methodBuilder(handleMethodName)
        .addJavadoc("Returns a loop specific handler for $L: $L", activity.getTypeName(), activity.getId())
        .addModifiers(Modifier.PUBLIC)
        .returns(suffix.isEmpty() ? strategy.getHandlerType() : TypeName.get(JobHandler.class))
        .addParameter(TypeName.INT, "loopIndex")
        .addStatement("return $LHandlers$L.computeIfAbsent(loopIndex, this::$L)", strategy.getLiteral(), suffix, createHandlerMethodName)
        .build();
  }

  protected MethodSpec buildHandleDefault(TestCaseActivity activity, String suffix) {
    GeneratorStrategy strategy = ctx.getStrategy(activity.getId());

    String capitalizedLiteral = StringUtils.capitalize(strategy.getLiteral());

    String handleMethodName;
    if (suffix.isEmpty()) {
      handleMethodName = String.format("handle%s", capitalizedLiteral);
    } else {
      handleMethodName = String.format("handle%s%s", capitalizedLiteral, suffix);
    }

    return MethodSpec.methodBuilder(handleMethodName)
        .addJavadoc("Returns the default handler for $L: $L", activity.getTypeName(), activity.getId())
        .addModifiers(Modifier.PUBLIC)
        .returns(suffix.isEmpty() ? strategy.getHandlerType() : TypeName.get(JobHandler.class))
        .addStatement("return $L(-1)", handleMethodName)
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

  protected TypeName getSuperClass(TestCaseActivityScope scope) {
    GeneratorStrategy strategy = ctx.getStrategy(scope.getId());

    // e.g. MultiInstanceScopeHandler<MyScopeHandler>
    return ParameterizedTypeName.get(ClassName.get(MultiInstanceScopeHandler.class), strategy.getHandlerType());
  }

  private boolean hasNoneEndEvent(TestCaseActivityScope scope) {
    if (scope.getActivities().isEmpty()) {
      return false;
    }

    TestCaseActivity activity = scope.getActivities().get(scope.getActivities().size() - 1);
    if (activity.getType() != TestCaseActivityType.OTHER) {
      return false;
    }

    FlowNode flowNode = activity.getFlowNode();
    if (!(flowNode instanceof ThrowEvent)) {
      return false;
    }

    return new BpmnEventSupport((ThrowEvent) flowNode).isNoneEnd();
  }
}
