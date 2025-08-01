package org.camunda.community.bpmndt;

import static com.google.common.truth.Truth.assertThat;
import static org.camunda.community.bpmndt.GeneratorStrategy.CALL_ACTIVITY;
import static org.camunda.community.bpmndt.GeneratorStrategy.EVENT;
import static org.camunda.community.bpmndt.GeneratorStrategy.EXTERNAL_TASK;
import static org.camunda.community.bpmndt.GeneratorStrategy.EXTERNAL_TASK_CLIENT;
import static org.camunda.community.bpmndt.GeneratorStrategy.JOB;
import static org.camunda.community.bpmndt.GeneratorStrategy.RECEIVE_TASK;
import static org.camunda.community.bpmndt.GeneratorStrategy.USER_TASK;
import static org.camunda.community.bpmndt.test.FieldSpecSubject.assertThat;
import static org.camunda.community.bpmndt.test.MethodSpecSubject.assertThat;
import static org.camunda.community.bpmndt.test.TypeSpecSubject.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.community.bpmndt.api.AbstractJUnit5TestCase;
import org.camunda.community.bpmndt.api.EventHandler;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

public class GeneratorSimpleTest {

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
    ctx.setMainResourcePath(TestPaths.simple());
    ctx.setTestSourcePath(temporaryDirectory);

    ctx.setPackageName("org.example");

    result = generator.getResult();

    String fileName = testInfo.getTestMethod().orElseThrow().getName().replace("test", "") + ".bpmn";
    bpmnFile = ctx.getMainResourcePath().resolve(StringUtils.uncapitalize(fileName));
  }

  @Test
  public void testSimple() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    JavaFile javaFile = result.getFiles().get(0);
    assertThat(javaFile.packageName).isEqualTo("org.example.simple");
    assertThat(javaFile.skipJavaLangImports).isTrue();
    assertThat(javaFile.typeSpec).isNotNull();

    TypeSpec typeSpec = javaFile.typeSpec;
    assertThat(typeSpec).hasName("TC_startEvent__endEvent");

    ClassName rawType = ClassName.get(AbstractJUnit5TestCase.class);
    ClassName typeArgument = ClassName.bestGuess(typeSpec.name);

    assertThat(typeSpec.superclass).isInstanceOf(ParameterizedTypeName.class);
    assertThat(((ParameterizedTypeName) typeSpec.superclass).rawType).isEqualTo(rawType);
    assertThat(((ParameterizedTypeName) typeSpec.superclass).typeArguments.get(0)).isEqualTo(typeArgument);

    assertThat(typeSpec).hasFields(0);
    assertThat(typeSpec).hasMethods(6);

    assertThat(typeSpec.methodSpecs.get(0)).hasName("beforeEach");
    assertThat(typeSpec.methodSpecs.get(0).parameters).hasSize(0);
    assertThat(typeSpec.methodSpecs.get(1)).hasName("execute");
    assertThat(typeSpec.methodSpecs.get(1)).hasParameters("pi");
    assertThat(typeSpec.methodSpecs.get(1).parameters.get(0).type).isEqualTo(ClassName.get(ProcessInstance.class));
    assertThat(typeSpec.methodSpecs.get(2)).hasName("getBpmnResourceName");
    assertThat(typeSpec.methodSpecs.get(2)).containsCode("\"simple.bpmn\"");
    assertThat(typeSpec.methodSpecs.get(3)).hasName("getEnd");
    assertThat(typeSpec.methodSpecs.get(3)).containsCode("\"endEvent\"");
    assertThat(typeSpec.methodSpecs.get(4)).hasName("getProcessDefinitionKey");
    assertThat(typeSpec.methodSpecs.get(4)).containsCode("\"simple\"");
    assertThat(typeSpec.methodSpecs.get(5)).hasName("getStart");
    assertThat(typeSpec.methodSpecs.get(5)).containsCode("\"startEvent\"");
  }

  /**
   * Should be the same as {@link #testSimple()}, when Spring is enabled.
   */
  @Test
  public void testSimpleSpringEnabled() {
    ctx.setSpringEnabled(true);

    // override auto built BPMN file path
    bpmnFile = ctx.getMainResourcePath().resolve("simple.bpmn");

    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    JavaFile javaFile = result.getFiles().get(0);
    assertThat(javaFile.packageName).isEqualTo("org.example.simple");
    assertThat(javaFile.skipJavaLangImports).isTrue();
    assertThat(javaFile.typeSpec).isNotNull();

    TypeSpec typeSpec = javaFile.typeSpec;
    assertThat(typeSpec).hasName("TC_startEvent__endEvent");

    ClassName rawType = ClassName.get(AbstractJUnit5TestCase.class);
    ClassName typeArgument = ClassName.bestGuess(typeSpec.name);

    assertThat(typeSpec.superclass).isInstanceOf(ParameterizedTypeName.class);
    assertThat(((ParameterizedTypeName) typeSpec.superclass).rawType).isEqualTo(rawType);
    assertThat(((ParameterizedTypeName) typeSpec.superclass).typeArguments.get(0)).isEqualTo(typeArgument);

    assertThat(typeSpec).hasFields(0);
    assertThat(typeSpec).hasMethods(7);

    assertThat(typeSpec.methodSpecs.get(0)).hasName("beforeEach");
    assertThat(typeSpec.methodSpecs.get(0).parameters).hasSize(0);
    assertThat(typeSpec.methodSpecs.get(1)).hasName("execute");
    assertThat(typeSpec.methodSpecs.get(1)).hasParameters("pi");
    assertThat(typeSpec.methodSpecs.get(1).parameters.get(0).type).isEqualTo(ClassName.get(ProcessInstance.class));
    assertThat(typeSpec.methodSpecs.get(2)).hasName("getBpmnResourceName");
    assertThat(typeSpec.methodSpecs.get(2)).containsCode("\"simple.bpmn\"");
    assertThat(typeSpec.methodSpecs.get(3)).hasName("getEnd");
    assertThat(typeSpec.methodSpecs.get(3)).containsCode("\"endEvent\"");
    assertThat(typeSpec.methodSpecs.get(4)).hasName("getProcessDefinitionKey");
    assertThat(typeSpec.methodSpecs.get(4)).containsCode("\"simple\"");
    assertThat(typeSpec.methodSpecs.get(5)).hasName("getStart");
    assertThat(typeSpec.methodSpecs.get(5)).containsCode("\"startEvent\"");
    assertThat(typeSpec.methodSpecs.get(6)).hasName("isSpringEnabled");
    assertThat(typeSpec.methodSpecs.get(6)).isProtected();
    assertThat(typeSpec.methodSpecs.get(6)).containsCode("return true");
  }

  @Test
  public void testSimpleAsync() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(2);
    assertThat(typeSpec).hasMethods(8);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("startEventAfter");
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(JOB);
    assertThat(typeSpec.fieldSpecs.get(1)).hasName("endEventBefore");
    assertThat(typeSpec.fieldSpecs.get(1)).hasType(JOB);

    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format("startEventAfter = new %s(getProcessEngine(), \"startEvent\");", JOB));
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format("endEventBefore = new %s(getProcessEngine(), \"endEvent\");", JOB));

    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(startEventAfter);");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(endEventBefore);");

    assertThat(typeSpec.methodSpecs.get(6)).hasName("handleStartEventAfter");
    assertThat(typeSpec.methodSpecs.get(6)).hasReturnType(JOB);
    assertThat(typeSpec.methodSpecs.get(7)).hasName("handleEndEventBefore");
    assertThat(typeSpec.methodSpecs.get(7)).hasReturnType(JOB);
  }

  @Test
  public void testSimpleCallActivity() {
    generator.generateTestCases(ctx, bpmnFile);

    // BPMN process contains 2 test cases
    assertThat(result.getFiles()).hasSize(2);
    assertThat(result.getFiles().get(0).typeSpec).hasName("TC_startEvent__endEvent");
    assertThat(result.getFiles().get(1).typeSpec).hasName("TC_startEvent__callActivity");

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(2);
    assertThat(typeSpec).hasMethods(7);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("callActivityBefore");
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(JOB);
    assertThat(typeSpec.fieldSpecs.get(1)).hasName("callActivity");
    assertThat(typeSpec.fieldSpecs.get(1)).hasType(CALL_ACTIVITY);

    assertThat(typeSpec.methodSpecs.get(6)).hasName("handleCallActivity");
    assertThat(typeSpec.methodSpecs.get(6)).hasReturnType(CALL_ACTIVITY);

    String expected = "callActivity = new %s(instance, \"callActivity\");";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format(expected, CALL_ACTIVITY));
  }

  @Test
  public void testSimpleCollaboration() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasMethods(6);
  }

  @Test
  public void testSimpleConditionalCatchEvent() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(7);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("conditionalCatchEvent");
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(EVENT);

    assertThat(typeSpec.methodSpecs.get(6)).hasName("handleConditionalCatchEvent");
    assertThat(typeSpec.methodSpecs.get(6)).hasReturnType(EVENT);

    String expected = "conditionalCatchEvent = new %s(getProcessEngine(), \"conditionalCatchEvent\", null);";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format(expected, EVENT));
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(conditionalCatchEvent);");
  }

  @Test
  public void testSimpleEventBasedGateway() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(3);
    assertThat(result.getFiles().get(0).typeSpec).hasName("TC_Message");
    assertThat(result.getFiles().get(1).typeSpec).hasName("TC_Timer");
    assertThat(result.getFiles().get(2).typeSpec).hasName("TC_startEvent__eventBasedGateway");

    TypeSpec typeSpec;

    typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(7);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("messageCatchEvent");
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(EVENT);

    assertThat(typeSpec.methodSpecs.get(6)).hasName("handleMessageCatchEvent");
    assertThat(typeSpec.methodSpecs.get(6)).hasReturnType(EVENT);

    typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(7);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("timerCatchEvent");
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(JOB);

    assertThat(typeSpec.methodSpecs.get(6)).hasName("handleTimerCatchEvent");
    assertThat(typeSpec.methodSpecs.get(6)).hasReturnType(JOB);

    typeSpec = result.getFiles().get(2).typeSpec;
    assertThat(typeSpec).hasFields(0);
    assertThat(typeSpec).hasMethods(7);

    assertThat(typeSpec.methodSpecs.get(6)).hasName("isProcessEnd");
    assertThat(typeSpec.methodSpecs.get(6)).containsCode("return false");
  }

  @Test
  public void testSimpleExternalTask() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(7);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("externalTask");
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(EXTERNAL_TASK);

    assertThat(typeSpec.methodSpecs.get(6)).hasName("handleExternalTask");
    assertThat(typeSpec.methodSpecs.get(6)).hasReturnType(EXTERNAL_TASK);

    String expected = "externalTask = new %s(getProcessEngine(), \"externalTask\", \"test-topic\");";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format(expected, "ExternalTaskHandler<>"));
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(externalTask);");
  }

  @Test
  public void testSimpleExternalTaskClient() {
    ctx.setExternalTaskClientUsed(true);

    // override auto built BPMN file path
    bpmnFile = ctx.getMainResourcePath().resolve("simpleExternalTask.bpmn");

    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(7);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("externalTask");
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(EXTERNAL_TASK_CLIENT);

    assertThat(typeSpec.methodSpecs.get(6)).hasName("handleExternalTask");
    assertThat(typeSpec.methodSpecs.get(6)).hasReturnType(EXTERNAL_TASK_CLIENT);

    String expected = "externalTask = new %s(getProcessEngine(), \"externalTask\", \"test-topic\");";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format(expected, "ExternalTaskClientHandler<>"));
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(externalTask);");
  }

  @Test
  public void testSimpleMessageCatchEvent() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(7);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("messageCatchEvent");
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(EVENT);

    assertThat(typeSpec.methodSpecs.get(6)).hasName("handleMessageCatchEvent");
    assertThat(typeSpec.methodSpecs.get(6)).hasReturnType(EVENT);

    String expected = "messageCatchEvent = new %s(getProcessEngine(), \"messageCatchEvent\", \"simpleMessage\");";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format(expected, EVENT));
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(messageCatchEvent);");
  }

  @Test
  public void testSimpleMessageThrowEvent() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(7);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("messageThrowEvent");
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(EXTERNAL_TASK);

    assertThat(typeSpec.methodSpecs.get(6)).hasName("handleMessageThrowEvent");
    assertThat(typeSpec.methodSpecs.get(6)).hasReturnType(EXTERNAL_TASK);

    String expected = "messageThrowEvent = new %s(getProcessEngine(), \"messageThrowEvent\", \"test-message\");";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format(expected, "ExternalTaskHandler<>"));
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(messageThrowEvent);");
  }

  @Test
  public void testSimpleReceiveTask() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(7);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("receiveTask");
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(RECEIVE_TASK);

    assertThat(typeSpec.methodSpecs.get(6)).hasName("handleReceiveTask");
    assertThat(typeSpec.methodSpecs.get(6)).hasReturnType(RECEIVE_TASK);

    String expected = "receiveTask = new %s(getProcessEngine(), \"receiveTask\", \"simpleMessage\");";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format(expected, RECEIVE_TASK));
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(receiveTask);");
  }

  @Test
  public void testSimpleSignalCatchEvent() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(7);
    assertThat(typeSpec.fieldSpecs.get(0)).hasName("signalCatchEvent");
    assertThat(typeSpec.fieldSpecs.get(0).type).isEqualTo(ClassName.get(EventHandler.class));

    assertThat(typeSpec.methodSpecs.get(6)).hasName("handleSignalCatchEvent");
    assertThat(typeSpec.methodSpecs.get(6)).hasReturnType(ClassName.get(EventHandler.class));

    String expected = "signalCatchEvent = new %s(getProcessEngine(), \"signalCatchEvent\", \"simpleSignal\");";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format(expected, EVENT));
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(signalCatchEvent);");
  }

  @Test
  public void testSimpleSubProcess() {
    generator.generateTestCases(ctx, bpmnFile);

    // BPMN process contains 2 test cases
    assertThat(result.getFiles()).hasSize(2);
    assertThat(result.getFiles().get(0).typeSpec).hasName("TC_startEvent__endEvent");
    assertThat(result.getFiles().get(1).typeSpec).hasName("TC_startEvent__subProcessEndEvent");

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasMethods(6);

    assertThat(typeSpec.methodSpecs.get(1)).containsCode("// startEvent: subProcessStartEvent");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("assertThat(pi).hasPassed(\"subProcessStartEvent\");");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("// endEvent: subProcessEndEvent");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("assertThat(pi).hasPassed(\"subProcessEndEvent\");");
  }

  @Test
  public void testSimpleTimerCatchEvent() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(7);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("timerCatchEvent");
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(JOB);

    assertThat(typeSpec.methodSpecs.get(6)).hasName("handleTimerCatchEvent");
    assertThat(typeSpec.methodSpecs.get(6)).hasReturnType(JOB);

    String expected = "timerCatchEvent = new %s(getProcessEngine(), \"timerCatchEvent\");";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format(expected, JOB));
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(timerCatchEvent);");
  }

  @Test
  public void testSimpleUserTask() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(7);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("userTask");
    assertThat(typeSpec.fieldSpecs.get(0).type).isEqualTo(USER_TASK);

    assertThat(typeSpec.methodSpecs.get(6)).hasName("handleUserTask");
    assertThat(typeSpec.methodSpecs.get(6)).hasReturnType(USER_TASK);

    String expected = "userTask = new %s(getProcessEngine(), \"userTask\");";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format(expected, USER_TASK));
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(userTask);");
  }

  /**
   * Tests the complete generation.
   */
  @Test
  public void testGenerate() {
    generator.generate(ctx);

    Predicate<String> isFile = (className) -> Files.isRegularFile(ctx.getTestSourcePath().resolve(className));

    // test cases
    assertThat(isFile.test("org/example/simple/TC_startEvent__endEvent.java")).isTrue();
    assertThat(isFile.test("org/example/simpleasync/TC_startEvent__endEvent.java")).isTrue();
    assertThat(isFile.test("org/example/simplecallactivity/TC_startEvent__endEvent.java")).isTrue();
    assertThat(isFile.test("org/example/simplecollaboration/TC_startEvent__endEvent.java")).isTrue();
    assertThat(isFile.test("org/example/simpleconditionalcatchevent/TC_startEvent__endEvent.java")).isTrue();
    assertThat(isFile.test("org/example/simpleexternaltask/TC_startEvent__endEvent.java")).isTrue();
    assertThat(isFile.test("org/example/simplemessagecatchevent/TC_startEvent__endEvent.java")).isTrue();
    assertThat(isFile.test("org/example/simplereceivetask/TC_startEvent__endEvent.java")).isTrue();
    assertThat(isFile.test("org/example/simplesignalcatchevent/TC_startEvent__endEvent.java")).isTrue();
    assertThat(isFile.test("org/example/simplesubprocess/TC_startEvent__endEvent.java")).isTrue();
    assertThat(isFile.test("org/example/simpletimercatchevent/TC_startEvent__endEvent.java")).isTrue();
    assertThat(isFile.test("org/example/simpleusertask/TC_startEvent__endEvent.java")).isTrue();
  }
}
