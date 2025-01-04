package org.camunda.community.bpmndt.cmd;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import javax.lang.model.element.Modifier;

import org.apache.commons.lang3.StringUtils;
import org.camunda.community.bpmndt.GeneratorResult;
import org.camunda.community.bpmndt.GeneratorStrategy;
import org.camunda.community.bpmndt.TestCaseContext;
import org.camunda.community.bpmndt.api.AbstractJUnit5TestCase;
import org.camunda.community.bpmndt.api.AbstractTestCase;
import org.camunda.community.bpmndt.api.TestCaseInstance;
import org.camunda.community.bpmndt.model.BpmnElementType;
import org.camunda.community.bpmndt.model.BpmnEventSupport;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import io.camunda.zeebe.model.bpmn.instance.CatchEvent;

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
    var strategies = ctx.getStrategies();

    var classBuilder = TypeSpec.classBuilder(ctx.getClassName())
        .addJavadoc(buildJavadoc(ctx))
        .superclass(AbstractJUnit5TestCase.class)
        .addModifiers(Modifier.PUBLIC);

    addHandlerFields(strategies, classBuilder);

    classBuilder.addMethod(buildBeforeEach(strategies));

    buildExecute(strategies, classBuilder);

    classBuilder.addMethod(buildGetBpmnProcessId(ctx));
    classBuilder.addMethod(buildGetBpmnResourceName(ctx));
    classBuilder.addMethod(buildGetEnd(ctx));
    classBuilder.addMethod(buildGetSimulateSubProcessResource());
    classBuilder.addMethod(buildGetStart(ctx));

    var startElement = ctx.getTestCase().getStartElement();
    if (startElement.isProcessStart()) {
      var eventSupport = new BpmnEventSupport(startElement.getFlowNode(CatchEvent.class));
      if (eventSupport.isMessage()) {
        classBuilder.addMethod(buildIsMessageStart());
      } else if (eventSupport.isSignal()) {
        classBuilder.addMethod(buildIsSignalStart());
      } else if (eventSupport.isTimer()) {
        classBuilder.addMethod(buildIsTimerStart());
      }
    } else {
      classBuilder.addMethod(buildIsNoProcessStart());
    }

    addHandlerMethods(strategies, classBuilder);

    var javaFile = JavaFile.builder(ctx.getPackageName(), classBuilder.build())
        .skipJavaLangImports(true)
        .build();

    result.addFile(javaFile);
  }

  private void addHandlerFields(List<GeneratorStrategy> strategies, TypeSpec.Builder classBuilder) {
    for (GeneratorStrategy strategy : strategies) {
      strategy.addHandlerField(classBuilder);
    }
  }

  private void addHandlerMethods(List<GeneratorStrategy> strategies, TypeSpec.Builder classBuilder) {
    for (GeneratorStrategy strategy : strategies) {
      strategy.addHandlerMethod(classBuilder);
    }
  }

  /**
   * Overrides the {@code beforeEach} method to initialize the element handlers (e.g. {@code UserTaskHandler}) that are required for a given test case.
   *
   * @return The {@code beforeEach} method.
   */
  private MethodSpec buildBeforeEach(List<GeneratorStrategy> strategies) {
    var builder = MethodSpec.methodBuilder("beforeEach")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED);

    for (GeneratorStrategy strategy : strategies) {
      strategy.initHandlerElement(builder);
    }

    for (GeneratorStrategy strategy : strategies) {
      strategy.initHandler(builder);
    }

    return builder.build();
  }

  private void buildExecute(List<GeneratorStrategy> strategies, TypeSpec.Builder classBuilder) {
    var executeBuilder = MethodSpec.methodBuilder("execute")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .addParameter(TestCaseInstance.class, "instance")
        .addParameter(TypeName.LONG, "flowScopeKey");

    new BuildTestCaseExecution().accept(strategies, executeBuilder);

    classBuilder.addMethod(executeBuilder.build());

    // for each non multi instance scope, generate an execute method
    for (GeneratorStrategy strategy : strategies) {
      if (strategy.getElement().getType() != BpmnElementType.SCOPE || strategy.getElement().isMultiInstance()) {
        continue;
      }

      var element = strategy.getElement();

      var executeScopeBuilder = MethodSpec.methodBuilder(String.format("execute%s", StringUtils.capitalize(strategy.getLiteral())))
          .addJavadoc(CodeBlock.builder().add("Executes $L: $L", element.getTypeName(), element.getId()).build())
          .addModifiers(Modifier.PROTECTED)
          .addParameter(TestCaseInstance.class, "instance")
          .addParameter(TypeName.LONG, "parentFlowScopeKey");

      executeScopeBuilder.addCode("long flowScopeKey = instance.getElementInstanceKey(parentFlowScopeKey, $S);\n", element.getId());

      new BuildTestCaseExecution(element).accept(strategies, executeScopeBuilder);

      classBuilder.addMethod(executeScopeBuilder.build());
    }
  }

  private MethodSpec buildGetBpmnResourceName(TestCaseContext testCaseContext) {
    return MethodSpec.methodBuilder("getBpmnResourceName")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .returns(String.class)
        .addStatement("return $S", testCaseContext.getResourceName())
        .build();
  }

  private MethodSpec buildGetEnd(TestCaseContext ctx) {
    var end = ctx.getTestCase().getEndElement();

    return MethodSpec.methodBuilder("getEnd")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(String.class)
        .addStatement("return $S", end.getId())
        .build();
  }

  private MethodSpec buildGetBpmnProcessId(TestCaseContext ctx) {
    return MethodSpec.methodBuilder("getBpmnProcessId")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(String.class)
        .addStatement("return $S", ctx.getTestCase().getProcessId())
        .build();
  }

  private MethodSpec buildGetSimulateSubProcessResource() {
    var simulateSubProcessResource = ClassName.get(AbstractTestCase.class.getPackageName(), "SimulateSubProcessResource");

    return MethodSpec.methodBuilder("getSimulateSubProcessResource")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .returns(String.class)
        .addStatement("return $T.VALUE", simulateSubProcessResource)
        .build();
  }

  private MethodSpec buildGetStart(TestCaseContext ctx) {
    var start = ctx.getTestCase().getStartElement();

    return MethodSpec.methodBuilder("getStart")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(String.class)
        .addStatement("return $S", start.getId())
        .build();
  }

  private MethodSpec buildIsMessageStart() {
    return MethodSpec.methodBuilder("isMessageStart")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .returns(TypeName.BOOLEAN)
        .addStatement("return true")
        .build();
  }

  private MethodSpec buildIsNoProcessStart() {
    return MethodSpec.methodBuilder("isProcessStart")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .returns(TypeName.BOOLEAN)
        .addStatement("return false")
        .build();
  }

  private MethodSpec buildIsSignalStart() {
    return MethodSpec.methodBuilder("isSignalStart")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .returns(TypeName.BOOLEAN)
        .addStatement("return true")
        .build();
  }

  private MethodSpec buildIsTimerStart() {
    return MethodSpec.methodBuilder("isTimerStart")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .returns(TypeName.BOOLEAN)
        .addStatement("return true")
        .build();
  }

  private CodeBlock buildJavadoc(TestCaseContext ctx) {
    var builder = CodeBlock.builder();

    var testCase = ctx.getTestCase();
    if (testCase.getDescription() != null) {
      builder.add(testCase.getDescription());
      builder.add("\n<br>\n");
    }

    var a = testCase.getStartElement();
    var b = testCase.getEndElement();

    var args = new LinkedList<>();
    args.add(a.getTypeName());
    args.add(a.getId());
    args.add(b.getTypeName());
    args.add(b.getId());
    args.add(testCase.getElementIds().size());

    builder.add("From: $L: $L, To: $L: $L, Length: $L", args.toArray(new Object[0]));

    return builder.build();
  }
}
