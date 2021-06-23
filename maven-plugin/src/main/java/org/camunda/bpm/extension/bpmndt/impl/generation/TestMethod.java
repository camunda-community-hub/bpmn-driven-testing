package org.camunda.bpm.extension.bpmndt.impl.generation;

import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.AFTER;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.BEFORE;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.BUSINESS_KEY;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.CALL_ACTIVITY_RULE;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.PROCESS_ENGINE_RULE;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.PROCESS_INSTANCE;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.TEST_PATH;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.VARIABLES;

import java.util.function.BiFunction;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.extension.bpmndt.BpmnNode;
import org.camunda.bpm.extension.bpmndt.BpmnSupport;
import org.camunda.bpm.extension.bpmndt.GeneratorContext;
import org.camunda.bpm.extension.bpmndt.type.Path;
import org.camunda.bpm.extension.bpmndt.type.TestCase;
import org.junit.Test;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;

/**
 * Function that builds the actual {@link Test} method, which starts a process instance at the start
 * node and performs all the flow node type related actions and assertions.
 */
public class TestMethod implements BiFunction<GeneratorContext, TestCase, MethodSpec> {

  @Override
  public MethodSpec apply(GeneratorContext context, TestCase testCase) {
    BpmnSupport bpmnSupport = context.getBpmnSupport();

    AnnotationSpec deploymentAnnotation = AnnotationSpec.builder(Deployment.class)
        .addMember("resources", "{$S}", context.getBpmnResourceName())
        .build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder(TEST_PATH)
        .addAnnotation(Test.class)
        .addAnnotation(deploymentAnnotation)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addStatement("$T.init($L.getProcessEngine())", ProcessEngineTests.class, PROCESS_ENGINE_RULE)
        .addCode("\n// register call activity rule at parse listener\n")
        .addStatement("$L.register($L)", CALL_ACTIVITY_RULE, PROCESS_ENGINE_RULE);

    Path path = testCase.getPath();

    for (String flowNodeId : path.getFlowNodeIds()) {
      BpmnNode node = bpmnSupport.get(flowNodeId);

      if (node.isCallActivity()) {
        // register callbacks for input/output mapping
        builder.addCode("\n// $L: $L\n", node.getType(), node.getId());

        Object[] args = {CALL_ACTIVITY_RULE, node.getId(), node.getLiteral()};
        builder.addStatement("$L.callbackI.put($S, this::$L_input)", args);
        builder.addStatement("$L.callbackO.put($S, this::$L_output)", args);
      }
    }

    builder.addCode("\n")
        .addStatement("$T $L = $T.createVariables()", VariableMap.class, VARIABLES, Variables.class)
        .addStatement("$T $L = $L($L)", String.class, BUSINESS_KEY, BEFORE, VARIABLES)
        .addCode("\n")
        .addCode("$L = $L.getRuntimeService()\n", PROCESS_INSTANCE, PROCESS_ENGINE_RULE)
        .addCode("    .createProcessInstanceByKey($S)\n", bpmnSupport.getProcessId())
        .addCode("    .businessKey($L)", BUSINESS_KEY)
        .addCode("    .setVariables($L)\n", VARIABLES)
        .addCode("    .startBeforeActivity($S)\n", path.getStart())
        .addCode("    .execute();\n\n")
        .addStatement("assertThat($L).isStarted()", PROCESS_INSTANCE);

    // build flow node specific code blocks
    for (String flowNodeId : path.getFlowNodeIds()) {
      BpmnNode node = bpmnSupport.get(flowNodeId);

      builder.addCode("\n// $L: $L\n", node.getType(), node.getId());

      if (node.isAsyncBefore()) {
        builder.addCode(new HandleAsyncBeforeCodeBlock().apply(node));
      }

      if (node.isExternalTask()) {
        builder.addCode(new HandleExternalTaskCodeBlock().apply(node));
      }
      if (node.isIntermediateCatchEvent()) {
        builder.addCode(new HandleIntermediateCatchEventCodeBlock().apply(node));
      }
      if (node.isUserTask()) {
        builder.addCode(new HandleUserTaskCodeBlock().apply(node));
      }

      if (node.isAsyncAfter()) {
        builder.addCode(new HandleAsyncAfterCodeBlock().apply(node));
      }

      builder.addStatement("assertThat($L).hasPassed($S)", PROCESS_INSTANCE, flowNodeId);
    }

    builder.addCode("\n");
    builder.addStatement("$L()", AFTER);

    return builder.build();
  }
}
