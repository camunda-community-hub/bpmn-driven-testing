package org.camunda.community.bpmndt;

import static org.camunda.community.bpmndt.test.ContainsCode.containsCode;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.community.bpmndt.api.AbstractJUnit4TestCase;
import org.camunda.community.bpmndt.api.cfg.SpringConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.mockito.Mockito;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class GeneratorTest {

  @Rule
  public TestName testName = new TestName();
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder(new File("./target"));

  private GeneratorContext ctx;
  private GeneratorResult result;
  private Generator generator;

  private Path bpmnFile;

  @Before
  public void setUp() {
    generator = new Generator(Mockito.mock(Log.class));

    ctx = new GeneratorContext();
    ctx.setBasePath(Paths.get("."));
    ctx.setMainResourcePath(Paths.get("./src/test/resources"));
    ctx.setTestSourcePath(temporaryFolder.getRoot().toPath());

    ctx.setPackageName("org.example");

    result = generator.getResult();

    String fileName = testName.getMethodName().replace("test", "") + ".bpmn";
    bpmnFile = ctx.getMainResourcePath().resolve("bpmn").resolve(StringUtils.uncapitalize(fileName));
  }

  /**
   * Should generate the first test case and skip the second. Since the second test case has the same
   * name as the first, which is not allowed.
   */
  @Test
  public void testDuplicateTestCaseNames() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));
    assertThat(result.getFiles().get(0).packageName, equalTo("org.example.duplicate_test_case_names"));

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.name, equalTo("TC_startEvent__endEvent"));
  }

  @Test
  public void testEmpty() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));
    assertThat(result.getFiles().get(0).packageName, equalTo("org.example.empty"));

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.name, equalTo("TC_empty"));

    TypeName superclass = ClassName.get(AbstractJUnit4TestCase.class);
    assertThat(typeSpec.superclass, equalTo(superclass));
    assertThat(typeSpec.fieldSpecs, hasSize(0));
    assertThat(typeSpec.methodSpecs, hasSize(6));
    assertThat(typeSpec.methodSpecs.get(0).name, equalTo("starting"));
    assertThat(typeSpec.methodSpecs.get(1).name, equalTo("execute"));

    containsCode(typeSpec.methodSpecs.get(0)).contains("throw new java.lang.RuntimeException(\"Path is empty\");");
    containsCode(typeSpec.methodSpecs.get(1)).contains("throw new java.lang.RuntimeException(\"Path is empty\");");
  }
  
  @Test
  public void testIncomplete() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));
    assertThat(result.getFiles().get(0).packageName, equalTo("org.example.incomplete"));

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.name, equalTo("TC_incomplete"));

    TypeName superclass = ClassName.get(AbstractJUnit4TestCase.class);
    assertThat(typeSpec.superclass, equalTo(superclass));
    assertThat(typeSpec.fieldSpecs, hasSize(0));
    assertThat(typeSpec.methodSpecs, hasSize(6));
    assertThat(typeSpec.methodSpecs.get(0).name, equalTo("starting"));
    assertThat(typeSpec.methodSpecs.get(1).name, equalTo("execute"));
    
    containsCode(typeSpec.methodSpecs.get(0)).contains("throw new java.lang.RuntimeException(\"Path is incomplete\");");
    containsCode(typeSpec.methodSpecs.get(1)).contains("throw new java.lang.RuntimeException(\"Path is incomplete\");");
  }

  @Test
  public void testInvalid() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));
    assertThat(result.getFiles().get(0).packageName, equalTo("org.example.invalid"));

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.name, equalTo("TC_startEvent__endEvent"));

    TypeName superclass = ClassName.get(AbstractJUnit4TestCase.class);
    assertThat(typeSpec.superclass, equalTo(superclass));
    assertThat(typeSpec.fieldSpecs, hasSize(0));
    assertThat(typeSpec.methodSpecs, hasSize(6));
    assertThat(typeSpec.methodSpecs.get(0).name, equalTo("starting"));
    assertThat(typeSpec.methodSpecs.get(1).name, equalTo("execute"));

    containsCode(typeSpec.methodSpecs.get(0)).contains("// Not existing flow nodes");
    containsCode(typeSpec.methodSpecs.get(0)).contains("// a");
    containsCode(typeSpec.methodSpecs.get(0)).contains("// b");
    containsCode(typeSpec.methodSpecs.get(0)).contains("throw new java.lang.RuntimeException(\"Path is invalid\");");

    containsCode(typeSpec.methodSpecs.get(1)).contains("// Not existing flow nodes");
    containsCode(typeSpec.methodSpecs.get(1)).contains("// a");
    containsCode(typeSpec.methodSpecs.get(1)).contains("// b");
    containsCode(typeSpec.methodSpecs.get(1)).contains("throw new java.lang.RuntimeException(\"Path is invalid\");");
  }

  @Test
  public void testHappyPath() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));
    assertThat(result.getFiles().get(0).packageName, equalTo("org.example.happy_path"));

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.name, equalTo("TC_Happy_Path"));
  }

  @Test
  public void testNoTestCases() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(0));
  }

  /**
   * Tests the complete generation.
   */
  @Test
  public void testGenerate() {
    generator.generate(ctx);
    
    Predicate<String> isFile = (className) -> {
      return Files.isRegularFile(ctx.getTestSourcePath().resolve(className));
    };
    
    // test cases
    assertThat(isFile.test("org/example/duplicate_test_case_names/TC_startEvent__endEvent.java"), is(true));
    assertThat(isFile.test("org/example/empty/TC_empty.java"), is(true));
    assertThat(isFile.test("org/example/happy_path/TC_Happy_Path.java"), is(true));
    assertThat(isFile.test("org/example/incomplete/TC_incomplete.java"), is(true));
    assertThat(isFile.test("org/example/invalid/TC_startEvent__endEvent.java"), is(true));

    // should not exist, since the BPMN process provides no test cases
    assertThat(isFile.test("org/example/no_test_cases/TC_startEvent__endEvent.java"), is(false));

    // API classes
    assertThat(isFile.test("org/camunda/community/bpmndt/api/AbstractJUnit4TestCase.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/CallActivityDefinition.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/CallActivityHandler.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/EventHandler.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/ExternalTaskHandler.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/JobHandler.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/MultiInstanceHandler.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/TestCaseInstance.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/TestCaseExecutor.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/UserTaskHandler.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/cfg/BpmndtParseListener.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/cfg/BpmndtProcessEnginePlugin.java"), is(true));
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
    assertThat(isFile.test("org/example/duplicate_test_case_names/TC_startEvent__endEvent.java"), is(true));
    assertThat(isFile.test("org/example/empty/TC_empty.java"), is(true));
    assertThat(isFile.test("org/example/happy_path/TC_Happy_Path.java"), is(true));
    assertThat(isFile.test("org/example/incomplete/TC_incomplete.java"), is(true));
    assertThat(isFile.test("org/example/invalid/TC_startEvent__endEvent.java"), is(true));

    // should not exist, since the BPMN process provides no test cases
    assertThat(isFile.test("org/example/no_test_cases/TC_startEvent__endEvent.java"), is(false));

    // Spring configuration
    assertThat(isFile.test("org/example/BpmndtConfiguration.java"), is(true));

    // API classes
    assertThat(isFile.test("org/camunda/community/bpmndt/api/AbstractJUnit4TestCase.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/CallActivityDefinition.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/CallActivityHandler.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/EventHandler.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/ExternalTaskHandler.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/JobHandler.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/MultiInstanceHandler.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/TestCaseInstance.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/TestCaseExecutor.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/UserTaskHandler.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/cfg/BpmndtParseListener.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/cfg/BpmndtProcessEnginePlugin.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/cfg/SpringConfiguration.java"), is(true));
  }

  @Test
  public void testGenerateSpringConfiguration() {
    List<String> processEnginePluginNames = new LinkedList<>();
    processEnginePluginNames.add("org.example.Abc");
    processEnginePluginNames.add("ExamplePlugin");
    processEnginePluginNames.add("org.camunda.Xzy");
    ctx.setProcessEnginePluginNames(processEnginePluginNames);

    generator.generateSpringConfiguration(ctx);
    assertThat(result.getAdditionalFiles(), hasSize(1));
    assertThat(result.getAdditionalFiles().get(0).packageName, equalTo("org.example"));

    TypeSpec typeSpec = result.getAdditionalFiles().get(0).typeSpec;
    assertThat(typeSpec.name, equalTo("BpmndtConfiguration"));
    assertThat(typeSpec.superclass, equalTo(ClassName.get(SpringConfiguration.class)));
    assertThat(typeSpec.methodSpecs, hasSize(1));

    MethodSpec methodSpec = typeSpec.methodSpecs.get(0);
    assertThat(methodSpec.name, equalTo("getProcessEnginePlugins"));

    String expected = "java.util.List<%s> processEnginePlugins = new java.util.LinkedList<>()";
    containsCode(methodSpec).contains(String.format(expected, ClassName.get(ProcessEnginePlugin.class)));
    containsCode(methodSpec).contains("processEnginePlugins.add(new org.example.Abc())");
    containsCode(methodSpec).notContains("processEnginePlugins.add(new ExamplePlugin())");
    containsCode(methodSpec).contains("processEnginePlugins.add(new org.camunda.Xzy())");
    containsCode(methodSpec).contains("return processEnginePlugins");
  }

  @Test
  public void testGenerateSpringConfigurationEmptyProcessEnginePlugins() {
    generator.generateSpringConfiguration(ctx);
    assertThat(result.getAdditionalFiles(), hasSize(1));

    TypeSpec typeSpec = result.getAdditionalFiles().get(0).typeSpec;
    assertThat(typeSpec.name, equalTo("BpmndtConfiguration"));
    assertThat(typeSpec.superclass, equalTo(ClassName.get(SpringConfiguration.class)));
    assertThat(typeSpec.methodSpecs, hasSize(0));
  }
}
