package org.camunda.community.bpmndt.cmd;

import java.lang.reflect.Type;
import java.util.function.BiConsumer;

import javax.lang.model.element.Modifier;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.community.bpmndt.GeneratorContext;
import org.camunda.community.bpmndt.GeneratorResult;
import org.camunda.community.bpmndt.TestCaseActivity;
import org.camunda.community.bpmndt.TestCaseActivityType;
import org.camunda.community.bpmndt.TestCaseContext;
import org.camunda.community.bpmndt.api.AbstractJUnit4SpringBasedTestRule;
import org.camunda.community.bpmndt.api.AbstractJUnit4TestRule;
import org.camunda.community.bpmndt.api.CallActivityHandler;
import org.camunda.community.bpmndt.api.ExternalTaskHandler;
import org.camunda.community.bpmndt.api.IntermediateCatchEventHandler;
import org.camunda.community.bpmndt.api.JobHandler;
import org.camunda.community.bpmndt.api.UserTaskHandler;
import org.camunda.community.bpmndt.cmd.generation.Execute;
import org.camunda.community.bpmndt.cmd.generation.GetProcessEnginePlugins;
import org.camunda.community.bpmndt.cmd.generation.Starting;
import org.junit.rules.TestRule;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

/**
 * Generates a JUnit 4 based test case in form of a JUnit {@link TestRule} implementation.
 * 
 * @see AbstractJUnit4TestRule
 * @see AbstractJUnit4SpringBasedTestRule
 */
public class GenerateJUnit4TestRule implements BiConsumer<GeneratorContext, TestCaseContext> {

  private final GeneratorResult result;

  public GenerateJUnit4TestRule(GeneratorResult result) {
    this.result = result;
  }

  @Override
  public void accept(GeneratorContext gCtx, TestCaseContext ctx) {
    Type superClass;
    if (gCtx.isSpringEnabled()) {
      superClass = AbstractJUnit4SpringBasedTestRule.class;
    } else {
      superClass = AbstractJUnit4TestRule.class;
    }

    TypeSpec.Builder classBuilder = TypeSpec.classBuilder(ctx.getClassName())
        .addJavadoc(buildJavadoc(ctx))
        .superclass(superClass)
        .addModifiers(Modifier.PUBLIC);

    addHandlerFields(ctx, classBuilder);

    classBuilder.addMethod(new Starting().apply(ctx));
    classBuilder.addMethod(new Execute().apply(ctx));

    classBuilder.addMethod(buildGetBpmnResourceName(gCtx, ctx));
    classBuilder.addMethod(buildGetEnd(ctx));
    classBuilder.addMethod(buildGetProcessDefinitionKey(ctx));
    classBuilder.addMethod(new GetProcessEnginePlugins().apply(gCtx));
    classBuilder.addMethod(buildGetStart(ctx));

    addHandlerMethods(ctx, classBuilder);

    JavaFile javaFile = JavaFile.builder(gCtx.getPackageName(), classBuilder.build())
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
      if (activity.isAsyncBefore()) {
        classBuilder.addField(JobHandler.class, activity.getLiteralBefore(), Modifier.PRIVATE);
      }

      switch (activity.getType()) {
        case CALL_ACTIVITY:
          classBuilder.addField(CallActivityHandler.class, activity.getLiteral(), Modifier.PRIVATE);
          break;
        case EXTERNAL_TASK:
          classBuilder.addField(ExternalTaskHandler.class, activity.getLiteral(), Modifier.PRIVATE);
          break;
        case MESSAGE_CATCH_EVENT:
        case SIGNAL_CATCH_EVENT:
          classBuilder.addField(IntermediateCatchEventHandler.class, activity.getLiteral(), Modifier.PRIVATE);
          break;
        case TIMER_CATCH_EVENT:
          classBuilder.addField(JobHandler.class, activity.getLiteral(), Modifier.PRIVATE);
          break;
        case USER_TASK:
          classBuilder.addField(UserTaskHandler.class, activity.getLiteral(), Modifier.PRIVATE);
          break;
        default:
          // other activities are not handled
          break;
      }

      if (activity.isAsyncAfter()) {
        classBuilder.addField(JobHandler.class, activity.getLiteralAfter(), Modifier.PRIVATE);
      }
    }
  }

  protected void addHandlerMethods(TestCaseContext ctx, TypeSpec.Builder classBuilder) {
    if (!ctx.isValid()) {
      return;
    }
    
    MethodSpec.Builder builder;
    
    for (TestCaseActivity activity : ctx.getActivities()) {
      if (activity.isAsyncBefore()) {
        builder = MethodSpec.methodBuilder(buildHandleMethodName(activity.getLiteralBefore()))
            .addJavadoc(buildHandleMethodJavadoc(activity))
            .addModifiers(Modifier.PUBLIC)
            .returns(JobHandler.class)
            .addStatement("return $L", activity.getLiteralBefore());
        
        classBuilder.addMethod(builder.build());
      }
      
      builder = MethodSpec.methodBuilder(buildHandleMethodName(activity.getLiteral()))
          .addJavadoc(buildHandleMethodJavadoc(activity))
          .addModifiers(Modifier.PUBLIC)
          .addStatement("return $L", activity.getLiteral());

      switch (activity.getType()) {
        case CALL_ACTIVITY:
          builder.returns(CallActivityHandler.class);
          break;
        case EXTERNAL_TASK:
          builder.returns(ExternalTaskHandler.class);
          break;
        case MESSAGE_CATCH_EVENT:
        case SIGNAL_CATCH_EVENT:
          builder.returns(IntermediateCatchEventHandler.class);
          break;
        case TIMER_CATCH_EVENT:
          builder.returns(JobHandler.class);
          break;
        case USER_TASK:
          builder.returns(UserTaskHandler.class);
          break;
        default:
          // other activities are not handled
          break;
      }

      if (activity.getType() != TestCaseActivityType.OTHER) {
        classBuilder.addMethod(builder.build());
      }

      if (activity.isAsyncAfter()) {
        builder = MethodSpec.methodBuilder(buildHandleMethodName(activity.getLiteralAfter()))
            .addJavadoc(buildHandleMethodJavadoc(activity))
            .addModifiers(Modifier.PUBLIC)
            .returns(JobHandler.class)
            .addStatement("return $L", activity.getLiteralAfter());
        
        classBuilder.addMethod(builder.build());
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
        .addModifiers(Modifier.PROTECTED)
        .returns(String.class)
        .addStatement("return $S", end != null ? end.getId() : null)
        .build();
  }

  protected MethodSpec buildGetProcessDefinitionKey(TestCaseContext testCaseContext) {
    return MethodSpec.methodBuilder("getProcessDefinitionKey")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .returns(String.class)
        .addStatement("return $S", testCaseContext.getProcessId())
        .build();
  }

  protected MethodSpec buildGetStart(TestCaseContext ctx) {
    TestCaseActivity start = ctx.getStartActivity();

    return MethodSpec.methodBuilder("getStart")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .returns(String.class)
        .addStatement("return $S", start != null ? start.getId() : null)
        .build();
  }

  protected String buildHandleMethodJavadoc(TestCaseActivity activity) {
    return String.format("Returns the handler for %s: %s", activity.getTypeName(), activity.getId());
  }

  protected String buildHandleMethodName(String literal) {
    return String.format("handle%s", StringUtils.capitalize(literal));
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

      builder.add("From: ");
      builder.add("$L: $L", a.getType(), a.getId());
      builder.add(", To: ");
      builder.add("$L: $L", b.getType(), b.getId());
      builder.add(", Length: ");
      builder.add("$L", ctx.getActivities().size());
    }

    return builder.build();
  }
}
