package org.camunda.community.bpmndt;

import static com.google.common.truth.Truth.assertThat;
import static org.camunda.community.bpmndt.test.FieldSpecSubject.assertThat;
import static org.camunda.community.bpmndt.test.MethodSpecSubject.assertThat;
import static org.camunda.community.bpmndt.test.TypeSpecSubject.assertThat;

import java.nio.file.Path;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;
import org.camunda.community.bpmndt.api.AbstractJUnit5TestCase;
import org.camunda.community.bpmndt.strategy.DefaultStrategy;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

import com.squareup.javapoet.ClassName;

class GeneratorAdvancedMultiInstanceTest {

  @TempDir
  private Path temporaryDirectory;

  private GeneratorContext ctx;
  private GeneratorResult result;
  private Generator generator;

  private Path bpmnFile;

  @BeforeEach
  void setUp(TestInfo testInfo) {
    generator = new Generator();

    ctx = new GeneratorContext();
    ctx.setBasePath(temporaryDirectory.getRoot());
    ctx.setMainResourcePath(TestPaths.advancedMultiInstance());
    ctx.setTestSourcePath(temporaryDirectory);

    ctx.setPackageName("org.example");

    result = generator.getResult();

    var fileName = testInfo.getTestMethod().orElseThrow(NoSuchElementException::new).getName().replace("test", "") + ".bpmn";
    bpmnFile = ctx.getMainResourcePath().resolve(StringUtils.uncapitalize(fileName));
  }

  @Test
  void testScopeErrorEndEvent() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(2);

    // TC_None
    var javaFile = result.getFiles().get(0);
    assertThat(javaFile.packageName).isEqualTo("org.example.scopeerrorendevent");
    assertThat(javaFile.skipJavaLangImports).isTrue();
    assertThat(javaFile.typeSpec).isNotNull();

    var typeSpec = javaFile.typeSpec;
    assertThat(typeSpec).hasName("TC_None");

    assertThat(typeSpec.superclass).isInstanceOf(ClassName.class);
    assertThat(typeSpec.superclass).isEqualTo(ClassName.get(AbstractJUnit5TestCase.class));

    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(8);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("subProcess");
    assertThat(typeSpec.fieldSpecs.get(0).type).isEqualTo(DefaultStrategy.CUSTOM_MULTI_INSTANCE);

    assertThat(typeSpec.methodSpecs.get(7)).hasName("handleSubProcess");
    assertThat(typeSpec.methodSpecs.get(7)).hasReturnType(DefaultStrategy.CUSTOM_MULTI_INSTANCE);

    var expected = "subProcess = new %s(subProcessElement);";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format(expected, DefaultStrategy.CUSTOM_MULTI_INSTANCE));

    expected = "subProcessElement.id = \"subProcess\";";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);
    expected = "subProcessElement.sequential = true;";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);

    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(flowScopeKey, \"startEvent\");");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(flowScopeKey, subProcess);");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassedMultiInstance(flowScopeKey, \"subProcess\");");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(flowScopeKey, \"join\");");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(flowScopeKey, \"endEvent\");");

    // TC_Error
    javaFile = result.getFiles().get(1);
    assertThat(javaFile.packageName).isEqualTo("org.example.scopeerrorendevent");
    assertThat(javaFile.skipJavaLangImports).isTrue();
    assertThat(javaFile.typeSpec).isNotNull();

    typeSpec = javaFile.typeSpec;
    assertThat(typeSpec).hasName("TC_Error");

    assertThat(typeSpec.superclass).isInstanceOf(ClassName.class);
    assertThat(typeSpec.superclass).isEqualTo(ClassName.get(AbstractJUnit5TestCase.class));

    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(8);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("subProcess");
    assertThat(typeSpec.fieldSpecs.get(0).type).isEqualTo(DefaultStrategy.CUSTOM_MULTI_INSTANCE);

    assertThat(typeSpec.methodSpecs.get(7)).hasName("handleSubProcess");
    assertThat(typeSpec.methodSpecs.get(7)).hasReturnType(DefaultStrategy.CUSTOM_MULTI_INSTANCE);

    expected = "subProcess = new %s(subProcessElement);";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format(expected, DefaultStrategy.CUSTOM_MULTI_INSTANCE));

    expected = "subProcessElement.id = \"subProcess\";";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);
    expected = "subProcessElement.sequential = true;";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);

    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(flowScopeKey, \"startEvent\");");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(flowScopeKey, subProcess);");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasTerminatedMultiInstance(flowScopeKey, \"subProcess\");");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(flowScopeKey, \"subProcessErrorBoundaryEvent\");");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(flowScopeKey, \"join\");");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(flowScopeKey, \"endEvent\");");
  }
}
