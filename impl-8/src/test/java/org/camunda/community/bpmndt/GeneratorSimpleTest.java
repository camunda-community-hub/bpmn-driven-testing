package org.camunda.community.bpmndt;

import static com.google.common.truth.Truth.assertThat;
import static org.camunda.community.bpmndt.test.FieldSpecSubject.assertThat;
import static org.camunda.community.bpmndt.test.MethodSpecSubject.assertThat;
import static org.camunda.community.bpmndt.test.TypeSpecSubject.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.camunda.community.bpmndt.api.AbstractJUnit5TestCase;
import org.camunda.community.bpmndt.api.TestCaseInstance;
import org.camunda.community.bpmndt.strategy.DefaultStrategy;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

class GeneratorSimpleTest {

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
    ctx.setMainResourcePath(TestPaths.simple());
    ctx.setTestSourcePath(temporaryDirectory);

    ctx.setPackageName("org.example");

    result = generator.getResult();

    var fileName = testInfo.getTestMethod().orElseThrow(NoSuchElementException::new).getName().replace("test", "") + ".bpmn";
    bpmnFile = ctx.getMainResourcePath().resolve(StringUtils.uncapitalize(fileName));
  }

  @Test
  void testSimple() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    var javaFile = result.getFiles().get(0);
    assertThat(javaFile.packageName).isEqualTo("org.example.simple");
    assertThat(javaFile.skipJavaLangImports).isTrue();
    assertThat(javaFile.typeSpec).isNotNull();

    var typeSpec = javaFile.typeSpec;
    assertThat(typeSpec).hasName("TC_startEvent__endEvent");

    assertThat(typeSpec.superclass).isInstanceOf(ClassName.class);
    assertThat(typeSpec.superclass).isEqualTo(ClassName.get(AbstractJUnit5TestCase.class));

    assertThat(typeSpec).hasFields(0);
    assertThat(typeSpec).hasMethods(7);

    assertThat(typeSpec.methodSpecs.get(0)).hasName("beforeEach");
    assertThat(typeSpec.methodSpecs.get(0).parameters).hasSize(0);
    assertThat(typeSpec.methodSpecs.get(1)).hasName("execute");
    assertThat(typeSpec.methodSpecs.get(1)).hasParameters("instance", "flowScopeKey");
    assertThat(typeSpec.methodSpecs.get(1).parameters.get(0).type).isEqualTo(ClassName.get(TestCaseInstance.class));
    assertThat(typeSpec.methodSpecs.get(1).parameters.get(1).type).isEqualTo(TypeName.LONG);
    assertThat(typeSpec.methodSpecs.get(2)).hasName("getBpmnProcessId");
    assertThat(typeSpec.methodSpecs.get(2)).containsCode("\"simple\"");
    assertThat(typeSpec.methodSpecs.get(3)).hasName("getBpmnResourceName");
    assertThat(typeSpec.methodSpecs.get(3)).containsCode("\"simple.bpmn\"");
    assertThat(typeSpec.methodSpecs.get(4)).hasName("getEnd");
    assertThat(typeSpec.methodSpecs.get(4)).containsCode("\"endEvent\"");
    assertThat(typeSpec.methodSpecs.get(5)).hasName("getSimulateSubProcessResource");
    assertThat(typeSpec.methodSpecs.get(5)).containsCode("return org.camunda.community.bpmndt.api.SimulateSubProcessResource.VALUE");
    assertThat(typeSpec.methodSpecs.get(6)).hasName("getStart");
    assertThat(typeSpec.methodSpecs.get(6)).containsCode("\"startEvent\"");
  }

  @Test
  void testSimpleCallActivity() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(2);

    var typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(8);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("callActivity");
    assertThat(typeSpec.fieldSpecs.get(0).type).isEqualTo(DefaultStrategy.CALL_ACTIVITY);

    assertThat(typeSpec.methodSpecs.get(7)).hasName("handleCallActivity");
    assertThat(typeSpec.methodSpecs.get(7)).hasReturnType(DefaultStrategy.CALL_ACTIVITY);

    var expected = "callActivity = new %s(callActivityElement);";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format(expected, DefaultStrategy.CALL_ACTIVITY));

    expected = "callActivityElement.id = \"callActivity\";";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);
    expected = "callActivityElement.processId = \"=\\\"simple\\\"\";";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);
    expected = "callActivityElement.propagateAllChildVariables = true;";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);
    expected = "callActivityElement.propagateAllParentVariables = true;";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);

    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.isWaitingAt(flowScopeKey, \"callActivity\");");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(flowScopeKey, callActivity);");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(flowScopeKey, \"callActivity\");");
  }

  @Test
  void testSimpleCollaboration() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    var typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasMethods(7);
  }

  @Test
  void testSimpleEventBasedGateway() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(3);
    assertThat(result.getFiles().get(0).typeSpec).hasName("TC_Message");
    assertThat(result.getFiles().get(1).typeSpec).hasName("TC_Timer");
    assertThat(result.getFiles().get(2).typeSpec).hasName("TC_startEvent__eventBasedGateway");

    var typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(8);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("messageCatchEvent");
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(DefaultStrategy.MESSAGE_EVENT);

    assertThat(typeSpec.methodSpecs.get(7)).hasName("handleMessageCatchEvent");
    assertThat(typeSpec.methodSpecs.get(7)).hasReturnType(DefaultStrategy.MESSAGE_EVENT);

    assertThat(typeSpec.methodSpecs.get(1)).containsCode("// eventBasedGateway: eventBasedGateway");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.isWaitingAt(flowScopeKey, \"eventBasedGateway\");");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("// intermediateCatchEvent: messageCatchEvent");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(flowScopeKey, messageCatchEvent);");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(flowScopeKey, \"eventBasedGateway\");");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(flowScopeKey, \"messageCatchEvent\");");

    typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(8);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("timerCatchEvent");
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(DefaultStrategy.TIMER_EVENT);

    assertThat(typeSpec.methodSpecs.get(7)).hasName("handleTimerCatchEvent");
    assertThat(typeSpec.methodSpecs.get(7)).hasReturnType(DefaultStrategy.TIMER_EVENT);

    assertThat(typeSpec.methodSpecs.get(1)).containsCode("// eventBasedGateway: eventBasedGateway");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.isWaitingAt(flowScopeKey, \"eventBasedGateway\");");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("// intermediateCatchEvent: timerCatchEvent");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(flowScopeKey, timerCatchEvent);");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(flowScopeKey, \"eventBasedGateway\");");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(flowScopeKey, \"timerCatchEvent\");");

    typeSpec = result.getFiles().get(2).typeSpec;
    assertThat(typeSpec).hasFields(0);
    assertThat(typeSpec).hasMethods(7);

    assertThat(typeSpec.methodSpecs.get(1)).containsCode("// eventBasedGateway: eventBasedGateway");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.isWaitingAt(flowScopeKey, \"eventBasedGateway\");");
  }

  @Test
  void testSimpleMessageCatchEvent() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    var typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(8);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("messageCatchEvent");
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(DefaultStrategy.MESSAGE_EVENT);

    assertThat(typeSpec.methodSpecs.get(7)).hasName("handleMessageCatchEvent");
    assertThat(typeSpec.methodSpecs.get(7)).hasReturnType(DefaultStrategy.MESSAGE_EVENT);

    var expected = "messageCatchEvent = new %s(messageCatchEventElement);";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format(expected, DefaultStrategy.MESSAGE_EVENT));
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.isWaitingAt(flowScopeKey, \"messageCatchEvent\");");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(flowScopeKey, messageCatchEvent);");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(flowScopeKey, \"messageCatchEvent\");");
  }

  @Test
  void testSimpleMessageStartEvent() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    var typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(0);
    assertThat(typeSpec).hasMethods(8);

    assertThat(typeSpec.methodSpecs.get(7)).hasName("isMessageStart");
    assertThat(typeSpec.methodSpecs.get(7)).hasReturnType(TypeName.BOOLEAN);
    assertThat(typeSpec.methodSpecs.get(7)).containsCode("return true");
  }

  @Test
  void testSimpleMessageThrowEvent() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    var typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(8);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("messageThrowEvent");
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(DefaultStrategy.JOB);

    assertThat(typeSpec.methodSpecs.get(7)).hasName("handleMessageThrowEvent");
    assertThat(typeSpec.methodSpecs.get(7)).hasReturnType(DefaultStrategy.JOB);

    var expected = "messageThrowEvent = new %s(messageThrowEventElement);";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format(expected, DefaultStrategy.JOB));
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.isWaitingAt(flowScopeKey, \"messageThrowEvent\");");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(flowScopeKey, messageThrowEvent);");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(flowScopeKey, \"messageThrowEvent\");");
  }

  @Test
  void testSimpleReceiveTask() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    var typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(8);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("receiveTask");
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(DefaultStrategy.RECEIVE_TASK);

    assertThat(typeSpec.methodSpecs.get(7)).hasName("handleReceiveTask");
    assertThat(typeSpec.methodSpecs.get(7)).hasReturnType(DefaultStrategy.RECEIVE_TASK);

    var expected = "receiveTask = new %s(receiveTaskElement);";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format(expected, DefaultStrategy.RECEIVE_TASK));
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.isWaitingAt(flowScopeKey, \"receiveTask\");");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(flowScopeKey, receiveTask);");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(flowScopeKey, \"receiveTask\");");
  }

  @Test
  void testSimpleSignalCatchEvent() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    var typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(8);
    assertThat(typeSpec.fieldSpecs.get(0)).hasName("signalCatchEvent");
    assertThat(typeSpec.fieldSpecs.get(0).type).isEqualTo(DefaultStrategy.SIGNAL_EVENT);

    assertThat(typeSpec.methodSpecs.get(7)).hasName("handleSignalCatchEvent");
    assertThat(typeSpec.methodSpecs.get(7)).hasReturnType(DefaultStrategy.SIGNAL_EVENT);

    var expected = "signalCatchEvent = new %s(signalCatchEventElement);";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format(expected, DefaultStrategy.SIGNAL_EVENT));
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.isWaitingAt(flowScopeKey, \"signalCatchEvent\");");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(flowScopeKey, signalCatchEvent);");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(flowScopeKey, \"signalCatchEvent\");");
  }

  @Test
  void testSimpleSignalStartEvent() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    var typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(0);
    assertThat(typeSpec).hasMethods(8);

    assertThat(typeSpec.methodSpecs.get(7)).hasName("isSignalStart");
    assertThat(typeSpec.methodSpecs.get(7)).hasReturnType(TypeName.BOOLEAN);
    assertThat(typeSpec.methodSpecs.get(7)).containsCode("return true");
  }

  @Test
  void testSimpleSubProcess() {
    generator.generateTestCases(ctx, bpmnFile);

    assertThat(result.getFiles()).hasSize(3);
    assertThat(result.getFiles().get(0).typeSpec).hasName("TC_startEvent__endEvent");
    assertThat(result.getFiles().get(1).typeSpec).hasName("TC_startEvent__subProcessEndEvent");
    assertThat(result.getFiles().get(2).typeSpec).hasName("TC_subProcessStartEvent__endEvent");

    var typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasMethods(8);

    assertThat(typeSpec.methodSpecs.get(1)).hasName("execute");
    assertThat(typeSpec.methodSpecs.get(1)).hasParameters("instance", "flowScopeKey");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("// startEvent: startEvent");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(flowScopeKey, \"startEvent\");");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("// subProcess: subProcess");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("executeSubProcess(instance, flowScopeKey);");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("// endEvent: endEvent");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(flowScopeKey, \"endEvent\");");

    assertThat(typeSpec.methodSpecs.get(2)).hasName("executeSubProcess");
    assertThat(typeSpec.methodSpecs.get(2)).hasParameters("instance", "parentFlowScopeKey");
    assertThat(typeSpec.methodSpecs.get(2)).containsCode("long flowScopeKey = instance.getElementInstanceKey(parentFlowScopeKey, \"subProcess\");");
    assertThat(typeSpec.methodSpecs.get(2)).containsCode("// startEvent: subProcessStartEvent");
    assertThat(typeSpec.methodSpecs.get(2)).containsCode("instance.hasPassed(flowScopeKey, \"subProcessStartEvent\");");
    assertThat(typeSpec.methodSpecs.get(2)).containsCode("// endEvent: subProcessEndEvent");
    assertThat(typeSpec.methodSpecs.get(2)).containsCode("instance.hasPassed(flowScopeKey, \"subProcessEndEvent\");");

    typeSpec = result.getFiles().get(2).typeSpec;
    assertThat(typeSpec).hasMethods(9);

    assertThat(typeSpec.methodSpecs.get(1)).hasName("execute");
    assertThat(typeSpec.methodSpecs.get(1)).hasParameters("instance", "flowScopeKey");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("// subProcess: subProcess");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("executeSubProcess(instance, flowScopeKey);");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("// endEvent: endEvent");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(flowScopeKey, \"endEvent\");");

    assertThat(typeSpec.methodSpecs.get(2)).hasName("executeSubProcess");
    assertThat(typeSpec.methodSpecs.get(2)).hasParameters("instance", "parentFlowScopeKey");
    assertThat(typeSpec.methodSpecs.get(2)).containsCode("long flowScopeKey = instance.getElementInstanceKey(parentFlowScopeKey, \"subProcess\");");
    assertThat(typeSpec.methodSpecs.get(2)).containsCode("// startEvent: subProcessStartEvent");
    assertThat(typeSpec.methodSpecs.get(2)).containsCode("instance.hasPassed(flowScopeKey, \"subProcessStartEvent\");");
    assertThat(typeSpec.methodSpecs.get(2)).containsCode("// endEvent: subProcessEndEvent");
    assertThat(typeSpec.methodSpecs.get(2)).containsCode("instance.hasPassed(flowScopeKey, \"subProcessEndEvent\");");
  }

  @Test
  void testSimpleSubProcessNested() {
    generator.generateTestCases(ctx, bpmnFile);

    assertThat(result.getFiles()).hasSize(2);
    assertThat(result.getFiles().get(0).typeSpec).hasName("TC_startEvent__endEvent");
    assertThat(result.getFiles().get(1).typeSpec).hasName("TC_userTask__endEvent");

    var typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasMethods(10);

    assertThat(typeSpec.methodSpecs.get(1)).hasName("execute");
    assertThat(typeSpec.methodSpecs.get(1)).hasParameters("instance", "flowScopeKey");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("// startEvent: startEvent");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(flowScopeKey, \"startEvent\");");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("// subProcess: subProcess");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("executeSubProcess(instance, flowScopeKey);");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("// endEvent: endEvent");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(flowScopeKey, \"endEvent\");");

    assertThat(typeSpec.methodSpecs.get(2)).hasName("executeSubProcess");
    assertThat(typeSpec.methodSpecs.get(2)).hasParameters("instance", "parentFlowScopeKey");
    assertThat(typeSpec.methodSpecs.get(2)).containsCode("long flowScopeKey = instance.getElementInstanceKey(parentFlowScopeKey, \"subProcess\");");
    assertThat(typeSpec.methodSpecs.get(2)).containsCode("// startEvent: subProcessStartEvent");
    assertThat(typeSpec.methodSpecs.get(2)).containsCode("instance.hasPassed(flowScopeKey, \"subProcessStartEvent\");");
    assertThat(typeSpec.methodSpecs.get(2)).containsCode("// subProcess: nestedSubProcess");
    assertThat(typeSpec.methodSpecs.get(2)).containsCode("executeNestedSubProcess(instance, flowScopeKey);");
    assertThat(typeSpec.methodSpecs.get(2)).containsCode("// endEvent: subProcessEndEvent");
    assertThat(typeSpec.methodSpecs.get(2)).containsCode("instance.hasPassed(flowScopeKey, \"subProcessEndEvent\");");

    assertThat(typeSpec.methodSpecs.get(3)).hasName("executeNestedSubProcess");
    assertThat(typeSpec.methodSpecs.get(3)).hasParameters("instance", "parentFlowScopeKey");
    assertThat(typeSpec.methodSpecs.get(3)).containsCode("long flowScopeKey = instance.getElementInstanceKey(parentFlowScopeKey, \"nestedSubProcess\");");
    assertThat(typeSpec.methodSpecs.get(3)).containsCode("// startEvent: nestedSubProcessStartEvent");
    assertThat(typeSpec.methodSpecs.get(3)).containsCode("instance.hasPassed(flowScopeKey, \"nestedSubProcessStartEvent\");");
    assertThat(typeSpec.methodSpecs.get(3)).containsCode("// userTask: userTask");
    assertThat(typeSpec.methodSpecs.get(3)).containsCode("instance.isWaitingAt(flowScopeKey, \"userTask\");");
    assertThat(typeSpec.methodSpecs.get(3)).containsCode("instance.apply(flowScopeKey, userTask);");
    assertThat(typeSpec.methodSpecs.get(3)).containsCode("instance.hasPassed(flowScopeKey, \"userTask\");");
    assertThat(typeSpec.methodSpecs.get(3)).containsCode("// endEvent: nestedSubProcessEndEvent");
    assertThat(typeSpec.methodSpecs.get(3)).containsCode("instance.hasPassed(flowScopeKey, \"nestedSubProcessEndEvent\");");

    typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec).hasMethods(11);

    assertThat(typeSpec.methodSpecs.get(1)).hasName("execute");
    assertThat(typeSpec.methodSpecs.get(1)).hasParameters("instance", "flowScopeKey");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("// subProcess: subProcess");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("executeSubProcess(instance, flowScopeKey);");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("// endEvent: endEvent");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(flowScopeKey, \"endEvent\");");

    assertThat(typeSpec.methodSpecs.get(2)).hasName("executeSubProcess");
    assertThat(typeSpec.methodSpecs.get(2)).hasParameters("instance", "parentFlowScopeKey");
    assertThat(typeSpec.methodSpecs.get(2)).containsCode("long flowScopeKey = instance.getElementInstanceKey(parentFlowScopeKey, \"subProcess\");");
    assertThat(typeSpec.methodSpecs.get(2)).containsCode("// subProcess: nestedSubProcess");
    assertThat(typeSpec.methodSpecs.get(2)).containsCode("executeNestedSubProcess(instance, flowScopeKey);");
    assertThat(typeSpec.methodSpecs.get(2)).containsCode("// endEvent: subProcessEndEvent");
    assertThat(typeSpec.methodSpecs.get(2)).containsCode("instance.hasPassed(flowScopeKey, \"subProcessEndEvent\");");

    assertThat(typeSpec.methodSpecs.get(3)).hasName("executeNestedSubProcess");
    assertThat(typeSpec.methodSpecs.get(3)).hasParameters("instance", "parentFlowScopeKey");
    assertThat(typeSpec.methodSpecs.get(3)).containsCode("long flowScopeKey = instance.getElementInstanceKey(parentFlowScopeKey, \"nestedSubProcess\");");
    assertThat(typeSpec.methodSpecs.get(3)).containsCode("// userTask: userTask");
    assertThat(typeSpec.methodSpecs.get(3)).containsCode("instance.isWaitingAt(flowScopeKey, \"userTask\");");
    assertThat(typeSpec.methodSpecs.get(3)).containsCode("instance.apply(flowScopeKey, userTask);");
    assertThat(typeSpec.methodSpecs.get(3)).containsCode("instance.hasPassed(flowScopeKey, \"userTask\");");
    assertThat(typeSpec.methodSpecs.get(3)).containsCode("// endEvent: nestedSubProcessEndEvent");
    assertThat(typeSpec.methodSpecs.get(3)).containsCode("instance.hasPassed(flowScopeKey, \"nestedSubProcessEndEvent\");");

    assertThat(typeSpec.methodSpecs.get(9)).hasName("isProcessStart");
    assertThat(typeSpec.methodSpecs.get(9)).containsCode("return false;");
  }

  @Test
  void testSimpleTimerCatchEvent() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    var typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(8);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("timerCatchEvent");
    assertThat(typeSpec.fieldSpecs.get(0).type).isEqualTo(DefaultStrategy.TIMER_EVENT);

    assertThat(typeSpec.methodSpecs.get(7)).hasName("handleTimerCatchEvent");
    assertThat(typeSpec.methodSpecs.get(7)).hasReturnType(DefaultStrategy.TIMER_EVENT);

    var expected = "timerCatchEvent = new %s(timerCatchEventElement);";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format(expected, DefaultStrategy.TIMER_EVENT));

    expected = "timerCatchEventElement.id = \"timerCatchEvent\";";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);
    expected = "imerCatchEventElement.timeDuration = \"PT1H\";";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);

    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.isWaitingAt(flowScopeKey, \"timerCatchEvent\");");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(flowScopeKey, timerCatchEvent);");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(flowScopeKey, \"timerCatchEvent\");");
  }

  @Test
  void testSimpleTimerStartEvent() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    var typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(0);
    assertThat(typeSpec).hasMethods(8);

    assertThat(typeSpec.methodSpecs.get(7)).hasName("isTimerStart");
    assertThat(typeSpec.methodSpecs.get(7)).hasReturnType(TypeName.BOOLEAN);
    assertThat(typeSpec.methodSpecs.get(7)).containsCode("return true");
  }

  @Test
  void testSimpleUserTask() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

    var typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(3);
    assertThat(typeSpec).hasMethods(10);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("userTask");
    assertThat(typeSpec.fieldSpecs.get(0).type).isEqualTo(DefaultStrategy.USER_TASK);

    assertThat(typeSpec.methodSpecs.get(7)).hasName("handleUserTask");
    assertThat(typeSpec.methodSpecs.get(7)).hasReturnType(DefaultStrategy.USER_TASK);

    var expected = "userTask = new %s(userTaskElement);";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format(expected, DefaultStrategy.USER_TASK));

    expected = "userTaskElement.id = \"userTask\";";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);
    expected = "userTaskElement.assignee = \"=\\\"simpleAssignee\\\"\";";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);
    expected = "userTaskElement.candidateGroups = \"=[\\\"simpleGroupA\\\", \\\"simpleGroupB\\\"]\";";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);
    expected = "userTaskElement.candidateUsers = \"=[\\\"simpleUserA\\\", \\\"simpleUserB\\\"]\";";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);
    expected = "userTaskElement.dueDate = \"=\\\"2023-02-17T00:00:00Z\\\"\";";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);
    expected = "userTaskElement.followUpDate = \"=\\\"2023-02-18T00:00:00Z\\\"\";";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);

    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.isWaitingAt(flowScopeKey, \"userTask\");");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(flowScopeKey, userTask);");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(flowScopeKey, \"userTask\");");
  }

  /**
   * Tests the complete generation.
   */
  @Test
  void testGenerate() {
    generator.generate(ctx);

    Predicate<String> isFile = (className) -> Files.isRegularFile(ctx.getTestSourcePath().resolve(className));

    // test cases
    assertThat(isFile.test("org/example/simple/TC_startEvent__endEvent.java")).isTrue();
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
