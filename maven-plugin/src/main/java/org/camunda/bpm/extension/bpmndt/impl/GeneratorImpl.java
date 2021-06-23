package org.camunda.bpm.extension.bpmndt.impl;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.extension.bpmndt.BpmnNode;
import org.camunda.bpm.extension.bpmndt.BpmnSupport;
import org.camunda.bpm.extension.bpmndt.Generator;
import org.camunda.bpm.extension.bpmndt.GeneratorContext;
import org.camunda.bpm.extension.bpmndt.impl.generation.AbstractTestCase;
import org.camunda.bpm.extension.bpmndt.impl.generation.After;
import org.camunda.bpm.extension.bpmndt.impl.generation.Before;
import org.camunda.bpm.extension.bpmndt.impl.generation.BpmndtConfiguration;
import org.camunda.bpm.extension.bpmndt.impl.generation.BpmndtPlugin;
import org.camunda.bpm.extension.bpmndt.impl.generation.CallActivityParseListener;
import org.camunda.bpm.extension.bpmndt.impl.generation.CallActivityRule;
import org.camunda.bpm.extension.bpmndt.impl.generation.HandleAsyncAfter;
import org.camunda.bpm.extension.bpmndt.impl.generation.HandleAsyncBefore;
import org.camunda.bpm.extension.bpmndt.impl.generation.HandleCallActivityInput;
import org.camunda.bpm.extension.bpmndt.impl.generation.HandleCallActivityOutput;
import org.camunda.bpm.extension.bpmndt.impl.generation.HandleExternalTask;
import org.camunda.bpm.extension.bpmndt.impl.generation.HandleIntermediateCatchEvent;
import org.camunda.bpm.extension.bpmndt.impl.generation.HandleUserTask;
import org.camunda.bpm.extension.bpmndt.impl.generation.TestMethod;
import org.camunda.bpm.extension.bpmndt.impl.generation.TestMethodPathEmpty;
import org.camunda.bpm.extension.bpmndt.impl.generation.TestMethodPathNotValid;
import org.camunda.bpm.extension.bpmndt.type.Path;
import org.camunda.bpm.extension.bpmndt.type.TestCase;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

class GeneratorImpl implements Generator {

  private final GeneratorContext context;

  GeneratorImpl(GeneratorContext context) {
    this.context = context;
  }

  protected CodeBlock buildJavadoc(GeneratorContext context, TestCase testCase) {
    Path path = testCase.getPath();

    BpmnNode startNode = context.getBpmnSupport().get(path.getStart());
    BpmnNode endNode = context.getBpmnSupport().get(path.getEnd());

    CodeBlock.Builder builder = CodeBlock.builder();

    if (testCase.getDescription() != null) {
      builder.add(testCase.getDescription());
      builder.add("\n<br>\n");
    }

    builder.add("Start:");
    builder.add("\n<br>\n");
    builder.add("$L: $L", startNode.getType(), startNode.getId());
    builder.add("\n<br><br>\n");
    builder.add("End:");
    builder.add("\n<br>\n");
    builder.add("$L: $L", endNode.getType(), endNode.getId());
    builder.add("\n<br><br>\n");
    builder.add("Flow nodes:");
    builder.add("\n<br>\n");
    builder.add("$L", path.getFlowNodeIds().size());

    return builder.build();
  }

  protected void buildStaticImports(TestCase testCase, JavaFile.Builder javaFileBuilder) {
    List<String> flowNodeIds = testCase.getPath().getFlowNodeIds();

    Set<String> bpmnAwareHelpers = new HashSet<>();
    bpmnAwareHelpers.add("assertThat");

    if (context.getBpmnSupport().hasJob(flowNodeIds)) {
      bpmnAwareHelpers.add("job");
    }
    if (context.getBpmnSupport().hasUserTask(flowNodeIds)) {
      bpmnAwareHelpers.add("task");
    }

    // add static imports
    if (!bpmnAwareHelpers.isEmpty()) {
      javaFileBuilder.addStaticImport(ProcessEngineTests.class, bpmnAwareHelpers.toArray(new String[0]));
    }
  }

  protected boolean buildTestCase(TestCase testCase, TypeSpec.Builder classBuilder) {
    List<String> flowNodeIds = testCase.getPath().getFlowNodeIds();
    if (flowNodeIds.isEmpty()) {
      // path is empty
      classBuilder.addMethod(new TestMethodPathEmpty().apply(context, testCase));
      return false;
    }

    BpmnSupport bpmnSupport = context.getBpmnSupport();

    if (!bpmnSupport.has(flowNodeIds)) {
      // path is not valid
      classBuilder.addMethod(new TestMethodPathNotValid().apply(context, testCase));
      return false;
    }

    classBuilder.addMethod(new TestMethod().apply(context, testCase));

    for (String flowNodeId : flowNodeIds) {
      BpmnNode node = bpmnSupport.get(flowNodeId);

      if (node.isAsyncBefore()) {
        classBuilder.addMethod(new HandleAsyncBefore().apply(node));
      }

      if (node.isCallActivity()) {
        classBuilder.addMethod(new HandleCallActivityInput().apply(node));
        classBuilder.addMethod(new HandleCallActivityOutput().apply(node));
      }
      if (node.isExternalTask()) {
        classBuilder.addMethod(new HandleExternalTask().apply(node));
      }
      if (node.isIntermediateCatchEvent()) {
        classBuilder.addMethod(new HandleIntermediateCatchEvent().apply(node));
      }
      if (node.isUserTask()) {
        classBuilder.addMethod(new HandleUserTask().apply(node));
      }

      if (node.isAsyncAfter()) {
        classBuilder.addMethod(new HandleAsyncAfter().apply(node));
      }
    }

    classBuilder.addMethod(new After().apply(context));

    return true;
  }

  protected String buildTestCaseClassName(TestCase testCase) {
    String processId = BpmnSupport.convert(context.getBpmnSupport().getProcessId());
    return String.format("TC_%s__%s", processId, testCase.getName());
  }

  protected JavaFile buildType(Function<GeneratorContext, TypeSpec> builderFunction) {
    return JavaFile.builder(context.getPackageName(), builderFunction.apply(context)).skipJavaLangImports(true).build();
  }

  @Override
  public JavaFile generate(TestCase testCase) {
    TypeSpec.Builder classBuilder = TypeSpec.classBuilder(buildTestCaseClassName(testCase))
        .addJavadoc(buildJavadoc(context, testCase))
        .superclass(ClassName.get(context.getPackageName(), GeneratorConstants.TYPE_ABSTRACT_TEST_CASE))
        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
        .addMethod(new Before().apply(context));

    // build actual test case
    boolean built = buildTestCase(testCase, classBuilder);

    JavaFile.Builder javaFileBuilder = JavaFile.builder(context.getPackageName(), classBuilder.build())
        .skipJavaLangImports(true);

    if (built) {
      // gather names of required static helper methods
      buildStaticImports(testCase, javaFileBuilder);
    }

    return javaFileBuilder.build();
  }

  @Override
  public List<JavaFile> generateFramework() {
    List<JavaFile> framework = new LinkedList<>();
    framework.add(buildType(new AbstractTestCase()));
    framework.add(buildType(new CallActivityParseListener()));
    framework.add(buildType(new CallActivityRule()));
    framework.add(buildType(new BpmndtPlugin()));

    if (context.isSpringEnabled()) {
      framework.add(buildType(new BpmndtConfiguration()));
    }

    return framework;
  }
}
