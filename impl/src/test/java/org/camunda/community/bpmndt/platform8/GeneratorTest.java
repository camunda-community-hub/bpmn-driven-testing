package org.camunda.community.bpmndt.platform8;

import static com.google.common.truth.Truth.assertThat;
import static org.camunda.community.bpmndt.test.TypeSpecSubject.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.camunda.community.bpmndt.GeneratorContextBase;
import org.camunda.community.bpmndt.GeneratorResult;
import org.camunda.community.bpmndt.platform8.api.AbstractJUnit5TestCase;
import org.camunda.community.bpmndt.test.Platform8TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;

public class GeneratorTest {

  @TempDir
  private Path temporaryDirectory;

  private GeneratorContextBase ctx;
  private GeneratorResult result;
  private Generator generator;

  private Path bpmnFile;

  @BeforeEach
  public void setUp(TestInfo testInfo) {
    generator = new Generator();

    ctx = new GeneratorContextBase();
    ctx.setBasePath(temporaryDirectory.getRoot());
    ctx.setMainResourcePath(Platform8TestPaths.simple().resolve("special"));
    ctx.setTestSourcePath(temporaryDirectory);

    ctx.setPackageName("org.example");

    result = generator.getResult();

    String fileName = testInfo.getTestMethod().orElseThrow(NoSuchElementException::new).getName().replace("test", "") + ".bpmn";
    bpmnFile = ctx.getMainResourcePath().resolve(StringUtils.uncapitalize(fileName));
  }

  /**
   * Should generate the first test case and skip the second. Since the second test case has the same name as the first, which is not allowed.
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

    assertThat(typeSpec.superclass).isInstanceOf(ClassName.class);
    assertThat(((ClassName) typeSpec.superclass)).isEqualTo(ClassName.get(AbstractJUnit5TestCase.class));
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

    Predicate<String> isFile = (className) -> Files.isRegularFile(ctx.getTestSourcePath().resolve(className));

    // test cases
    assertThat(isFile.test("org/example/duplicatetestcasenames/TC_startEvent__endEvent.java")).isTrue();
    assertThat(isFile.test("org/example/empty/TC_empty.java")).isFalse();
    assertThat(isFile.test("org/example/happypath/TC_Happy_Path.java")).isTrue();
    assertThat(isFile.test("org/example/incomplete/TC_incomplete.java")).isFalse();
    assertThat(isFile.test("org/example/invalid/TC_startEvent__endEvent.java")).isFalse();

    // should not exist, since the BPMN process provides no test cases
    assertThat(isFile.test("org/example/notestcases/TC_startEvent__endEvent.java")).isFalse();

    // API classes
    assertThat(isFile.test("org/camunda/community/bpmndt/platform8/api/AbstractJUnit5TestCase.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/platform8/api/AbstractTestCase.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/platform8/api/JobHandler.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/platform8/api/TestCaseExecutor.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/platform8/api/TestCaseInstance.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/platform8/api/TestCaseInstanceElement.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/platform8/api/TestCaseInstanceMemo.java")).isTrue();
    assertThat(isFile.test("org/camunda/community/bpmndt/platform8/api/UserTaskHandler.java")).isTrue();
  }
}
