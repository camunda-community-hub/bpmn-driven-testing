package org.camunda.community.bpmndt.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import org.camunda.community.bpmndt.GeneratorContext;
import org.camunda.community.bpmndt.GeneratorResult;
import org.camunda.community.bpmndt.api.AbstractTestCase;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

/**
 * Generates a Java file, containing the simulate sub process resource BPMN as string constant.
 */
public class GenerateSimulateSubProcessResource implements Consumer<GeneratorContext> {

  private final GeneratorResult result;

  public GenerateSimulateSubProcessResource(GeneratorResult result) {
    this.result = result;
  }

  @Override
  public void accept(GeneratorContext ctx) {
    var classBuilder = TypeSpec.classBuilder("SimulateSubProcessResource")
        .addModifiers(Modifier.PUBLIC);

    var valueBuilder = CodeBlock.builder();

    boolean include = true;

    var lines = getSimulateSubProcessResource();
    for (int i = 0; i < lines.size(); i++) {
      var line = lines.get(i);

      // exclude BPMN diagram
      if (line.contains("bpmndi:BPMNDiagram")) {
        include = !include;
        continue;
      }

      if (!include) {
        continue;
      }

      if (i == 0) {
        valueBuilder.add("$S", line);
      } else {
        valueBuilder.add("\n+ $S", line);
      }
    }

    var valueFieldBuilder = FieldSpec.builder(String.class, "VALUE", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
        .initializer(valueBuilder.build());

    classBuilder.addField(valueFieldBuilder.build());

    var javaFile = JavaFile.builder(AbstractTestCase.class.getPackageName(), classBuilder.build())
        .skipJavaLangImports(true)
        .build();

    result.addFile(javaFile);
  }

  private List<String> getSimulateSubProcessResource() {
    try (InputStream resource = this.getClass().getResourceAsStream("/simulate-sub-process.bpmn")) {
      return new BufferedReader(new InputStreamReader(Objects.requireNonNull(resource))).lines().collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException("failed to read simulate sub process BPMN resource", e);
    }
  }
}
