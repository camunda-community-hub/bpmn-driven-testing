package org.camunda.community.bpmndt.cmd;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.community.bpmndt.GeneratorContext;
import org.camunda.community.bpmndt.GeneratorResult;
import org.camunda.community.bpmndt.GeneratorStrategy;
import org.camunda.community.bpmndt.TestCaseActivity;
import org.camunda.community.bpmndt.TestCaseContext;
import org.camunda.community.bpmndt.api.AbstractJUnit4TestCase;
import org.camunda.community.bpmndt.cmd.generation.Execute;
import org.camunda.community.bpmndt.cmd.generation.GetProcessEnginePlugins;
import org.camunda.community.bpmndt.cmd.generation.IsH2Version2;
import org.camunda.community.bpmndt.cmd.generation.Starting;
import org.junit.rules.TestRule;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

/**
 * Generates a JUnit 4 based test case in form of a JUnit {@link TestRule} implementation.
 * 
 * @see AbstractJUnit4TestCase
 */
public class GenerateJUnit4TestCase implements Consumer<TestCaseContext> {

  private final GeneratorContext gCtx;
  private final GeneratorResult result;

  public GenerateJUnit4TestCase(GeneratorContext gCtx, GeneratorResult result) {
    this.gCtx = gCtx;
    this.result = result;
  }

  @Override
  public void accept(TestCaseContext ctx) {
    TypeSpec.Builder classBuilder = TypeSpec.classBuilder(ctx.getClassName())
        .addJavadoc(buildJavadoc(ctx))
        .superclass(AbstractJUnit4TestCase.class)
        .addModifiers(Modifier.PUBLIC);

    addHandlerFields(ctx, classBuilder);

    classBuilder.addMethod(new Starting().apply(ctx));
    classBuilder.addMethod(new Execute().apply(ctx));

    classBuilder.addMethod(buildGetBpmnResourceName(gCtx, ctx));
    classBuilder.addMethod(buildGetEnd(ctx));
    classBuilder.addMethod(buildGetProcessDefinitionKey(ctx));

    if (!gCtx.getProcessEnginePluginNames().isEmpty()) {
      classBuilder.addMethod(new GetProcessEnginePlugins().apply(gCtx));
    }

    classBuilder.addMethod(buildGetStart(ctx));

    if (ctx.isValid() && !ctx.getEndActivity().isProcessEnd()) {
      classBuilder.addMethod(buildIsProcessEnd());
    }

    if (!gCtx.isH2Version2()) {
      classBuilder.addMethod(new IsH2Version2().apply(gCtx));
    }

    if (gCtx.isSpringEnabled()) {
      classBuilder.addMethod(buildIsSpringEnabled());
    }

    addHandlerMethods(ctx, classBuilder);

    String packageName = String.format("%s.%s", gCtx.getPackageName(), ctx.getPackageName());

    JavaFile javaFile = JavaFile.builder(packageName, classBuilder.build())
        .addStaticImport(ProcessEngineTests.class, "assertThat")
        .skipJavaLangImports(true)
        .build();

    result.addFile(javaFile);
  }

  protected void addHandlerFields(TestCaseContext ctx, TypeSpec.Builder classBuilder) {
    if (!ctx.isValid()) {
      return;
    }

    for (TestCaseActivity activity : ctx.getActivities()) {
      GeneratorStrategy strategy = activity.getStrategy();

      if (strategy.shouldHandleBefore()) {
        strategy.addHandlerFieldBefore(classBuilder);
      }

      strategy.addHandlerField(classBuilder);

      if (strategy.shouldHandleAfter()) {
        strategy.addHandlerFieldAfter(classBuilder);
      }
    }
  }

  protected void addHandlerMethods(TestCaseContext ctx, TypeSpec.Builder classBuilder) {
    if (!ctx.isValid()) {
      return;
    }
    
    for (TestCaseActivity activity : ctx.getActivities()) {
      GeneratorStrategy strategy = activity.getStrategy();

      if (activity.isAsyncBefore()) {
        strategy.addHandlerMethodBefore(classBuilder);
      }

      strategy.addHandlerMethod(classBuilder);

      if (strategy.shouldHandleAfter()) {
        strategy.addHandlerMethodAfter(classBuilder);
      }
    }
  }

  protected MethodSpec buildGetBpmnResourceName(GeneratorContext ctx, TestCaseContext testCaseContext) {
    return MethodSpec.methodBuilder("getBpmnResourceName")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .returns(String.class)
        .addStatement("return $S", testCaseContext.getResourceName(ctx.getMainResourcePath()))
        .build();
  }

  protected MethodSpec buildGetEnd(TestCaseContext ctx) {
    TestCaseActivity end = ctx.getEndActivity();

    return MethodSpec.methodBuilder("getEnd")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(String.class)
        .addStatement("return $S", end != null ? end.getId() : null)
        .build();
  }

  protected MethodSpec buildGetProcessDefinitionKey(TestCaseContext testCaseContext) {
    return MethodSpec.methodBuilder("getProcessDefinitionKey")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(String.class)
        .addStatement("return $S", testCaseContext.getProcessId())
        .build();
  }

  protected MethodSpec buildGetStart(TestCaseContext ctx) {
    TestCaseActivity start = ctx.getStartActivity();

    return MethodSpec.methodBuilder("getStart")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(String.class)
        .addStatement("return $S", start != null ? start.getId() : null)
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

    if (ctx.getDescription() != null) {
      builder.add(ctx.getDescription());
      builder.add("\n<br>\n");
    }

    if (ctx.isValid()) {
      TestCaseActivity a = ctx.getStartActivity();
      TestCaseActivity b = ctx.getEndActivity();

      List<Object> args = new LinkedList<>();
      args.add(a.getTypeName());
      args.add(a.getId());
      args.add(b.getTypeName());
      args.add(b.getId());
      args.add(ctx.getActivities().size());

      builder.add("From: $L: $L, To: $L: $L, Length: $L", args.toArray(new Object[0]));
    }

    return builder.build();
  }
}
