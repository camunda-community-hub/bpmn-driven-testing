package org.camunda.bpm.extension.bpmndt.impl.generation;

import java.util.function.Function;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.extension.bpmndt.BpmnNode;

import com.squareup.javapoet.MethodSpec;

public class HandleCallActivityInput implements Function<BpmnNode, MethodSpec> {

  @Override
  public MethodSpec apply(BpmnNode node) {
    return MethodSpec.methodBuilder(String.format("%s_input", node.getLiteral()))
        .addJavadoc("Overwrite to assert input mapping of call activity $L.<br>", node.getId())
        .addJavadoc("Use {@code callActivityRule} to access:\n")
        .addJavadoc("<ul>\n")
        .addJavadoc("  <li>binding</li>\n")
        .addJavadoc("  <li>businessKey</li>\n")
        .addJavadoc("  <li>definitionKey</li>\n")
        .addJavadoc("  <li>tenantId</li>\n")
        .addJavadoc("  <li>version</li>\n")
        .addJavadoc("  <li>versionTag</li>\n")
        .addJavadoc("</ul>\n\n")
        .addJavadoc("Set variables to mock the behavior of the called sub instance.")
        .addModifiers(Modifier.PROTECTED)
        .addParameter(VariableScope.class, "subInstance")
        .build();
  }
}
