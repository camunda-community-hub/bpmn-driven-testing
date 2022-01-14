package org.camunda.community.bpmndt;

import static org.camunda.community.bpmndt.test.ContainsCode.containsCode;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.camunda.community.bpmndt.api.CallActivityHandler;
import org.camunda.community.bpmndt.api.EventHandler;
import org.camunda.community.bpmndt.api.JobHandler;
import org.camunda.community.bpmndt.api.UserTaskHandler;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.mockito.Mockito;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class GeneratorMultiInstanceTest {

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
    ctx.setH2Version2(true);
    ctx.setMainResourcePath(Paths.get("./src/test/it/advanced-multi-instance/src/main/resources"));
    ctx.setTestSourcePath(temporaryFolder.getRoot().toPath());

    ctx.setPackageName("org.example");

    result = generator.getResult();

    String fileName = testName.getMethodName().replace("test", "") + ".bpmn";
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
    assertThat(result.getFiles(), hasSize(4));
    assertThat(result.getFiles().get(0).typeSpec.name, equalTo("TC_startEvent__endEvent"));
    assertThat(result.getFiles().get(1).typeSpec.name, equalTo("MultiInstanceCallActivityHandler"));
    assertThat(result.getFiles().get(2).typeSpec.name, equalTo("TC_startEvent__multiInstanceCallActivity"));
    assertThat(result.getFiles().get(3).typeSpec.name, equalTo("MultiInstanceCallActivityHandler"));

    TypeSpec typeSpec;

    typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.fieldSpecs, hasSize(1));
    assertThat(typeSpec.fieldSpecs.get(0).name, equalTo("multiInstanceCallActivity"));
    assertThat(typeSpec.fieldSpecs.get(0).type, equalTo(multiInstanceHandlerType));
    assertThat(typeSpec.methodSpecs, hasSize(7));
    assertThat(typeSpec.methodSpecs.get(6).javadoc.isEmpty(), is(false));
    assertThat(typeSpec.methodSpecs.get(6).name, equalTo("handleMultiInstanceCallActivity"));
    assertThat(typeSpec.methodSpecs.get(6).returnType, equalTo(multiInstanceHandlerType));

    JavaFile javaFile = result.getFiles().get(1);
    assertThat(javaFile.packageName, equalTo("org.example.callactivity"));
    assertThat(javaFile.skipJavaLangImports, is(true));
    assertThat(javaFile.typeSpec, notNullValue());

    typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec.javadoc.isEmpty(), is(false));
    assertThat(typeSpec.fieldSpecs, hasSize(0));
    assertThat(typeSpec.methodSpecs, hasSize(3));
    assertThat(typeSpec.methodSpecs.get(1).name, equalTo("apply"));
    assertThat(typeSpec.methodSpecs.get(2).name, equalTo("createHandler"));
    assertThat(typeSpec.methodSpecs.get(2).returnType, equalTo(TypeName.get(CallActivityHandler.class)));

    containsCode(typeSpec.methodSpecs.get(1))
        .contains(String.format("%s handler = getHandler(loopIndex)", TypeName.get(CallActivityHandler.class)))
        .contains("registerCallActivityHandler(handler)")
        .contains("getHandlerBefore(loopIndex)")
        .contains("getHandlerAfter(loopIndex)");
    
    containsCode(typeSpec.methodSpecs.get(2))
        .contains(String.format("return new %s", TypeName.get(CallActivityHandler.class)));
  }

  @Test
  public void testCallActivityError() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(2));

    TypeSpec typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec.javadoc.isEmpty(), is(false));
    assertThat(typeSpec.fieldSpecs, hasSize(0));
    assertThat(typeSpec.methodSpecs, hasSize(3));
    assertThat(typeSpec.methodSpecs.get(1).name, equalTo("apply"));
    assertThat(typeSpec.methodSpecs.get(2).name, equalTo("createHandler"));
    assertThat(typeSpec.methodSpecs.get(2).returnType, equalTo(TypeName.get(CallActivityHandler.class)));

    containsCode(typeSpec.methodSpecs.get(1))
        .contains(String.format("%s handler = getHandler(loopIndex)", TypeName.get(CallActivityHandler.class)))
        .contains("registerCallActivityHandler(handler)")
        .contains("getHandlerBefore(loopIndex)")
        .contains("getHandlerAfter(loopIndex)");
  }

  @Test
  public void testCallActivityTimer() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(2));

    TypeSpec typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec.javadoc.isEmpty(), is(false));
    assertThat(typeSpec.fieldSpecs, hasSize(1));
    assertThat(typeSpec.fieldSpecs.get(0).name, equalTo("boundaryEventHandler"));
    assertThat(typeSpec.fieldSpecs.get(0).type, equalTo(TypeName.get(JobHandler.class)));
    assertThat(typeSpec.methodSpecs, hasSize(4));
    assertThat(typeSpec.methodSpecs.get(1).name, equalTo("apply"));
    assertThat(typeSpec.methodSpecs.get(2).name, equalTo("createHandler"));
    assertThat(typeSpec.methodSpecs.get(2).returnType, equalTo(TypeName.get(CallActivityHandler.class)));
    assertThat(typeSpec.methodSpecs.get(3).name, equalTo("handleBoundaryEvent"));
    assertThat(typeSpec.methodSpecs.get(3).returnType, equalTo(TypeName.get(JobHandler.class)));

    containsCode(typeSpec.methodSpecs.get(1))
        .contains(String.format("%s handler = getHandler(loopIndex)", TypeName.get(CallActivityHandler.class)))
        .contains("registerCallActivityHandler(handler)")
        .contains("if (handler.isWaitingForBoundaryEvent())")
        .contains("instance.apply(boundaryEventHandler)")
        .contains("getHandlerBefore(loopIndex)")
        .contains("getHandlerAfter(loopIndex)");
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
    assertThat(result.getFiles(), hasSize(2));

    TypeSpec typeSpec;

    typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.fieldSpecs, hasSize(1));
    assertThat(typeSpec.fieldSpecs.get(0).name, equalTo("multiInstanceManualTask"));
    assertThat(typeSpec.fieldSpecs.get(0).type, equalTo(multiInstanceHandlerType));
    assertThat(typeSpec.methodSpecs, hasSize(7));
    assertThat(typeSpec.methodSpecs.get(6).javadoc.isEmpty(), is(false));
    assertThat(typeSpec.methodSpecs.get(6).name, equalTo("handleMultiInstanceManualTask"));
    assertThat(typeSpec.methodSpecs.get(6).returnType, equalTo(multiInstanceHandlerType));

    JavaFile javaFile = result.getFiles().get(1);
    assertThat(javaFile.packageName, equalTo("org.example.sequential"));
    assertThat(javaFile.skipJavaLangImports, is(true));
    assertThat(javaFile.typeSpec, notNullValue());

    typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec.javadoc.isEmpty(), is(false));
    assertThat(typeSpec.fieldSpecs, hasSize(0));
    assertThat(typeSpec.methodSpecs, hasSize(1));
  }

  /**
   * Tests the code generation for parallel multi instance - {@code isSequential} method is
   * overridden.
   */
  @Test
  public void testParallel() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(2));

    TypeSpec typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec.javadoc.isEmpty(), is(false));
    assertThat(typeSpec.fieldSpecs, hasSize(0));
    assertThat(typeSpec.methodSpecs, hasSize(2));
    assertThat(typeSpec.methodSpecs.get(1).name, equalTo("isSequential"));
    assertThat(typeSpec.methodSpecs.get(1).returnType, equalTo(TypeName.BOOLEAN));

    String isSequentialCode = typeSpec.methodSpecs.get(1).code.toString();
    assertThat(isSequentialCode, containsString("return false"));
  }


  @Test
  public void testSequential() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(2));

    TypeSpec typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec.javadoc.isEmpty(), is(false));
    assertThat(typeSpec.fieldSpecs, hasSize(0));
    assertThat(typeSpec.methodSpecs, hasSize(1));
  }

  /**
   * Tests the code generation for multi instance with wait state - {@code apply} and
   * {@code createHandler} method are overridden.
   */
  @Test
  public void testUserTask() {
    TypeName multiInstanceHandlerType = ClassName.get("org.example.usertask", "MultiInstanceUserTaskHandler");

    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(2));

    TypeSpec typeSpec;

    typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.fieldSpecs, hasSize(1));
    assertThat(typeSpec.fieldSpecs.get(0).name, equalTo("multiInstanceUserTask"));
    assertThat(typeSpec.fieldSpecs.get(0).type, equalTo(multiInstanceHandlerType));
    assertThat(typeSpec.methodSpecs, hasSize(7));
    assertThat(typeSpec.methodSpecs.get(6).javadoc.isEmpty(), is(false));
    assertThat(typeSpec.methodSpecs.get(6).name, equalTo("handleMultiInstanceUserTask"));
    assertThat(typeSpec.methodSpecs.get(6).returnType, equalTo(multiInstanceHandlerType));

    JavaFile javaFile = result.getFiles().get(1);
    assertThat(javaFile.packageName, equalTo("org.example.usertask"));
    assertThat(javaFile.skipJavaLangImports, is(true));
    assertThat(javaFile.typeSpec, notNullValue());

    typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec.javadoc.isEmpty(), is(false));
    assertThat(typeSpec.fieldSpecs, hasSize(0));
    assertThat(typeSpec.methodSpecs, hasSize(3));
    assertThat(typeSpec.methodSpecs.get(1).name, equalTo("apply"));
    assertThat(typeSpec.methodSpecs.get(2).name, equalTo("createHandler"));
    assertThat(typeSpec.methodSpecs.get(2).returnType, equalTo(TypeName.get(UserTaskHandler.class)));

    containsCode(typeSpec.methodSpecs.get(1))
        .contains("getHandler(loopIndex)")
        .contains("getHandlerBefore(loopIndex)")
        .contains("getHandlerAfter(loopIndex)");

    containsCode(typeSpec.methodSpecs.get(2))
        .contains(String.format("return new %s", TypeName.get(UserTaskHandler.class)));
  }

  @Test
  public void testUserTaskError() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(2));

    TypeSpec typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec.javadoc.isEmpty(), is(false));
    assertThat(typeSpec.fieldSpecs, hasSize(0));
    assertThat(typeSpec.methodSpecs, hasSize(3));
    assertThat(typeSpec.methodSpecs.get(1).name, equalTo("apply"));
    assertThat(typeSpec.methodSpecs.get(2).name, equalTo("createHandler"));
    assertThat(typeSpec.methodSpecs.get(2).returnType, equalTo(TypeName.get(UserTaskHandler.class)));

    containsCode(typeSpec.methodSpecs.get(1))
        .contains(String.format("%s handler = getHandler(loopIndex)", TypeName.get(UserTaskHandler.class)))
        .contains("getHandlerBefore(loopIndex)")
        .contains("instance.apply(handler)")
        .contains("getHandlerAfter(loopIndex)");
  }

  @Test
  public void testUserTaskMessage() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(2));

    TypeSpec typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec.javadoc.isEmpty(), is(false));
    assertThat(typeSpec.fieldSpecs, hasSize(1));
    assertThat(typeSpec.fieldSpecs.get(0).name, equalTo("boundaryEventHandler"));
    assertThat(typeSpec.fieldSpecs.get(0).type, equalTo(TypeName.get(EventHandler.class)));
    assertThat(typeSpec.methodSpecs, hasSize(4));
    assertThat(typeSpec.methodSpecs.get(1).name, equalTo("apply"));
    assertThat(typeSpec.methodSpecs.get(2).name, equalTo("createHandler"));
    assertThat(typeSpec.methodSpecs.get(2).returnType, equalTo(TypeName.get(UserTaskHandler.class)));
    assertThat(typeSpec.methodSpecs.get(3).name, equalTo("handleBoundaryEvent"));
    assertThat(typeSpec.methodSpecs.get(3).returnType, equalTo(TypeName.get(EventHandler.class)));

    containsCode(typeSpec.methodSpecs.get(1))
        .contains(String.format("%s handler = getHandler(loopIndex)", TypeName.get(UserTaskHandler.class)))
        .contains("if (handler.isWaitingForBoundaryEvent())")
        .contains("instance.apply(boundaryEventHandler)")
        .contains("getHandlerBefore(loopIndex)")
        .contains("getHandlerAfter(loopIndex)");
  }
}
