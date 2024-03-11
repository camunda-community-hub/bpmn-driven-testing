package org.camunda.community.bpmndt.platform8.cmd;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.community.bpmndt.GeneratorResult;
import org.camunda.community.bpmndt.model.platform8.BpmnElement;
import org.camunda.community.bpmndt.model.platform8.TestCase;
import org.camunda.community.bpmndt.platform8.GeneratorStrategy;
import org.camunda.community.bpmndt.platform8.TestCaseContext;
import org.camunda.community.bpmndt.platform8.api.AbstractJUnit5TestCase;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

/**
 * Generates a test case, using a test framework (JUnit 5) specific superclass.
 *
 * @see AbstractJUnit5TestCase
 */
public class GenerateTestCase implements Consumer<TestCaseContext> {

  private final GeneratorResult result;

  public GenerateTestCase(GeneratorResult result) {
    this.result = result;
  }

  @Override
  public void accept(TestCaseContext ctx) {
    List<GeneratorStrategy> strategies = new ArrayList<>(ctx.getTestCase().getElementIds().size());
    for (String elementId : ctx.getTestCase().getElementIds()) {
      strategies.add(ctx.getStrategy(elementId));
    }

    TypeSpec.Builder classBuilder = TypeSpec.classBuilder(ctx.getClassName())
        .addJavadoc(buildJavadoc(ctx))
        .superclass(AbstractJUnit5TestCase.class)
        .addModifiers(Modifier.PUBLIC);

    addHandlerFields(strategies, classBuilder);

    classBuilder.addMethod(buildBeforeEach(strategies));
    classBuilder.addMethod(buildExecute(ctx, strategies));

    classBuilder.addMethod(buildGetBpmnProcessId(ctx));
    classBuilder.addMethod(buildGetBpmnResourceName(ctx));
    classBuilder.addMethod(buildGetEnd(ctx));

    classBuilder.addMethod(buildGetStart(ctx));

    if (!ctx.getTestCase().getEndElement().isProcessEnd()) {
      classBuilder.addMethod(buildIsProcessEnd());
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
      strategy.addHandlerField(classBuilder);
    }
  }

  protected void addHandlerMethods(List<GeneratorStrategy> strategies, TypeSpec.Builder classBuilder) {
    for (GeneratorStrategy strategy : strategies) {
      strategy.addHandlerMethod(classBuilder);
    }
  }

  /**
   * Overrides the {@code beforeEach} method to initialize the element handlers (e.g. {@code UserTaskHandler}) that are required for a given test case.
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
      strategy.initHandlerElement(builder);
    }

    for (GeneratorStrategy strategy : strategies) {
      strategy.initHandler(builder);
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

  protected MethodSpec buildGetBpmnResourceName(TestCaseContext testCaseContext) {
    return MethodSpec.methodBuilder("getBpmnResourceName")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .returns(String.class)
        .addStatement("return $S", testCaseContext.getResourceName())
        .build();
  }

  protected MethodSpec buildGetEnd(TestCaseContext ctx) {
    BpmnElement end = ctx.getTestCase().getEndElement();

    return MethodSpec.methodBuilder("getEnd")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(String.class)
        .addStatement("return $S", end.getId())
        .build();
  }

  protected MethodSpec buildGetBpmnProcessId(TestCaseContext ctx) {
    return MethodSpec.methodBuilder("getBpmnProcessId")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(String.class)
        .addStatement("return $S", ctx.getTestCase().getProcessId())
        .build();
  }

  protected MethodSpec buildGetStart(TestCaseContext ctx) {
    BpmnElement start = ctx.getTestCase().getStartElement();

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

  protected CodeBlock buildJavadoc(TestCaseContext ctx) {
    CodeBlock.Builder builder = CodeBlock.builder();

    TestCase testCase = ctx.getTestCase();
    if (testCase.getDescription() != null) {
      builder.add(testCase.getDescription());
      builder.add("\n<br>\n");
    }

    BpmnElement a = testCase.getStartElement();
    BpmnElement b = testCase.getEndElement();

    List<Object> args = new LinkedList<>();
    args.add(a.getTypeName());
    args.add(a.getId());
    args.add(b.getTypeName());
    args.add(b.getId());
    args.add(testCase.getElementIds().size());

    builder.add("From: $L: $L, To: $L: $L, Length: $L", args.toArray(new Object[0]));

    return builder.build();
  }
}
