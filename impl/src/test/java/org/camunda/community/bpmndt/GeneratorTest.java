package org.camunda.community.bpmndt;

import static com.google.common.truth.Truth.assertThat;
import static org.camunda.community.bpmndt.test.MethodSpecSubject.assertThat;
import static org.camunda.community.bpmndt.test.TypeSpecSubject.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.community.bpmndt.api.AbstractJUnit5TestCase;
import org.camunda.community.bpmndt.api.cfg.SpringConfiguration;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

public class GeneratorTest {

  @TempDir
  private Path temporaryDirectory;

  private GeneratorContext ctx;
  private GeneratorResult result;
  private Generator generator;

  private Path bpmnFile;

  @BeforeEach
  public void setUp(TestInfo testInfo) {
    generator = new Generator();

    ctx = new GeneratorContext();
    ctx.setBasePath(temporaryDirectory.getRoot());
    ctx.setMainResourcePath(TestPaths.simple().resolve("special"));
    ctx.setTestSourcePath(temporaryDirectory);

    ctx.setPackageName("org.example");

    result = generator.getResult();

    String fileName = testInfo.getTestMethod().get().getName().replace("test", "") + ".bpmn";
    bpmnFile = ctx.getMainResourcePath().resolve(StringUtils.uncapitalize(fileName));
  }

  @Test
  public void testToJavaLiteral() {
    assertThat(Generator.toJavaLiteral("Happy Path")).isEqualTo("happy_path");
    assertThat(Generator.toJavaLiteral("Happy-Path")).isEqualTo("happy_path");
    assertThat(Generator.toJavaLiteral("Happy Path!")).isEqualTo("happy_path_");
    assertThat(Generator.toJavaLiteral("startEvent__endEvent")).isEqualTo("startevent__endevent");
    assertThat(Generator.toJavaLiteral("123\nABC")).isEqualTo("_123_abc");
    assertThat(Generator.toJavaLiteral("New")).isEqualTo("_new");
  }

  @Test
  public void testToLiteral() {
    assertThat(Generator.toLiteral("Happy Path")).isEqualTo("Happy_Path");
    assertThat(Generator.toLiteral("Happy-Path")).isEqualTo("Happy_Path");
    assertThat(Generator.toLiteral("Happy Path!")).isEqualTo("Happy_Path_");
    assertThat(Generator.toLiteral("startEvent__endEvent")).isEqualTo("startEvent__endEvent");
    assertThat(Generator.toLiteral("123\nABC")).isEqualTo("123_ABC");
  }

  /**
   * Should generate the first test case and skip the second. Since the second test case has the same
   * name as the first, which is not allowed.
   */
  @Test
  public void testDuplicateTestCaseNames() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);
    assertThat(result.getFiles().get(0).packageName).isEqualTo("org.example.duplicatetestcasenames");

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasName("TC_startEvent__endEvent");
  }

  @Test
  public void testEmpty() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).isEmpty();
  }

  @Test
  public void testIncomplete() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).isEmpty();
  }

  @Test
  public void testInvalid() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).isEmpty();
  }

  @Test
  public void testHappyPath() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);
    assertThat(result.getFiles().get(0).packageName).isEqualTo("org.example.happypath");

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasName("TC_Happy_Path");

    ClassName rawType = ClassName.get(AbstractJUnit5TestCase.class);
    ClassName typeArgument = ClassName.bestGuess(typeSpec.name);

    assertThat(typeSpec.superclass).isInstanceOf(ParameterizedTypeName.class);
    assertThat(((ParameterizedTypeName) typeSpec.superclass).rawType).isEqualTo(rawType);
    assertThat(((ParameterizedTypeName) typeSpec.superclass).typeArguments.get(0)).isEqualTo(typeArgument);
  }

  @Test
  public void testNoTestCases() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(0);
  }

  /**
   * Tests the complete generation for JUnit 5.
   */
  @Test
  public void testGenerate() {
    generator.generate(ctx);

    Predicate<String> isFile = (className) -> {
      return Files.isRegularFile(ctx.getTestSourcePath().resolve(className));
    };

    // test cases
    assertThat(isFile.test("org/example/duplicatetestcasenames/TC_startEvent__endEvent.java")).isTrue();
    assertThat(isFile.test("org/example/empty/TC_empty.java")).isFalse();
    assertThat(isFile.test("org/example/happypath/TC_Happy_Path.java")).isTrue();
    assertThat(isFile.test("org/example/incomplete/TC_incomplete.java")).isFalse();
    assertThat(isFile.test("org/example/invalid/TC_startEvent__endEvent.java")).isFalse();

    // should not exist, since the BPMN process provides no test cases
    assertThat(isFile.test("org/example/notestcases/TC_startEvent__endEvent.java")).isFalse();

    // API classes
    assertThat(isFile.test("org/camunda/community/bpmndt/api/AbstractJUnit5TestCase.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/api/AbstractTestCase.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/api/CallActivityDefinition.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/api/CallActivityHandler.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/api/EventHandler.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/api/ExternalTaskHandler.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/api/JobHandler.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/api/MultiInstanceHandler.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/api/TestCaseInstance.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/api/TestCaseExecutor.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/api/UserTaskHandler.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/api/cfg/BpmndtParseListener.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/api/cfg/BpmndtProcessEnginePlugin.java")).isTrue();
  }

  /**
   * Tests the complete generation with Spring enabled.
   */
  @Test
  public void testGenerateSpringEnabled() {
    ctx.setSpringEnabled(true);

    generator.generate(ctx);

    Predicate<String> isFile = (className) -> {
      return Files.isRegularFile(ctx.getTestSourcePath().resolve(className));
    };

    // test cases
    assertThat(isFile.test("org/example/duplicatetestcasenames/TC_startEvent__endEvent.java")).isTrue();
    assertThat(isFile.test("org/example/empty/TC_empty.java")).isFalse();
    assertThat(isFile.test("org/example/happypath/TC_Happy_Path.java")).isTrue();
    assertThat(isFile.test("org/example/incomplete/TC_incomplete.java")).isFalse();
    assertThat(isFile.test("org/example/invalid/TC_startEvent__endEvent.java")).isFalse();

    // should not exist, since the BPMN process provides no test cases
    assertThat(isFile.test("org/example/notestcases/TC_startEvent__endEvent.java")).isFalse();

    // Spring configuration
    assertThat(isFile.test("org/example/BpmndtConfiguration.java")).isTrue();

    // API classes
    assertThat(isFile.test("org/camunda/community/bpmndt/api/AbstractJUnit5TestCase.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/api/AbstractTestCase.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/api/CallActivityDefinition.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/api/CallActivityHandler.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/api/EventHandler.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/api/ExternalTaskHandler.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/api/JobHandler.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/api/MultiInstanceHandler.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/api/MultiInstanceScopeHandler.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/api/TestCaseInstance.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/api/TestCaseExecutor.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/api/UserTaskHandler.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/api/cfg/BpmndtParseListener.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/api/cfg/BpmndtProcessEnginePlugin.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/api/cfg/SpringConfiguration.java")).isTrue();
  }

  @Test
  public void testGenerateSpringConfiguration() {
    List<String> processEnginePluginNames = new LinkedList<>();
    processEnginePluginNames.add("org.example.Abc");
    processEnginePluginNames.add("ExamplePlugin");
    processEnginePluginNames.add("org.camunda.Xzy");
    ctx.setProcessEnginePluginNames(processEnginePluginNames);

    generator.generateSpringConfiguration(ctx);
    assertThat(result.getAdditionalFiles()).hasSize(1);
    assertThat(result.getAdditionalFiles().get(0).packageName).isEqualTo("org.example");

    TypeSpec typeSpec = result.getAdditionalFiles().get(0).typeSpec;
    assertThat(typeSpec).hasName("BpmndtConfiguration");
    assertThat(typeSpec.superclass).isEqualTo(ClassName.get(SpringConfiguration.class));
    assertThat(typeSpec).hasMethods(1);

    MethodSpec methodSpec = typeSpec.methodSpecs.get(0);
    assertThat(methodSpec).hasName("getProcessEnginePlugins");

    String expected = "java.util.List<%s> processEnginePlugins = new java.util.LinkedList<>()";
    assertThat(methodSpec).containsCode(String.format(expected, ClassName.get(ProcessEnginePlugin.class)));
    assertThat(methodSpec).containsCode("processEnginePlugins.add(new org.example.Abc())");
    assertThat(methodSpec).notContainsCode("processEnginePlugins.add(new ExamplePlugin())");
    assertThat(methodSpec).containsCode("processEnginePlugins.add(new org.camunda.Xzy())");
    assertThat(methodSpec).containsCode("return processEnginePlugins");
  }

  @Test
  public void testGenerateSpringConfigurationEmptyProcessEnginePlugins() {
    generator.generateSpringConfiguration(ctx);
    assertThat(result.getAdditionalFiles()).hasSize(1);

    TypeSpec typeSpec = result.getAdditionalFiles().get(0).typeSpec;
    assertThat(typeSpec).hasName("BpmndtConfiguration");
    assertThat(typeSpec.superclass).isEqualTo(ClassName.get(SpringConfiguration.class));
    assertThat(typeSpec).hasMethods(0);
  }
}
