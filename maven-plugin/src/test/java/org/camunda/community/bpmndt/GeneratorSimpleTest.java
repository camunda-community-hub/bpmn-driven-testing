package org.camunda.community.bpmndt;

import static org.camunda.community.bpmndt.test.ContainsCode.containsCode;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;

import javax.lang.model.element.Modifier;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.community.bpmndt.api.AbstractJUnit4TestCase;
import org.camunda.community.bpmndt.api.CallActivityHandler;
import org.camunda.community.bpmndt.api.EventHandler;
import org.camunda.community.bpmndt.api.ExternalTaskHandler;
import org.camunda.community.bpmndt.api.JobHandler;
import org.camunda.community.bpmndt.api.UserTaskHandler;
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

public class GeneratorSimpleTest {

  private static final TypeName CALL_ACTIVITY_HANDLER = TypeName.get(CallActivityHandler.class);
  private static final TypeName EVENT_HANDLER = TypeName.get(EventHandler.class);
  private static final TypeName EXTERNAL_TASK_HANDLER = TypeName.get(ExternalTaskHandler.class);
  private static final TypeName JOB_HANDLER = TypeName.get(JobHandler.class);
  private static final TypeName USER_TASK_HANDLER = TypeName.get(UserTaskHandler.class);

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
    ctx.setMainResourcePath(Paths.get("./src/test/it/simple/src/main/resources"));
    ctx.setTestSourcePath(temporaryFolder.getRoot().toPath());

    ctx.setPackageName("org.example");

    result = generator.getResult();

    String fileName = testName.getMethodName().replace("test", "") + ".bpmn";
    bpmnFile = ctx.getMainResourcePath().resolve(StringUtils.uncapitalize(fileName));
  }

  @Test
  public void testSimple() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));

    JavaFile javaFile = result.getFiles().get(0);
    assertThat(javaFile.packageName, equalTo("org.example.simple"));
    assertThat(javaFile.skipJavaLangImports, is(true));
    assertThat(javaFile.typeSpec, notNullValue());

    TypeSpec typeSpec = javaFile.typeSpec;
    assertThat(typeSpec.name, equalTo("TC_startEvent__endEvent"));

    TypeName superclass = ClassName.get(AbstractJUnit4TestCase.class);
    assertThat(typeSpec.superclass, equalTo(superclass));
    assertThat(typeSpec.fieldSpecs, hasSize(0));
    assertThat(typeSpec.methodSpecs, hasSize(6));
    assertThat(typeSpec.methodSpecs.get(0).name, equalTo("starting"));
    assertThat(typeSpec.methodSpecs.get(0).parameters, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(0).parameters.get(0).name, equalTo("description"));
    assertThat(typeSpec.methodSpecs.get(0).parameters.get(0).type, equalTo(ClassName.get(Description.class)));
    assertThat(typeSpec.methodSpecs.get(1).name, equalTo("execute"));
    assertThat(typeSpec.methodSpecs.get(1).parameters, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(1).parameters.get(0).name, equalTo("pi"));
    assertThat(typeSpec.methodSpecs.get(1).parameters.get(0).type, equalTo(ClassName.get(ProcessInstance.class)));
    assertThat(typeSpec.methodSpecs.get(2).name, equalTo("getBpmnResourceName"));
    assertThat(typeSpec.methodSpecs.get(2).code.toString(), containsString("\"simple.bpmn\""));
    assertThat(typeSpec.methodSpecs.get(3).name, equalTo("getEnd"));
    assertThat(typeSpec.methodSpecs.get(3).code.toString(), containsString("\"endEvent\""));
    assertThat(typeSpec.methodSpecs.get(4).name, equalTo("getProcessDefinitionKey"));
    assertThat(typeSpec.methodSpecs.get(4).code.toString(), containsString("\"simple\""));
    assertThat(typeSpec.methodSpecs.get(5).name, equalTo("getStart"));
    assertThat(typeSpec.methodSpecs.get(5).code.toString(), containsString("\"startEvent\""));
  }

  /**
   * Should be the same as {@link #testSimple()}, when Spring is enabled.
   */
  @Test
  public void testSimpleSpringEnabled() {
    ctx.setSpringEnabled(true);

    // overridde auto built BPMN file path
    bpmnFile = ctx.getMainResourcePath().resolve("simple.bpmn");

    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));

    JavaFile javaFile = result.getFiles().get(0);
    assertThat(javaFile.packageName, equalTo("org.example.simple"));
    assertThat(javaFile.skipJavaLangImports, is(true));
    assertThat(javaFile.typeSpec, notNullValue());

    TypeSpec typeSpec = javaFile.typeSpec;
    assertThat(typeSpec.name, equalTo("TC_startEvent__endEvent"));

    assertThat(typeSpec.superclass, equalTo(ClassName.get(AbstractJUnit4TestCase.class)));
    assertThat(typeSpec.fieldSpecs, hasSize(0));
    assertThat(typeSpec.methodSpecs, hasSize(7));
    assertThat(typeSpec.methodSpecs.get(0).isConstructor(), is(true));
    assertThat(typeSpec.methodSpecs.get(0).modifiers, hasItem(Modifier.PUBLIC));
    assertThat(typeSpec.methodSpecs.get(1).name, equalTo("starting"));
    assertThat(typeSpec.methodSpecs.get(1).parameters, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(1).parameters.get(0).name, equalTo("description"));
    assertThat(typeSpec.methodSpecs.get(1).parameters.get(0).type, equalTo(ClassName.get(Description.class)));
    assertThat(typeSpec.methodSpecs.get(2).name, equalTo("execute"));
    assertThat(typeSpec.methodSpecs.get(2).parameters, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(2).parameters.get(0).name, equalTo("pi"));
    assertThat(typeSpec.methodSpecs.get(2).parameters.get(0).type, equalTo(ClassName.get(ProcessInstance.class)));
    assertThat(typeSpec.methodSpecs.get(3).name, equalTo("getBpmnResourceName"));
    assertThat(typeSpec.methodSpecs.get(3).code.toString(), containsString("\"simple.bpmn\""));
    assertThat(typeSpec.methodSpecs.get(4).name, equalTo("getEnd"));
    assertThat(typeSpec.methodSpecs.get(4).code.toString(), containsString("\"endEvent\""));
    assertThat(typeSpec.methodSpecs.get(5).name, equalTo("getProcessDefinitionKey"));
    assertThat(typeSpec.methodSpecs.get(5).code.toString(), containsString("\"simple\""));
    assertThat(typeSpec.methodSpecs.get(6).name, equalTo("getStart"));
    assertThat(typeSpec.methodSpecs.get(6).code.toString(), containsString("\"startEvent\""));
  }

  @Test
  public void testSimpleAsync() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.fieldSpecs, hasSize(2));
    assertThat(typeSpec.fieldSpecs.get(0).name, equalTo("startEventAfter"));
    assertThat(typeSpec.fieldSpecs.get(0).type, equalTo(JOB_HANDLER));
    assertThat(typeSpec.fieldSpecs.get(1).name, equalTo("endEventBefore"));
    assertThat(typeSpec.fieldSpecs.get(1).type, equalTo(JOB_HANDLER));
    assertThat(typeSpec.methodSpecs, hasSize(8));
    assertThat(typeSpec.methodSpecs.get(6).name, equalTo("handleStartEventAfter"));
    assertThat(typeSpec.methodSpecs.get(6).returnType, equalTo(JOB_HANDLER));
    assertThat(typeSpec.methodSpecs.get(7).name, equalTo("handleEndEventBefore"));
    assertThat(typeSpec.methodSpecs.get(7).returnType, equalTo(JOB_HANDLER));

    containsCode(typeSpec.methodSpecs.get(0))
        .contains(String.format("startEventAfter = new %s(getProcessEngine(), \"startEvent\");", JOB_HANDLER))
        .contains(String.format("endEventBefore = new %s(getProcessEngine(), \"endEvent\");", JOB_HANDLER));

    containsCode(typeSpec.methodSpecs.get(1))
        .contains("instance.apply(startEventAfter);")
        .contains("instance.apply(endEventBefore);");
  }

  @Test
  public void testSimpleCallActivity() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.methodSpecs, hasSize(7));
    assertThat(typeSpec.methodSpecs.get(6).name, equalTo("handleCallActivity"));
    assertThat(typeSpec.methodSpecs.get(6).returnType, equalTo(CALL_ACTIVITY_HANDLER));

    String expected = "callActivity = new %s(instance, \"callActivity\");";
    containsCode(typeSpec.methodSpecs.get(0)).contains(String.format(expected, CALL_ACTIVITY_HANDLER));
  }

  @Test
  public void testSimpleCollaboration() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.methodSpecs, hasSize(6));
  }

  @Test
  public void testSimpleConditionalCatchEvent() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.fieldSpecs, hasSize(1));
    assertThat(typeSpec.fieldSpecs.get(0).name, equalTo("conditionalCatchEvent"));
    assertThat(typeSpec.fieldSpecs.get(0).type, equalTo(EVENT_HANDLER));
    assertThat(typeSpec.methodSpecs, hasSize(7));
    assertThat(typeSpec.methodSpecs.get(6).name, equalTo("handleConditionalCatchEvent"));
    assertThat(typeSpec.methodSpecs.get(6).returnType, equalTo(EVENT_HANDLER));

    String expected = "conditionalCatchEvent = new %s(getProcessEngine(), \"conditionalCatchEvent\", null);";
    containsCode(typeSpec.methodSpecs.get(0)).contains(String.format(expected, EVENT_HANDLER));
    containsCode(typeSpec.methodSpecs.get(1)).contains("instance.apply(conditionalCatchEvent);");
  }

  @Test
  public void testSimpleExternalTask() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.fieldSpecs, hasSize(1));
    assertThat(typeSpec.fieldSpecs.get(0).name, equalTo("externalTask"));
    assertThat(typeSpec.fieldSpecs.get(0).type, equalTo(EXTERNAL_TASK_HANDLER));
    assertThat(typeSpec.methodSpecs, hasSize(7));
    assertThat(typeSpec.methodSpecs.get(6).name, equalTo("handleExternalTask"));
    assertThat(typeSpec.methodSpecs.get(6).returnType, equalTo(EXTERNAL_TASK_HANDLER));

    String expected = "externalTask = new %s(getProcessEngine(), \"externalTask\", \"test-topic\");";
    containsCode(typeSpec.methodSpecs.get(0)).contains(String.format(expected, EXTERNAL_TASK_HANDLER));
    containsCode(typeSpec.methodSpecs.get(1)).contains("instance.apply(externalTask);");
  }

  @Test
  public void testSimpleMessageCatchEvent() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.fieldSpecs, hasSize(1));
    assertThat(typeSpec.fieldSpecs.get(0).name, equalTo("messageCatchEvent"));
    assertThat(typeSpec.fieldSpecs.get(0).type, equalTo(EVENT_HANDLER));
    assertThat(typeSpec.methodSpecs, hasSize(7));
    assertThat(typeSpec.methodSpecs.get(6).name, equalTo("handleMessageCatchEvent"));
    assertThat(typeSpec.methodSpecs.get(6).returnType, equalTo(EVENT_HANDLER));

    String expected = "messageCatchEvent = new %s(getProcessEngine(), \"messageCatchEvent\", \"simpleMessage\");";
    containsCode(typeSpec.methodSpecs.get(0)).contains(String.format(expected, EVENT_HANDLER));
    containsCode(typeSpec.methodSpecs.get(1)).contains("instance.apply(messageCatchEvent);");
  }

  @Test
  public void testSimpleReceiveTask() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.fieldSpecs, hasSize(1));
    assertThat(typeSpec.fieldSpecs.get(0).name, equalTo("receiveTask"));
    assertThat(typeSpec.fieldSpecs.get(0).type, equalTo(EVENT_HANDLER));
    assertThat(typeSpec.methodSpecs, hasSize(7));
    assertThat(typeSpec.methodSpecs.get(6).name, equalTo("handleReceiveTask"));
    assertThat(typeSpec.methodSpecs.get(6).returnType, equalTo(EVENT_HANDLER));

    String expected = "receiveTask = new %s(getProcessEngine(), \"receiveTask\", \"simpleMessage\");";
    containsCode(typeSpec.methodSpecs.get(0)).contains(String.format(expected, EVENT_HANDLER));
    containsCode(typeSpec.methodSpecs.get(1)).contains("instance.apply(receiveTask);");
  }

  @Test
  public void testSimpleSignalCatchEvent() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.fieldSpecs, hasSize(1));
    assertThat(typeSpec.fieldSpecs.get(0).name, equalTo("signalCatchEvent"));
    assertThat(typeSpec.fieldSpecs.get(0).type, equalTo(ClassName.get(EventHandler.class)));
    assertThat(typeSpec.methodSpecs, hasSize(7));
    assertThat(typeSpec.methodSpecs.get(6).name, equalTo("handleSignalCatchEvent"));
    assertThat(typeSpec.methodSpecs.get(6).returnType, equalTo(ClassName.get(EventHandler.class)));

    String expected = "signalCatchEvent = new %s(getProcessEngine(), \"signalCatchEvent\", \"simpleSignal\");";
    containsCode(typeSpec.methodSpecs.get(0)).contains(String.format(expected, EVENT_HANDLER));
    containsCode(typeSpec.methodSpecs.get(1)).contains("instance.apply(signalCatchEvent);");
  }

  @Test
  public void testSimpleSubProcess() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.methodSpecs, hasSize(6));

    containsCode(typeSpec.methodSpecs.get(1))
        .contains("// startEvent: subProcessStartEvent")
        .contains("assertThat(pi).hasPassed(\"subProcessStartEvent\");")
        .contains("// endEvent: subProcessEndEvent")
        .contains("assertThat(pi).hasPassed(\"subProcessEndEvent\");");
  }

  @Test
  public void testSimpleTimerCatchEvent() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.fieldSpecs, hasSize(1));
    assertThat(typeSpec.fieldSpecs.get(0).name, equalTo("timerCatchEvent"));
    assertThat(typeSpec.fieldSpecs.get(0).type, equalTo(JOB_HANDLER));
    assertThat(typeSpec.methodSpecs, hasSize(7));
    assertThat(typeSpec.methodSpecs.get(6).name, equalTo("handleTimerCatchEvent"));
    assertThat(typeSpec.methodSpecs.get(6).returnType, equalTo(JOB_HANDLER));

    String expected = "timerCatchEvent = new %s(getProcessEngine(), \"timerCatchEvent\");";
    containsCode(typeSpec.methodSpecs.get(0)).contains(String.format(expected, JOB_HANDLER));
    containsCode(typeSpec.methodSpecs.get(1)).contains("instance.apply(timerCatchEvent);");
  }

  @Test
  public void testSimpleUserTask() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(1));

    TypeSpec typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.fieldSpecs, hasSize(1));
    assertThat(typeSpec.fieldSpecs.get(0).name, equalTo("userTask"));
    assertThat(typeSpec.fieldSpecs.get(0).type, equalTo(USER_TASK_HANDLER));
    assertThat(typeSpec.methodSpecs, hasSize(7));
    assertThat(typeSpec.methodSpecs.get(6).name, equalTo("handleUserTask"));
    assertThat(typeSpec.methodSpecs.get(6).returnType, equalTo(USER_TASK_HANDLER));

    String expected = "userTask = new %s(getProcessEngine(), \"userTask\");";
    containsCode(typeSpec.methodSpecs.get(0)).contains(String.format(expected, USER_TASK_HANDLER));
    containsCode(typeSpec.methodSpecs.get(1)).contains("instance.apply(userTask);");
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
    assertThat(isFile.test("org/example/simple/TC_startEvent__endEvent.java"), is(true));
    assertThat(isFile.test("org/example/simpleasync/TC_startEvent__endEvent.java"), is(true));
    assertThat(isFile.test("org/example/simplecallactivity/TC_startEvent__endEvent.java"), is(true));
    assertThat(isFile.test("org/example/simplecollaboration/TC_startEvent__endEvent.java"), is(true));
    assertThat(isFile.test("org/example/simpleconditionalcatchevent/TC_startEvent__endEvent.java"), is(true));
    assertThat(isFile.test("org/example/simpleexternaltask/TC_startEvent__endEvent.java"), is(true));
    assertThat(isFile.test("org/example/simplemessagecatchevent/TC_startEvent__endEvent.java"), is(true));
    assertThat(isFile.test("org/example/simplereceivetask/TC_startEvent__endEvent.java"), is(true));
    assertThat(isFile.test("org/example/simplesignalcatchevent/TC_startEvent__endEvent.java"), is(true));
    assertThat(isFile.test("org/example/simplesubprocess/TC_startEvent__endEvent.java"), is(true));
    assertThat(isFile.test("org/example/simpletimercatchevent/TC_startEvent__endEvent.java"), is(true));
    assertThat(isFile.test("org/example/simpleusertask/TC_startEvent__endEvent.java"), is(true));
  }
}
