package org.camunda.community.bpmndt.cmd;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.community.bpmndt.GeneratorContext;
import org.camunda.community.bpmndt.GeneratorResult;
import org.camunda.community.bpmndt.GeneratorStrategy;
import org.camunda.community.bpmndt.TestCaseContext;
import org.camunda.community.bpmndt.api.AbstractJUnit4TestCase;
import org.camunda.community.bpmndt.api.AbstractJUnit5TestCase;
import org.camunda.community.bpmndt.api.AbstractTestCase;
import org.camunda.community.bpmndt.model.TestCase;
import org.camunda.community.bpmndt.model.TestCaseActivity;
import org.camunda.community.bpmndt.model.TestCaseActivityType;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

/**
 * Generates a test case, using a test framework (JUnit 4 or 5) specific superclass.
 * 
 * @see AbstractJUnit4TestCase
 * @see AbstractJUnit5TestCase
 */
public class GenerateTestCase implements Consumer<TestCaseContext> {

  private final GeneratorContext gCtx;
  private final GeneratorResult result;

  public GenerateTestCase(GeneratorContext gCtx, GeneratorResult result) {
    this.gCtx = gCtx;
    this.result = result;
  }

  @Override
  public void accept(TestCaseContext ctx) {
    List<GeneratorStrategy> strategies = new GetStrategies().apply(ctx, ctx.getTestCase().getActivities());

    TypeSpec.Builder classBuilder = TypeSpec.classBuilder(ctx.getClassName())
        .addJavadoc(buildJavadoc(ctx))
        .superclass(getSuperClass(ctx))
        .addModifiers(Modifier.PUBLIC);

    addHandlerFields(strategies, classBuilder);

    classBuilder.addMethod(buildBeforeEach(strategies));
    classBuilder.addMethod(buildExecute(ctx, strategies));

    classBuilder.addMethod(buildGetBpmnResourceName(gCtx, ctx));
    classBuilder.addMethod(buildGetEnd(ctx));
    classBuilder.addMethod(buildGetProcessDefinitionKey(ctx));

    if (!gCtx.getProcessEnginePluginNames().isEmpty()) {
      classBuilder.addMethod(new BuildGetProcessEnginePlugins().apply(gCtx));
    }

    classBuilder.addMethod(buildGetStart(ctx));

    if (!ctx.getTestCase().getEndActivity().isProcessEnd()) {
      classBuilder.addMethod(buildIsProcessEnd());
    }

    if (gCtx.isSpringEnabled()) {
      classBuilder.addMethod(buildIsSpringEnabled());
    }

    addHandlerMethods(strategies, classBuilder);

    JavaFile javaFile = JavaFile.builder(ctx.getPackageName(), classBuilder.build())
        .addStaticImport(ProcessEngineTests.class, "assertThat")
        .skipJavaLangImports(true)
        .build();

    result.addFile(javaFile);
  }

  protected void addHandlerFields(List<GeneratorStrategy> strategies, TypeSpec.Builder classBuilder) {
    for (GeneratorStrategy strategy : strategies) {
      if (strategy.shouldHandleBefore()) {
        strategy.addHandlerFieldBefore(classBuilder);
      }

      strategy.addHandlerField(classBuilder);

      if (strategy.shouldHandleAfter()) {
        strategy.addHandlerFieldAfter(classBuilder);
      }
    }
  }

  protected void addHandlerMethods(List<GeneratorStrategy> strategies, TypeSpec.Builder classBuilder) {
    for (GeneratorStrategy strategy : strategies) {
      TestCaseActivity activity = strategy.getActivity();

      // since the async before of a call activity is instrumented
      // the handler method should not be generated
      if (strategy.shouldHandleBefore() && activity.getType() != TestCaseActivityType.CALL_ACTIVITY) {
        strategy.addHandlerMethodBefore(classBuilder);
      }

      strategy.addHandlerMethod(classBuilder);

      if (strategy.shouldHandleAfter()) {
        strategy.addHandlerMethodAfter(classBuilder);
      }
    }
  }

  /**
   * Overrides the {@code beforeEach} method of the {@link AbstractTestCase} to initialize the
   * activity handlers (e.g. {@code UserTaskHandler}) that are required for a given test case.
   * 
   * @return The {@code beforeEach} method.
   */
  protected MethodSpec buildBeforeEach(List<GeneratorStrategy> strategies) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("beforeEach")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED);

    // call #beforeEach of superclass
    builder.addStatement("super.$L()", "beforeEach");

    for (GeneratorStrategy strategy : strategies) {
      if (strategy.shouldHandleBefore()) {
        strategy.initHandlerBefore(builder);
      }

      strategy.initHandler(builder);

      if (strategy.shouldHandleAfter()) {
        strategy.initHandlerAfter(builder);
      }
    }

    return builder.build();
  }

  protected MethodSpec buildExecute(TestCaseContext ctx, List<GeneratorStrategy> strategies) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("execute")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .addParameter(ProcessInstance.class, "pi");

    new BuildTestCaseExecution(ctx).accept(strategies, builder);

    return builder.build();
  }

  protected MethodSpec buildGetBpmnResourceName(GeneratorContext ctx, TestCaseContext testCaseContext) {
    return MethodSpec.methodBuilder("getBpmnResourceName")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .returns(String.class)
        .addStatement("return $S", testCaseContext.getResourceName())
        .build();
  }

  protected MethodSpec buildGetEnd(TestCaseContext ctx) {
    TestCaseActivity end = ctx.getTestCase().getEndActivity();

    return MethodSpec.methodBuilder("getEnd")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(String.class)
        .addStatement("return $S", end.getId())
        .build();
  }

  protected MethodSpec buildGetProcessDefinitionKey(TestCaseContext ctx) {
    return MethodSpec.methodBuilder("getProcessDefinitionKey")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(String.class)
        .addStatement("return $S", ctx.getTestCase().getProcessId())
        .build();
  }

  protected MethodSpec buildGetStart(TestCaseContext ctx) {
    TestCaseActivity start = ctx.getTestCase().getStartActivity();

    return MethodSpec.methodBuilder("getStart")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(String.class)
        .addStatement("return $S", start.getId())
        .build();
  }

  protected MethodSpec buildIsProcessEnd() {
    return MethodSpec.methodBuilder("isProcessEnd")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .returns(TypeName.BOOLEAN)
        .addStatement("return false")
        .build();
  }

  protected MethodSpec buildIsSpringEnabled() {
    return MethodSpec.methodBuilder("isSpringEnabled")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .returns(TypeName.BOOLEAN)
        .addStatement("return true")
        .build();
  }

  protected CodeBlock buildJavadoc(TestCaseContext ctx) {
    CodeBlock.Builder builder = CodeBlock.builder();

    TestCase testCase = ctx.getTestCase();
    if (testCase.getDescription() != null) {
      builder.add(testCase.getDescription());
      builder.add("\n<br>\n");
    }

    TestCaseActivity a = ctx.getTestCase().getStartActivity();
    TestCaseActivity b = ctx.getTestCase().getEndActivity();

    List<Object> args = new LinkedList<>();
    args.add(a.getTypeName());
    args.add(a.getId());
    args.add(b.getTypeName());
    args.add(b.getId());
    args.add(testCase.getFlowNodeIds().size());

    builder.add("From: $L: $L, To: $L: $L, Length: $L", args.toArray(new Object[0]));

    return builder.build();
  }

  protected TypeName getSuperClass(TestCaseContext ctx) {
    ClassName rawType;
    if (gCtx.isJUnit5Enabled()) {
      rawType = ClassName.get(AbstractJUnit5TestCase.class);
    } else {
      rawType = ClassName.get(AbstractJUnit4TestCase.class);
    }

    // e.g. AbstractJUnit4TestCase<TC_startEvent__endEvent>
    return ParameterizedTypeName.get(rawType, ClassName.bestGuess(ctx.getClassName()));
  }
}
