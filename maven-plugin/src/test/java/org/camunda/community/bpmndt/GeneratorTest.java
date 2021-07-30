package org.camunda.community.bpmndt;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.community.bpmndt.api.AbstractJUnit4SpringBasedTestRule;
import org.camunda.community.bpmndt.api.AbstractJUnit4TestRule;
import org.camunda.community.bpmndt.api.CallActivityHandler;
import org.camunda.community.bpmndt.api.ExternalTaskHandler;
import org.camunda.community.bpmndt.api.IntermediateCatchEventHandler;
import org.camunda.community.bpmndt.api.JobHandler;
import org.camunda.community.bpmndt.api.UserTaskHandler;
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.junit.runner.Description;
import org.mockito.Mockito;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class GeneratorTest {

  private static final TypeName CALL_ACTIVITY_HANDLER = ClassName.get(CallActivityHandler.class);
  private static final TypeName EXTERNAL_TASK_HANDLER = ClassName.get(ExternalTaskHandler.class);
  private static final TypeName INTERMEDIATE_CATCH_EVENT_HANDLER = ClassName.get(IntermediateCatchEventHandler.class);
  private static final TypeName JOB_HANDLER = ClassName.get(JobHandler.class);
  private static final TypeName USER_TASK_HANDLER = ClassName.get(UserTaskHandler.class);

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

    result = new GeneratorResult();

    String fileName = testName.getMethodName().replace("test", "") + ".bpmn";
    bpmnFile = ctx.getMainResourcePath().resolve("bpmn").resolve(StringUtils.uncapitalize(fileName));
  }

  /**
   * Should generate the first test case and skip the second. Since the second test case has the same
   * name as the first, which is not allowed.
   */
  @Test
  public void testDuplicateTestCaseNames() {
    generator.generateTestCases(ctx, result, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.name, equalTo("TC_duplicateTestCaseNames__startEvent__endEvent"));
  }
  
  @Test
  public void testInvalid() {
    generator.generateTestCases(ctx, result, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.name, equalTo("TC_invalid__startEvent__endEvent"));

    TypeName superclass = ClassName.get(AbstractJUnit4TestRule.class);
    assertThat(typeSpec.superclass, equalTo(superclass));
    assertThat(typeSpec.fieldSpecs, hasSize(0));
    assertThat(typeSpec.methodSpecs, hasSize(7));
    assertThat(typeSpec.methodSpecs.get(0).name, equalTo("starting"));
    assertThat(typeSpec.methodSpecs.get(0).code.toString(), containsString("// Not existing flow nodes"));
    assertThat(typeSpec.methodSpecs.get(0).code.toString(), containsString("// a"));
    assertThat(typeSpec.methodSpecs.get(0).code.toString(), containsString("// b"));
    assertThat(typeSpec.methodSpecs.get(0).code.toString(), containsString("throw new java.lang.RuntimeException(\"Path is invalid\");"));

    assertThat(typeSpec.methodSpecs.get(1).name, equalTo("execute"));
    assertThat(typeSpec.methodSpecs.get(1).code.toString(), containsString("// Not existing flow nodes"));
    assertThat(typeSpec.methodSpecs.get(1).code.toString(), containsString("// a"));
    assertThat(typeSpec.methodSpecs.get(1).code.toString(), containsString("// b"));
    assertThat(typeSpec.methodSpecs.get(1).code.toString(), containsString("throw new java.lang.RuntimeException(\"Path is invalid\");"));
  }

  @Test
  public void testHappyPath() {
    generator.generateTestCases(ctx, result, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.name, equalTo("TC_happy_path__Happy_Path"));
  }

  @Test
  public void testNoTestCases() {
    generator.generateTestCases(ctx, result, bpmnFile);
    assertThat(result.getFiles(), hasSize(0));
  }

  @Test
  public void testSimple() {
    generator.generateTestCases(ctx, result, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));

    JavaFile javaFile = result.getFiles().get(0);
    assertThat(javaFile.packageName, equalTo(ctx.getPackageName()));
    assertThat(javaFile.skipJavaLangImports, is(true));
    assertThat(javaFile.typeSpec, notNullValue());

    TypeSpec typeSpec = javaFile.typeSpec;
    assertThat(typeSpec.name, equalTo("TC_simple__startEvent__endEvent"));

    TypeName superclass = ClassName.get(AbstractJUnit4TestRule.class);
    assertThat(typeSpec.superclass, equalTo(superclass));
    assertThat(typeSpec.fieldSpecs, hasSize(0));
    assertThat(typeSpec.methodSpecs, hasSize(7));
    assertThat(typeSpec.methodSpecs.get(0).name, equalTo("starting"));
    assertThat(typeSpec.methodSpecs.get(0).parameters, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(0).parameters.get(0).name, equalTo("description"));
    assertThat(typeSpec.methodSpecs.get(0).parameters.get(0).type, equalTo(ClassName.get(Description.class)));
    assertThat(typeSpec.methodSpecs.get(1).name, equalTo("execute"));
    assertThat(typeSpec.methodSpecs.get(1).parameters, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(1).parameters.get(0).name, equalTo("pi"));
    assertThat(typeSpec.methodSpecs.get(1).parameters.get(0).type, equalTo(ClassName.get(ProcessInstance.class)));
    assertThat(typeSpec.methodSpecs.get(2).name, equalTo("getBpmnResourceName"));
    assertThat(typeSpec.methodSpecs.get(2).code.toString(), containsString("\"bpmn/simple.bpmn\""));
    assertThat(typeSpec.methodSpecs.get(3).name, equalTo("getEnd"));
    assertThat(typeSpec.methodSpecs.get(3).code.toString(), containsString("\"endEvent\""));
    assertThat(typeSpec.methodSpecs.get(4).name, equalTo("getProcessDefinitionKey"));
    assertThat(typeSpec.methodSpecs.get(4).code.toString(), containsString("\"simple\""));
    assertThat(typeSpec.methodSpecs.get(5).name, equalTo("getProcessEnginePlugins"));
    assertThat(typeSpec.methodSpecs.get(5).code.toString(), containsString(SpinProcessEnginePlugin.class.getName()));
    assertThat(typeSpec.methodSpecs.get(6).name, equalTo("getStart"));
    assertThat(typeSpec.methodSpecs.get(6).code.toString(), containsString("\"startEvent\""));
  }

  /**
   * Should be the same as {@link #testSimple()}, when Spring is enabled.
   */
  @Test
  public void testSimpleSpringEnabled() {
    ctx.setSpringEnabled(true);

    // overwrite auto built BPMN file path
    bpmnFile = ctx.getMainResourcePath().resolve("bpmn/simple.bpmn");

    generator.generateTestCases(ctx, result, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));

    JavaFile javaFile = result.getFiles().get(0);
    assertThat(javaFile.packageName, equalTo(ctx.getPackageName()));
    assertThat(javaFile.skipJavaLangImports, is(true));
    assertThat(javaFile.typeSpec, notNullValue());

    TypeSpec typeSpec = javaFile.typeSpec;
    assertThat(typeSpec.name, equalTo("TC_simple__startEvent__endEvent"));

    TypeName superclass = ClassName.get(AbstractJUnit4SpringBasedTestRule.class);
    assertThat(typeSpec.superclass, equalTo(superclass));
    assertThat(typeSpec.fieldSpecs, hasSize(0));
    assertThat(typeSpec.methodSpecs, hasSize(7));
    assertThat(typeSpec.methodSpecs.get(0).name, equalTo("starting"));
    assertThat(typeSpec.methodSpecs.get(0).parameters, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(0).parameters.get(0).name, equalTo("description"));
    assertThat(typeSpec.methodSpecs.get(0).parameters.get(0).type, equalTo(ClassName.get(Description.class)));
    assertThat(typeSpec.methodSpecs.get(1).name, equalTo("execute"));
    assertThat(typeSpec.methodSpecs.get(1).parameters, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(1).parameters.get(0).name, equalTo("pi"));
    assertThat(typeSpec.methodSpecs.get(1).parameters.get(0).type, equalTo(ClassName.get(ProcessInstance.class)));
    assertThat(typeSpec.methodSpecs.get(2).name, equalTo("getBpmnResourceName"));
    assertThat(typeSpec.methodSpecs.get(2).code.toString(), containsString("\"bpmn/simple.bpmn\""));
    assertThat(typeSpec.methodSpecs.get(3).name, equalTo("getEnd"));
    assertThat(typeSpec.methodSpecs.get(3).code.toString(), containsString("\"endEvent\""));
    assertThat(typeSpec.methodSpecs.get(4).name, equalTo("getProcessDefinitionKey"));
    assertThat(typeSpec.methodSpecs.get(4).code.toString(), containsString("\"simple\""));
    assertThat(typeSpec.methodSpecs.get(5).name, equalTo("getProcessEnginePlugins"));
    assertThat(typeSpec.methodSpecs.get(5).code.toString(), containsString(SpinProcessEnginePlugin.class.getName()));
    assertThat(typeSpec.methodSpecs.get(6).name, equalTo("getStart"));
    assertThat(typeSpec.methodSpecs.get(6).code.toString(), containsString("\"startEvent\""));
  }

  @Test
  public void testSimpleAsync() {
    generator.generateTestCases(ctx, result, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.fieldSpecs, hasSize(2));
    assertThat(typeSpec.fieldSpecs.get(0).name, equalTo("startEventAfter"));
    assertThat(typeSpec.fieldSpecs.get(0).type, equalTo(JOB_HANDLER));
    assertThat(typeSpec.fieldSpecs.get(1).name, equalTo("endEventBefore"));
    assertThat(typeSpec.fieldSpecs.get(1).type, equalTo(JOB_HANDLER));
    assertThat(typeSpec.methodSpecs, hasSize(9));
    assertThat(typeSpec.methodSpecs.get(7).name, equalTo("handleStartEventAfter"));
    assertThat(typeSpec.methodSpecs.get(7).returnType, equalTo(JOB_HANDLER));
    assertThat(typeSpec.methodSpecs.get(8).name, equalTo("handleEndEventBefore"));
    assertThat(typeSpec.methodSpecs.get(8).returnType, equalTo(JOB_HANDLER));

    String expected;

    String startingCode = typeSpec.methodSpecs.get(0).code.toString();
    expected = String.format("startEventAfter = new %s(getProcessEngine(), \"startEvent\");", JOB_HANDLER);
    assertThat(startingCode, containsString(expected));
    expected = String.format("endEventBefore = new %s(getProcessEngine(), \"endEvent\");", JOB_HANDLER);
    assertThat(startingCode, containsString(expected));

    String executeCode = typeSpec.methodSpecs.get(1).code.toString();
    assertThat(executeCode, containsString("instance.apply(startEventAfter);"));
    assertThat(executeCode, containsString("instance.apply(endEventBefore);"));
  }

  @Test
  public void testSimpleCallActivity() {
    generator.generateTestCases(ctx, result, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.methodSpecs, hasSize(8));
    assertThat(typeSpec.methodSpecs.get(7).name, equalTo("handleCallActivity"));
    assertThat(typeSpec.methodSpecs.get(7).returnType, equalTo(CALL_ACTIVITY_HANDLER));

    String startingCode = typeSpec.methodSpecs.get(0).code.toString();
    String expected = String.format("callActivity = new %s(instance, \"callActivity\");", CALL_ACTIVITY_HANDLER);
    assertThat(startingCode, containsString(expected));
  }

  @Test
  public void testSimpleExternalTask() {
    generator.generateTestCases(ctx, result, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.fieldSpecs, hasSize(1));
    assertThat(typeSpec.fieldSpecs.get(0).name, equalTo("externalTask"));
    assertThat(typeSpec.fieldSpecs.get(0).type, equalTo(EXTERNAL_TASK_HANDLER));
    assertThat(typeSpec.methodSpecs, hasSize(8));
    assertThat(typeSpec.methodSpecs.get(7).name, equalTo("handleExternalTask"));
    assertThat(typeSpec.methodSpecs.get(7).returnType, equalTo(EXTERNAL_TASK_HANDLER));

    String startingCode = typeSpec.methodSpecs.get(0).code.toString();
    String expected = String.format("externalTask = new %s(getProcessEngine(), \"test-topic\");", EXTERNAL_TASK_HANDLER);
    assertThat(startingCode, containsString(expected));

    String executeCode = typeSpec.methodSpecs.get(1).code.toString();
    assertThat(executeCode, containsString("instance.apply(externalTask);"));
  }

  @Test
  public void testSimpleMessageCatchEvent() {
    generator.generateTestCases(ctx, result, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.fieldSpecs, hasSize(1));
    assertThat(typeSpec.fieldSpecs.get(0).name, equalTo("messageCatchEvent"));
    assertThat(typeSpec.fieldSpecs.get(0).type, equalTo(INTERMEDIATE_CATCH_EVENT_HANDLER));
    assertThat(typeSpec.methodSpecs, hasSize(8));
    assertThat(typeSpec.methodSpecs.get(7).name, equalTo("handleMessageCatchEvent"));
    assertThat(typeSpec.methodSpecs.get(7).returnType, equalTo(INTERMEDIATE_CATCH_EVENT_HANDLER));

    String startingCode = typeSpec.methodSpecs.get(0).code.toString();
    String expected = "messageCatchEvent = new %s(getProcessEngine(), \"messageCatchEvent\", \"simpleMessage\");";
    assertThat(startingCode, containsString(String.format(expected, INTERMEDIATE_CATCH_EVENT_HANDLER)));

    String executeCode = typeSpec.methodSpecs.get(1).code.toString();
    assertThat(executeCode, containsString("instance.apply(messageCatchEvent);"));
  }

  @Test
  public void testSimpleSignalCatchEvent() {
    generator.generateTestCases(ctx, result, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.fieldSpecs, hasSize(1));
    assertThat(typeSpec.fieldSpecs.get(0).name, equalTo("signalCatchEvent"));
    assertThat(typeSpec.fieldSpecs.get(0).type, equalTo(ClassName.get(IntermediateCatchEventHandler.class)));
    assertThat(typeSpec.methodSpecs, hasSize(8));
    assertThat(typeSpec.methodSpecs.get(7).name, equalTo("handleSignalCatchEvent"));
    assertThat(typeSpec.methodSpecs.get(7).returnType, equalTo(ClassName.get(IntermediateCatchEventHandler.class)));

    String startingCode = typeSpec.methodSpecs.get(0).code.toString();
    String expected = "signalCatchEvent = new %s(getProcessEngine(), \"signalCatchEvent\", \"simpleSignal\");";
    assertThat(startingCode, containsString(String.format(expected, INTERMEDIATE_CATCH_EVENT_HANDLER)));

    String executeCode = typeSpec.methodSpecs.get(1).code.toString();
    assertThat(executeCode, containsString("instance.apply(signalCatchEvent);"));
  }

  @Test
  public void testSimpleSubProcess() {
    generator.generateTestCases(ctx, result, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.methodSpecs, hasSize(7));

    String executeCode = typeSpec.methodSpecs.get(1).code.toString();
    assertThat(executeCode, containsString("// startEvent: subProcessStartEvent"));
    assertThat(executeCode, containsString("assertThat(pi).hasPassed(\"subProcessStartEvent\");"));
    assertThat(executeCode, containsString("// endEvent: subProcessEndEvent"));
    assertThat(executeCode, containsString("assertThat(pi).hasPassed(\"subProcessEndEvent\");"));
  }

  @Test
  public void testSimpleTimerCatchEvent() {
    generator.generateTestCases(ctx, result, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.fieldSpecs, hasSize(1));
    assertThat(typeSpec.fieldSpecs.get(0).name, equalTo("timerCatchEvent"));
    assertThat(typeSpec.fieldSpecs.get(0).type, equalTo(JOB_HANDLER));
    assertThat(typeSpec.methodSpecs, hasSize(8));
    assertThat(typeSpec.methodSpecs.get(7).name, equalTo("handleTimerCatchEvent"));
    assertThat(typeSpec.methodSpecs.get(7).returnType, equalTo(JOB_HANDLER));

    String startingCode = typeSpec.methodSpecs.get(0).code.toString();
    String expected = String.format("timerCatchEvent = new %s(getProcessEngine(), \"timerCatchEvent\");", JOB_HANDLER);
    assertThat(startingCode, containsString(expected));

    String executeCode = typeSpec.methodSpecs.get(1).code.toString();
    assertThat(executeCode, containsString("instance.apply(timerCatchEvent);"));
  }

  @Test
  public void testSimpleUserTask() {
    generator.generateTestCases(ctx, result, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.fieldSpecs, hasSize(1));
    assertThat(typeSpec.fieldSpecs.get(0).name, equalTo("userTask"));
    assertThat(typeSpec.fieldSpecs.get(0).type, equalTo(USER_TASK_HANDLER));
    assertThat(typeSpec.methodSpecs, hasSize(8));
    assertThat(typeSpec.methodSpecs.get(7).name, equalTo("handleUserTask"));
    assertThat(typeSpec.methodSpecs.get(7).returnType, equalTo(USER_TASK_HANDLER));

    String startingCode = typeSpec.methodSpecs.get(0).code.toString();
    String expected = String.format("userTask = new %s(getProcessEngine(), \"userTask\");", USER_TASK_HANDLER);
    assertThat(startingCode, containsString(expected));

    String executeCode = typeSpec.methodSpecs.get(1).code.toString();
    assertThat(executeCode, containsString("instance.apply(userTask);"));
  }

  /**
   * Tests the complete task execution.
   */
  @Test
  public void testExecute() {
    generator.generate(ctx);
    
    Predicate<String> isFile = (className) -> {
      return Files.isRegularFile(ctx.getTestSourcePath().resolve(className));
    };
    
    // test cases
    assertThat(isFile.test("org/example/TC_simple__startEvent__endEvent.java"), is(true));
    assertThat(isFile.test("org/example/TC_simpleAsync__startEvent__endEvent.java"), is(true));
    assertThat(isFile.test("org/example/TC_simpleCallActivity__startEvent__endEvent.java"), is(true));
    assertThat(isFile.test("org/example/TC_simpleExternalTask__startEvent__endEvent.java"), is(true));
    assertThat(isFile.test("org/example/TC_simpleMessageCatchEvent__startEvent__endEvent.java"), is(true));
    assertThat(isFile.test("org/example/TC_simpleSignalCatchEvent__startEvent__endEvent.java"), is(true));
    assertThat(isFile.test("org/example/TC_simpleSubProcess__startEvent__endEvent.java"), is(true));
    assertThat(isFile.test("org/example/TC_simpleUserTask__startEvent__endEvent.java"), is(true));

    // should not exist, since the BPMN process provides no test cases
    assertThat(isFile.test("org/example/TC_noTestCases__startEvent__endEvent.java"), is(false));

    // API classes
    assertThat(isFile.test("org/camunda/community/bpmndt/api/AbstractJUnit4TestRule.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/CallActivityDefinition.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/CallActivityHandler.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/ExternalTaskHandler.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/IntermediateCatchEventHandler.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/JobHandler.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/TestCaseInstance.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/TestCaseExecutor.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/UserTaskHandler.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/cfg/BpmndtCallActivityBehavior.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/cfg/BpmndtParseListener.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/cfg/BpmndtProcessEnginePlugin.java"), is(true));
  }

  /**
   * Tests the complete task execution with Spring enabled.
   */
  @Test
  public void testExecuteSpringEnabled() {
    ctx.setSpringEnabled(true);

    generator.generate(ctx);

    Predicate<String> isFile = (className) -> {
      return Files.isRegularFile(ctx.getTestSourcePath().resolve(className));
    };

    // Spring configuration
    assertThat(isFile.test("org/example/BpmndtConfiguration.java"), is(true));

    // API classes
    assertThat(isFile.test("org/camunda/community/bpmndt/api/AbstractJUnit4SpringBasedTestRule.java"), is(true));
    assertThat(isFile.test("org/camunda/community/bpmndt/api/cfg/AbstractConfiguration.java"), is(true));
  }
}
