package org.camunda.bpm.extension.bpmndt.impl;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Function;

import javax.lang.model.element.Modifier;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.extension.bpmndt.BpmnNode;
import org.camunda.bpm.extension.bpmndt.BpmnSupport;
import org.camunda.bpm.extension.bpmndt.Generator;
import org.camunda.bpm.extension.bpmndt.GeneratorContext;
import org.camunda.bpm.extension.bpmndt.impl.generation.Execute;
import org.camunda.bpm.extension.bpmndt.impl.generation.GetBpmnResourceName;
import org.camunda.bpm.extension.bpmndt.impl.generation.GetEnd;
import org.camunda.bpm.extension.bpmndt.impl.generation.GetProcessDefinitionKey;
import org.camunda.bpm.extension.bpmndt.impl.generation.GetProcessEnginePlugins;
import org.camunda.bpm.extension.bpmndt.impl.generation.GetStart;
import org.camunda.bpm.extension.bpmndt.impl.generation.Starting;
import org.camunda.bpm.extension.bpmndt.type.Path;
import org.camunda.bpm.extension.bpmndt.type.TestCase;
import org.camunda.bpm.model.bpmn.instance.EventDefinition;
import org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.camunda.bpm.model.bpmn.instance.SignalEventDefinition;
import org.camunda.bpm.model.bpmn.instance.TimerEventDefinition;
import org.camunda.community.bpmndt.api.AbstractJUnit4SpringBasedTestRule;
import org.camunda.community.bpmndt.api.AbstractJUnit4TestRule;
import org.camunda.community.bpmndt.api.CallActivityHandler;
import org.camunda.community.bpmndt.api.ExternalTaskHandler;
import org.camunda.community.bpmndt.api.IntermediateCatchEventHandler;
import org.camunda.community.bpmndt.api.JobHandler;
import org.camunda.community.bpmndt.api.UserTaskHandler;
import org.camunda.community.bpmndt.api.cfg.AbstractConfiguration;
import org.springframework.context.annotation.Configuration;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

class GeneratorImpl implements Generator {

  private final GeneratorContext context;

  GeneratorImpl(GeneratorContext context) {
    this.context = context;
  }

  protected void buildIntermediateCatchEvent(BpmnNode node, MethodSpec.Builder methodBuilder) {
    IntermediateCatchEvent event = node.as(IntermediateCatchEvent.class);

    Optional<EventDefinition> eventDefinition = event.getEventDefinitions().stream().findFirst();
    if (!eventDefinition.isPresent()) {
      return;
    } else if (eventDefinition.get() instanceof MessageEventDefinition) {
      methodBuilder.returns(IntermediateCatchEventHandler.class);
    } else if (eventDefinition.get() instanceof SignalEventDefinition) {
      methodBuilder.returns(IntermediateCatchEventHandler.class);
    } else if (eventDefinition.get() instanceof TimerEventDefinition) {
      methodBuilder.returns(JobHandler.class);
    } else {
      return;
    }
  }

  protected Optional<FieldSpec> buildIntermediateCatchEventHandler(BpmnNode node) {
    IntermediateCatchEvent event = node.as(IntermediateCatchEvent.class);

    Optional<EventDefinition> eventDefinition = event.getEventDefinitions().stream().findFirst();
    if (!eventDefinition.isPresent()) {
      return Optional.empty();
    } else if (eventDefinition.get() instanceof MessageEventDefinition) {
      return Optional.of(FieldSpec.builder(IntermediateCatchEventHandler.class, node.getLiteral(), Modifier.PRIVATE).build());
    } else if (eventDefinition.get() instanceof SignalEventDefinition) {
      return Optional.of(FieldSpec.builder(IntermediateCatchEventHandler.class, node.getLiteral(), Modifier.PRIVATE).build());
    } else if (eventDefinition.get() instanceof TimerEventDefinition) {
      return Optional.of(FieldSpec.builder(JobHandler.class, node.getLiteral(), Modifier.PRIVATE).build());
    } else {
      return Optional.empty();
    }
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

    builder.add("From: ");
    builder.add("$L: $L", startNode.getType(), startNode.getId());
    builder.add(", To: ");
    builder.add("$L: $L", endNode.getType(), endNode.getId());
    builder.add(", Length: ");;
    builder.add("$L", path.getFlowNodeIds().size());

    return builder.build();
  }

  protected void buildTestCase(TestCase testCase, TypeSpec.Builder classBuilder) {
    BpmnSupport bpmnSupport = context.getBpmnSupport();

    for (String flowNodeId : testCase.getPath().getFlowNodeIds()) {
      if (!bpmnSupport.has(flowNodeId)) {
        continue;
      }

      BpmnNode node = bpmnSupport.get(flowNodeId);

      if (node.isAsyncBefore()) {
        classBuilder.addField(JobHandler.class, String.format("%sBefore", node.getLiteral()), Modifier.PRIVATE);
      }
      if (node.isCallActivity()) {
        classBuilder.addField(CallActivityHandler.class, node.getLiteral(), Modifier.PRIVATE);
      }
      if (node.isExternalTask()) {
        classBuilder.addField(ExternalTaskHandler.class, node.getLiteral(), Modifier.PRIVATE);
      }
      if (node.isIntermediateCatchEvent()) {
        Optional<FieldSpec> fieldSpec = buildIntermediateCatchEventHandler(node);
        if (fieldSpec.isPresent()) {
          classBuilder.addField(fieldSpec.get());
        }
      }
      if (node.isUserTask()) {
        classBuilder.addField(UserTaskHandler.class, node.getLiteral(), Modifier.PRIVATE);
      }
      if (node.isAsyncAfter()) {
        classBuilder.addField(JobHandler.class, String.format("%sAfter", node.getLiteral()), Modifier.PRIVATE);
      }
    }

    classBuilder.addMethod(new Starting().apply(context, testCase));

    classBuilder.addMethod(new Execute().apply(context, testCase));
    classBuilder.addMethod(new GetBpmnResourceName().apply(context, testCase));
    classBuilder.addMethod(new GetEnd().apply(context, testCase));
    classBuilder.addMethod(new GetProcessDefinitionKey().apply(context, testCase));
    classBuilder.addMethod(new GetProcessEnginePlugins().apply(context));
    classBuilder.addMethod(new GetStart().apply(context, testCase));

    for (String flowNodeId : testCase.getPath().getFlowNodeIds()) {
      if (!bpmnSupport.has(flowNodeId)) {
        continue;
      }

      BpmnNode node = bpmnSupport.get(flowNodeId);

      String methodName = String.format("handle%s", StringUtils.capitalize(node.getLiteral()));
      String javadoc = String.format("%s: %s", node.getType(), node.getId());

      if (node.isAsyncBefore()) {
        methodName = methodName + "Before";
        javadoc = "before " + javadoc;
      }
      if (node.isAsyncAfter()) {
        methodName = methodName + "After";
        javadoc = "after " + javadoc;
      }

      MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
          .addJavadoc("Returns the handler for $L", javadoc)
          .addModifiers(Modifier.PUBLIC);

      if (node.isAsyncBefore()) {
        methodBuilder.returns(JobHandler.class);
        methodBuilder.addStatement("return $L", String.format("%sBefore", node.getLiteral()));
      }
      if (node.isCallActivity()) {
        methodBuilder.returns(CallActivityHandler.class);
        methodBuilder.addStatement("return $L", node.getLiteral());
      }
      if (node.isExternalTask()) {
        methodBuilder.returns(ExternalTaskHandler.class);
        methodBuilder.addStatement("return $L", node.getLiteral());
      }
      if (node.isIntermediateCatchEvent()) {
        buildIntermediateCatchEvent(node, methodBuilder);
        methodBuilder.addStatement("return $L", node.getLiteral());
      }
      if (node.isUserTask()) {
        methodBuilder.returns(UserTaskHandler.class);
        methodBuilder.addStatement("return $L", node.getLiteral());
      }
      if (node.isAsyncAfter()) {
        methodBuilder.returns(JobHandler.class);
        methodBuilder.addStatement("return $L", String.format("%sAfter", node.getLiteral()));
      }

      MethodSpec methodSpec = methodBuilder.build();
      if (methodSpec.returnType == TypeName.VOID) {
        continue;
      }

      classBuilder.addMethod(methodSpec);
    }
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
    Type superClass;
    if (context.isSpringEnabled()) {
      superClass = AbstractJUnit4SpringBasedTestRule.class;
    } else {
      superClass = AbstractJUnit4TestRule.class;
    }

    TypeSpec.Builder classBuilder = TypeSpec.classBuilder(buildTestCaseClassName(testCase))
        .addJavadoc(buildJavadoc(context, testCase))
        .superclass(superClass)
        .addModifiers(Modifier.PUBLIC);

    // build actual test case
    buildTestCase(testCase, classBuilder);

    JavaFile.Builder javaFileBuilder = JavaFile.builder(context.getPackageName(), classBuilder.build())
        .addStaticImport(ProcessEngineTests.class, "assertThat")
        .skipJavaLangImports(true);

    return javaFileBuilder.build();
  }

  @Override
  public JavaFile generateSpringConfiguration() {
    TypeSpec typeSpec = TypeSpec.classBuilder(GeneratorConstants.SPRING_CONFIGURATION)
        .superclass(AbstractConfiguration.class)
        .addAnnotation(Configuration.class)
        .addModifiers(Modifier.PUBLIC)
        .addMethod(new GetProcessEnginePlugins().apply(context))
        .build();
    
    return JavaFile.builder(context.getPackageName(), typeSpec)
        .skipJavaLangImports(true)
        .build();
  }
}
