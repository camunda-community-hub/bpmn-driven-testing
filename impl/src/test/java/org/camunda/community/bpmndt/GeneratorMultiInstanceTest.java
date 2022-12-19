package org.camunda.community.bpmndt;

import static com.google.common.truth.Truth.assertThat;
import static org.camunda.community.bpmndt.test.FieldSpecSubject.assertThat;
import static org.camunda.community.bpmndt.test.MethodSpecSubject.assertThat;
import static org.camunda.community.bpmndt.test.TypeSpecSubject.assertThat;

import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;
import org.camunda.community.bpmndt.api.CallActivityHandler;
import org.camunda.community.bpmndt.api.EventHandler;
import org.camunda.community.bpmndt.api.JobHandler;
import org.camunda.community.bpmndt.api.UserTaskHandler;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class GeneratorMultiInstanceTest {

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
    ctx.setMainResourcePath(TestPaths.advancedMultiInstance());
    ctx.setTestSourcePath(temporaryDirectory);

    ctx.setPackageName("org.example");

    result = generator.getResult();

    String fileName = testInfo.getTestMethod().get().getName().replace("test", "") + ".bpmn";
    bpmnFile = ctx.getMainResourcePath().resolve(StringUtils.uncapitalize(fileName));
  }

  /**
   * Tests the code generation for multi instance call activities - {@code apply} method is generated
   * differently.
   */
  @Test
  public void testCallActivity() {
    TypeName multiInstanceHandlerType = ClassName.get("org.example.callactivity", "MultiInstanceCallActivityHandler");

    generator.generateTestCases(ctx, bpmnFile);

    // BPMN process contains 2 test cases
    assertThat(result.getFiles()).hasSize(4);
    assertThat(result.getFiles().get(0).typeSpec.name).isEqualTo("TC_startEvent__endEvent");
    assertThat(result.getFiles().get(1).typeSpec.name).isEqualTo("MultiInstanceCallActivityHandler");
    assertThat(result.getFiles().get(2).typeSpec.name).isEqualTo("TC_startEvent__multiInstanceCallActivity");
    assertThat(result.getFiles().get(3).typeSpec.name).isEqualTo("MultiInstanceCallActivityHandler");

    TypeSpec typeSpec;

    typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(7);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("multiInstanceCallActivity");
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(multiInstanceHandlerType);

    assertThat(typeSpec.methodSpecs.get(6)).hasJavaDoc();
    assertThat(typeSpec.methodSpecs.get(6)).hasName("handleMultiInstanceCallActivity");
    assertThat(typeSpec.methodSpecs.get(6)).hasReturnType(multiInstanceHandlerType);

    JavaFile javaFile = result.getFiles().get(1);
    assertThat(javaFile.packageName).isEqualTo("org.example.callactivity");
    assertThat(javaFile.skipJavaLangImports).isTrue();
    assertThat(javaFile.typeSpec).isNotNull();

    typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec).hasFields(0);
    assertThat(typeSpec).hasJavaDoc();
    assertThat(typeSpec).hasMethods(3);

    assertThat(typeSpec.methodSpecs.get(1)).hasName("apply");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode(String.format("%s handler = getHandler(loopIndex)", TypeName.get(CallActivityHandler.class)));
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("registerCallActivityHandler(handler)");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("getHandlerBefore(loopIndex)");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("getHandlerAfter(loopIndex)");

    assertThat(typeSpec.methodSpecs.get(2)).hasName("createHandler");
    assertThat(typeSpec.methodSpecs.get(2)).hasReturnType(TypeName.get(CallActivityHandler.class));
    assertThat(typeSpec.methodSpecs.get(2)).containsCode(String.format("return new %s", TypeName.get(CallActivityHandler.class)));
  }

  @Test
  public void testCallActivityError() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(2);

    TypeSpec typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec).hasFields(0);
    assertThat(typeSpec).hasJavaDoc();
    assertThat(typeSpec).hasMethods(3);

    assertThat(typeSpec.methodSpecs.get(1)).hasName("apply");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode(String.format("%s handler = getHandler(loopIndex)", TypeName.get(CallActivityHandler.class)));
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("registerCallActivityHandler(handler)");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("getHandlerBefore(loopIndex)");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("getHandlerAfter(loopIndex)");

    assertThat(typeSpec.methodSpecs.get(2)).hasName("createHandler");
    assertThat(typeSpec.methodSpecs.get(2)).hasReturnType(TypeName.get(CallActivityHandler.class));
  }

  @Test
  public void testCallActivityTimer() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(2);

    TypeSpec typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasJavaDoc();
    assertThat(typeSpec).hasMethods(4);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("boundaryEventHandler");
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(TypeName.get(JobHandler.class));

    assertThat(typeSpec.methodSpecs.get(1)).hasName("apply");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode(String.format("%s handler = getHandler(loopIndex)", TypeName.get(CallActivityHandler.class)));
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("registerCallActivityHandler(handler)");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("if (handler.isWaitingForBoundaryEvent())");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(boundaryEventHandler)");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("getHandlerBefore(loopIndex)");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("getHandlerAfter(loopIndex)");

    assertThat(typeSpec.methodSpecs.get(2)).hasName("createHandler");
    assertThat(typeSpec.methodSpecs.get(2)).hasReturnType(TypeName.get(CallActivityHandler.class));
    assertThat(typeSpec.methodSpecs.get(3)).hasName("handleBoundaryEvent");
    assertThat(typeSpec.methodSpecs.get(3)).hasReturnType(TypeName.get(JobHandler.class));
  }

  /**
   * Tests the code generation for multi instance with other activities (no wait state) -
   * {@code apply} and {@code createHandler} method are not overridden.
   */
  @Test
  public void testManualTask() {
    // overridde auto built BPMN file path
    bpmnFile = ctx.getMainResourcePath().resolve("sequential.bpmn");

    TypeName multiInstanceHandlerType = ClassName.get("org.example.sequential", "MultiInstanceManualTaskHandler");

    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(2);

    TypeSpec typeSpec;

    typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(7);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("multiInstanceManualTask");
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(multiInstanceHandlerType);

    assertThat(typeSpec.methodSpecs.get(6)).hasJavaDoc();
    assertThat(typeSpec.methodSpecs.get(6)).hasName("handleMultiInstanceManualTask");
    assertThat(typeSpec.methodSpecs.get(6)).hasReturnType(multiInstanceHandlerType);

    JavaFile javaFile = result.getFiles().get(1);
    assertThat(javaFile.packageName).isEqualTo("org.example.sequential");
    assertThat(javaFile.skipJavaLangImports).isTrue();
    assertThat(javaFile.typeSpec).isNotNull();

    typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec).hasFields(0);
    assertThat(typeSpec).hasJavaDoc();
    assertThat(typeSpec).hasMethods(1);
  }

  /**
   * Tests the code generation for parallel multi instance - {@code isSequential} method is
   * overridden.
   */
  @Test
  public void testParallel() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(2);

    TypeSpec typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec).hasFields(0);
    assertThat(typeSpec).hasJavaDoc();
    assertThat(typeSpec).hasMethods(2);

    assertThat(typeSpec.methodSpecs.get(1)).hasName("isSequential");
    assertThat(typeSpec.methodSpecs.get(1)).hasReturnType(TypeName.BOOLEAN);
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("return false");
  }

  @Test
  public void testSequential() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(2);

    TypeSpec typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec).hasFields(0);
    assertThat(typeSpec).hasJavaDoc();
    assertThat(typeSpec).hasMethods(1);
  }

  /**
   * Tests the code generation for multi instance with wait state - {@code apply} and
   * {@code createHandler} method are overridden.
   */
  @Test
  public void testUserTask() {
    TypeName multiInstanceHandlerType = ClassName.get("org.example.usertask", "MultiInstanceUserTaskHandler");

    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(2);

    TypeSpec typeSpec;

    typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(7);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("multiInstanceUserTask");
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(multiInstanceHandlerType);

    assertThat(typeSpec.methodSpecs.get(6)).hasJavaDoc();
    assertThat(typeSpec.methodSpecs.get(6)).hasName("handleMultiInstanceUserTask");
    assertThat(typeSpec.methodSpecs.get(6)).hasReturnType(multiInstanceHandlerType);

    JavaFile javaFile = result.getFiles().get(1);
    assertThat(javaFile.packageName).isEqualTo("org.example.usertask");
    assertThat(javaFile.skipJavaLangImports).isTrue();
    assertThat(javaFile.typeSpec).isNotNull();

    typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec).hasFields(0);
    assertThat(typeSpec).hasJavaDoc();
    assertThat(typeSpec).hasMethods(3);

    assertThat(typeSpec.methodSpecs.get(1)).hasName("apply");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("getHandler(loopIndex)");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("getHandlerBefore(loopIndex)");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("getHandlerAfter(loopIndex)");

    assertThat(typeSpec.methodSpecs.get(2)).hasName("createHandler");
    assertThat(typeSpec.methodSpecs.get(2)).hasReturnType(TypeName.get(UserTaskHandler.class));
    assertThat(typeSpec.methodSpecs.get(2)).containsCode(String.format("return new %s", TypeName.get(UserTaskHandler.class)));
  }

  @Test
  public void testUserTaskError() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(2);

    TypeSpec typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec).hasFields(0);
    assertThat(typeSpec).hasJavaDoc();
    assertThat(typeSpec).hasMethods(3);

    assertThat(typeSpec.methodSpecs.get(1)).hasName("apply");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode(String.format("%s handler = getHandler(loopIndex)", TypeName.get(UserTaskHandler.class)));
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("getHandlerBefore(loopIndex)");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(handler)");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("getHandlerAfter(loopIndex)");

    assertThat(typeSpec.methodSpecs.get(2)).hasName("createHandler");
    assertThat(typeSpec.methodSpecs.get(2)).hasReturnType(TypeName.get(UserTaskHandler.class));
  }

  @Test
  public void testUserTaskMessage() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(2);

    TypeSpec typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasJavaDoc();
    assertThat(typeSpec).hasMethods(4);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("boundaryEventHandler");
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(TypeName.get(EventHandler.class));

    assertThat(typeSpec.methodSpecs.get(1)).hasName("apply");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode(String.format("%s handler = getHandler(loopIndex)", TypeName.get(UserTaskHandler.class)));
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("if (handler.isWaitingForBoundaryEvent())");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(boundaryEventHandler)");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("getHandlerBefore(loopIndex)");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("getHandlerAfter(loopIndex)");

    assertThat(typeSpec.methodSpecs.get(2)).hasName("createHandler");
    assertThat(typeSpec.methodSpecs.get(2)).hasReturnType(TypeName.get(UserTaskHandler.class));
    assertThat(typeSpec.methodSpecs.get(3)).hasName("handleBoundaryEvent");
    assertThat(typeSpec.methodSpecs.get(3)).hasReturnType(TypeName.get(EventHandler.class));
  }
}
