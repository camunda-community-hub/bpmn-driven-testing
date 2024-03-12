package org.camunda.community.bpmndt.platform8;

import static com.google.common.truth.Truth.assertThat;
import static org.camunda.community.bpmndt.test.FieldSpecSubject.assertThat;
import static org.camunda.community.bpmndt.test.MethodSpecSubject.assertThat;
import static org.camunda.community.bpmndt.test.TypeSpecSubject.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.community.bpmndt.GeneratorContextBase;
import org.camunda.community.bpmndt.GeneratorResult;
import org.camunda.community.bpmndt.platform8.api.AbstractJUnit5TestCase;
import org.camunda.community.bpmndt.platform8.strategy.DefaultStrategy;
import org.camunda.community.bpmndt.test.Platform8TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

public class GeneratorSimpleTest {

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
    ctx.setMainResourcePath(Platform8TestPaths.simple());
    ctx.setTestSourcePath(temporaryDirectory);

    ctx.setPackageName("org.example");

    result = generator.getResult();

    String fileName = testInfo.getTestMethod().orElseThrow(NoSuchElementException::new).getName().replace("test", "") + ".bpmn";
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

    assertThat(typeSpec.superclass).isInstanceOf(ClassName.class);
    assertThat(typeSpec.superclass).isEqualTo(ClassName.get(AbstractJUnit5TestCase.class));

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

  @Test
  public void testSimpleCollaboration() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasMethods(6);
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
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(DefaultStrategy.OTHER);

    assertThat(typeSpec.methodSpecs.get(6)).hasName("handleMessageCatchEvent");
    assertThat(typeSpec.methodSpecs.get(6)).hasReturnType(DefaultStrategy.OTHER);

    typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(7);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("timerCatchEvent");
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(DefaultStrategy.OTHER);

    assertThat(typeSpec.methodSpecs.get(6)).hasName("handleTimerCatchEvent");
    assertThat(typeSpec.methodSpecs.get(6)).hasReturnType(DefaultStrategy.OTHER);

    typeSpec = result.getFiles().get(2).typeSpec;
    assertThat(typeSpec).hasFields(0);
    assertThat(typeSpec).hasMethods(7);

    assertThat(typeSpec.methodSpecs.get(6)).hasName("isProcessEnd");
    assertThat(typeSpec.methodSpecs.get(6)).containsCode("return false");
  }

  @Test
  public void testSimpleMessageCatchEvent() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(7);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("messageCatchEvent");
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(DefaultStrategy.OTHER);

    assertThat(typeSpec.methodSpecs.get(6)).hasName("handleMessageCatchEvent");
    assertThat(typeSpec.methodSpecs.get(6)).hasReturnType(DefaultStrategy.OTHER);

    String expected = "messageCatchEvent = new %s(getProcessEngine(), \"messageCatchEvent\", \"simpleMessage\");";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format(expected, DefaultStrategy.OTHER));
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
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(DefaultStrategy.JOB);

    assertThat(typeSpec.methodSpecs.get(6)).hasName("handleMessageThrowEvent");
    assertThat(typeSpec.methodSpecs.get(6)).hasReturnType(DefaultStrategy.JOB);

    String expected = "messageThrowEvent = new %s(getProcessEngine(), \"messageThrowEvent\", \"test-message\");";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format(expected, DefaultStrategy.JOB));
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
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(DefaultStrategy.OTHER);

    assertThat(typeSpec.methodSpecs.get(6)).hasName("handleReceiveTask");
    assertThat(typeSpec.methodSpecs.get(6)).hasReturnType(DefaultStrategy.OTHER);

    String expected = "receiveTask = new %s(getProcessEngine(), \"receiveTask\", \"simpleMessage\");";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format(expected, DefaultStrategy.OTHER));
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
    assertThat(typeSpec.fieldSpecs.get(0).type).isEqualTo(DefaultStrategy.OTHER);

    assertThat(typeSpec.methodSpecs.get(6)).hasName("handleSignalCatchEvent");
    assertThat(typeSpec.methodSpecs.get(6)).hasReturnType(DefaultStrategy.OTHER);

    String expected = "signalCatchEvent = new %s(getProcessEngine(), \"signalCatchEvent\", \"simpleSignal\");";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format(expected, DefaultStrategy.OTHER));
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
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(processInstanceEvent, \"subProcessStartEvent\");");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("// endEvent: subProcessEndEvent");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(processInstanceEvent, \"subProcessEndEvent\");");
  }

  @Test
  public void testSimpleTimerCatchEvent() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(7);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("timerCatchEvent");
    assertThat(typeSpec.fieldSpecs.get(0).type).isEqualTo(DefaultStrategy.TIMER_EVENT);

    assertThat(typeSpec.methodSpecs.get(6)).hasName("handleTimerCatchEvent");
    assertThat(typeSpec.methodSpecs.get(6)).hasReturnType(DefaultStrategy.TIMER_EVENT);

    String expected;

    expected = "timerCatchEvent = new %s(timerCatchEventElement);";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format(expected, DefaultStrategy.TIMER_EVENT));

    expected = "timerCatchEventElement.setId(\"timerCatchEvent\");";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);
    expected = "imerCatchEventElement.setTimeDuration(\"PT1H\");";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);

    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.isWaitingAt(processInstanceEvent, \"timerCatchEvent\");");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(timerCatchEvent);");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(processInstanceEvent, \"timerCatchEvent\");");
  }

  @Test
  public void testSimpleUserTask() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(3);
    assertThat(typeSpec).hasMethods(9);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("userTask");
    assertThat(typeSpec.fieldSpecs.get(0).type).isEqualTo(DefaultStrategy.USER_TASK);

    assertThat(typeSpec.methodSpecs.get(6)).hasName("handleUserTask");
    assertThat(typeSpec.methodSpecs.get(6)).hasReturnType(DefaultStrategy.USER_TASK);

    String expected;

    expected = "userTask = new %s(userTaskElement);";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format(expected, DefaultStrategy.USER_TASK));

    expected = "userTaskElement.setId(\"userTask\");";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);
    expected = "userTaskElement.setAssignee(\"=\\\"simpleAssignee\\\"\");";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);
    expected = "userTaskElement.setCandidateGroups(\"=[\\\"simpleGroupA\\\", \\\"simpleGroupB\\\"]\");";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);
    expected = "userTaskElement.setCandidateUsers(\"=[\\\"simpleUserA\\\", \\\"simpleUserB\\\"]\");";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);
    expected = "userTaskElement.setDueDate(\"=\\\"2023-02-17T00:00:00Z\\\"\");";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);
    expected = "userTaskElement.setFollowUpDate(\"=\\\"2023-02-18T00:00:00Z\\\"\");";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);

    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.isWaitingAt(processInstanceEvent, \"userTask\");");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(userTask);");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(processInstanceEvent, \"userTask\");");
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
    assertThat(isFile.test("org/example/simplemessagecatchevent/TC_startEvent__endEvent.java")).isTrue();
    assertThat(isFile.test("org/example/simplereceivetask/TC_startEvent__endEvent.java")).isTrue();
    assertThat(isFile.test("org/example/simplesignalcatchevent/TC_startEvent__endEvent.java")).isTrue();
    assertThat(isFile.test("org/example/simplesubprocess/TC_startEvent__endEvent.java")).isTrue();
    assertThat(isFile.test("org/example/simpletimercatchevent/TC_startEvent__endEvent.java")).isTrue();
    assertThat(isFile.test("org/example/simpleusertask/TC_startEvent__endEvent.java")).isTrue();
  }
}
