package org.camunda.bpm.extension.bpmndt;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.Modifier;

import org.apache.maven.plugin.logging.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.mockito.Mockito;
import org.springframework.context.annotation.Configuration;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class GeneratorTaskTest {

  @Rule
  public TestName testName = new TestName();
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder(new File("./target"));

  private GeneratorTask task;
  
  private List<JavaFile> javaFiles;
  private Path bpmnFile;

  @Before
  public void setUp() {
    task = new GeneratorTask(Mockito.mock(Log.class));
    task.basePath = Paths.get(".");
    task.mainResourcePath = task.basePath.resolve("src/test/resources/bpmn");
    task.testSourcePath = temporaryFolder.getRoot().toPath();

    task.packageName = "org.example";

    javaFiles = new LinkedList<>();

    String fileName = testName.getMethodName().replace("test", "") + ".bpmn";
    bpmnFile = task.mainResourcePath.resolve(Character.toLowerCase(fileName.charAt(0)) + fileName.substring(1));
  }

  @Test
  public void testGenerateFramework() {
    List<JavaFile> framework = task.generateFramework();
    assertThat(framework, hasSize(4));

    TypeSpec typeSpec;

    // AbstractTestCase
    typeSpec = framework.get(0).typeSpec;
    assertThat(typeSpec.name, equalTo(GeneratorConstants.TYPE_ABSTRACT_TEST_CASE));
    assertThat(typeSpec.modifiers, hasSize(1));
    assertThat(typeSpec.modifiers, hasItem(Modifier.ABSTRACT));
    assertThat(typeSpec.fieldSpecs, hasSize(3));
    assertThat(typeSpec.fieldSpecs.get(0).name, equalTo(GeneratorConstants.PROCESS_ENGINE_RULE));
    assertThat(typeSpec.fieldSpecs.get(0).type, equalTo(ClassName.get(ProcessEngineRule.class)));
    assertThat(typeSpec.fieldSpecs.get(1).name, equalTo(GeneratorConstants.CALL_ACTIVITY_RULE));
    TypeName callActivityRuleType = ClassName.get(task.packageName, framework.get(2).typeSpec.name);
    assertThat(typeSpec.fieldSpecs.get(1).type, equalTo(callActivityRuleType));
    assertThat(typeSpec.fieldSpecs.get(2).name, equalTo(GeneratorConstants.PROCESS_INSTANCE));
    assertThat(typeSpec.fieldSpecs.get(2).type, equalTo(ClassName.get(ProcessInstance.class)));

    assertThat(typeSpec.methodSpecs, hasSize(4));
    assertThat(typeSpec.methodSpecs.get(0).name, equalTo(GeneratorConstants.BUILD_PROCESS_ENGINE));
    assertThat(typeSpec.methodSpecs.get(1).name, equalTo(GeneratorConstants.BUILD_PROCESS_ENGINE_CONFIGURATION));
    assertThat(typeSpec.methodSpecs.get(2).name, equalTo(GeneratorConstants.ASSERT_THAT_PI));
    assertThat(typeSpec.methodSpecs.get(3).name, equalTo(GeneratorConstants.FIND_EVENT_SUBSCRIPTION));
    assertThat(typeSpec.methodSpecs.get(3).modifiers, hasSize(0));

    // CallActivityParseListener
    typeSpec = framework.get(1).typeSpec;
    assertThat(typeSpec.name, equalTo(GeneratorConstants.TYPE_CALL_ACTIVITY_PARSE_LISTENER));
    assertThat(typeSpec.modifiers, hasSize(0));

    // CallActivityRule
    typeSpec = framework.get(2).typeSpec;
    assertThat(typeSpec.name, equalTo(GeneratorConstants.TYPE_CALL_ACTIVITY_RULE));
    assertThat(typeSpec.modifiers, hasSize(1));
    assertThat(typeSpec.modifiers, hasItem(Modifier.PUBLIC));
    assertThat(typeSpec.fieldSpecs.size(), greaterThan(0));
    assertThat(typeSpec.methodSpecs.size(), greaterThan(0));

    // BpmndtPlugin
    typeSpec = framework.get(3).typeSpec;
    assertThat(typeSpec.name, equalTo(GeneratorConstants.TYPE_BPMNDT_PLUGIN));
    assertThat(typeSpec.modifiers, hasSize(1));
    assertThat(typeSpec.modifiers, hasItem(Modifier.PUBLIC));
    assertThat(typeSpec.fieldSpecs.isEmpty(), is(true));
    assertThat(typeSpec.methodSpecs.size(), is(1));
  }

  @Test
  public void testGenerateFrameworkSpringEnabled() {
    task.springEnabled = true;

    List<JavaFile> framework = task.generateFramework();
    assertThat(framework, hasSize(5));

    // BpmndtConfiguration
    TypeSpec typeSpec = framework.get(4).typeSpec;
    assertThat(typeSpec.name, equalTo(GeneratorConstants.TYPE_BPMNDT_CONFIGURATION));
    assertThat(typeSpec.annotations.size(), is(1));
    assertThat(typeSpec.annotations.get(0).type, equalTo(ClassName.get(Configuration.class)));
    assertThat(typeSpec.modifiers, hasSize(1));
    assertThat(typeSpec.modifiers, hasItem(Modifier.PUBLIC));
    assertThat(typeSpec.fieldSpecs.size(), is(4));
    assertThat(typeSpec.methodSpecs.size(), is(5));
  }

  /**
   * Should generate the first test case and skip the second. Since the second test case has the same
   * name as the first, which is not allowed.
   */
  @Test
  public void testDuplicateTestCaseNames() {
    task.generate(javaFiles, bpmnFile);
    assertThat(javaFiles, hasSize(1));

    TypeSpec typeSpec = javaFiles.get(0).typeSpec;
    assertThat(typeSpec.name, equalTo("TC_duplicateTestCaseNames__startEvent__endEvent"));
  }

  @Test
  public void testHappyPath() {
    task.generate(javaFiles, bpmnFile);
    assertThat(javaFiles, hasSize(1));

    TypeSpec typeSpec = javaFiles.get(0).typeSpec;
    assertThat(typeSpec.name, equalTo("TC_happy_path__Happy_Path"));
  }

  @Test
  public void testNoTestCases() {
    task.generate(javaFiles, bpmnFile);
    assertThat(javaFiles, hasSize(0));
  }

  @Test
  public void testSimple() {
    task.generate(javaFiles, bpmnFile);
    assertThat(javaFiles, hasSize(1));

    JavaFile javaFile = javaFiles.get(0);
    assertThat(javaFile.packageName, equalTo(task.packageName));
    assertThat(javaFile.skipJavaLangImports, is(true));
    assertThat(javaFile.typeSpec, notNullValue());

    TypeSpec typeSpec = javaFile.typeSpec;
    assertThat(typeSpec.name, equalTo("TC_simple__startEvent__endEvent"));

    TypeName superclass = ClassName.get(task.packageName, GeneratorConstants.TYPE_ABSTRACT_TEST_CASE);
    assertThat(typeSpec.superclass, equalTo(superclass));
    assertThat(typeSpec.methodSpecs, hasSize(3));
    assertThat(typeSpec.methodSpecs.get(0).name, equalTo(GeneratorConstants.BEFORE));
    assertThat(typeSpec.methodSpecs.get(0).parameters, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(0).parameters.get(0).name, equalTo(GeneratorConstants.VARIABLES));
    assertThat(typeSpec.methodSpecs.get(0).parameters.get(0).type, equalTo(ClassName.get(VariableMap.class)));
    assertThat(typeSpec.methodSpecs.get(1).name, equalTo(GeneratorConstants.TEST_PATH));
    assertThat(typeSpec.methodSpecs.get(1).annotations, hasSize(2));
    assertThat(typeSpec.methodSpecs.get(1).annotations.get(0).type, equalTo(ClassName.get(Test.class)));
    assertThat(typeSpec.methodSpecs.get(1).annotations.get(1).type, equalTo(ClassName.get(Deployment.class)));
    assertThat(typeSpec.methodSpecs.get(2).name, equalTo(GeneratorConstants.AFTER));

    // check if @Deployment annotation has member resources with value "simple.bpmn"
    List<CodeBlock> deploymentMembers = typeSpec.methodSpecs.get(1).annotations.get(1).members.get("resources");
    assertThat(deploymentMembers, hasSize(1));
    assertThat(deploymentMembers.get(0).toString(), containsString(bpmnFile.getFileName().toString()));
  }

  /**
   * Should be the same as {@link #testSimple()}, when Spring is enabled.
   */
  @Test
  public void testSimpleSpringEnabled() {
    task.springEnabled = true;

    // overwrite auto built BPMN file path
    bpmnFile = task.mainResourcePath.resolve("simple.bpmn");

    task.generate(javaFiles, bpmnFile);
    assertThat(javaFiles, hasSize(1));

    JavaFile javaFile = javaFiles.get(0);
    assertThat(javaFile.packageName, equalTo(task.packageName));
    assertThat(javaFile.skipJavaLangImports, is(true));
    assertThat(javaFile.typeSpec, notNullValue());

    TypeSpec typeSpec = javaFile.typeSpec;
    assertThat(typeSpec.name, equalTo("TC_simple__startEvent__endEvent"));

    TypeName superclass = ClassName.get(task.packageName, GeneratorConstants.TYPE_ABSTRACT_TEST_CASE);
    assertThat(typeSpec.superclass, equalTo(superclass));
    assertThat(typeSpec.methodSpecs, hasSize(3));
    assertThat(typeSpec.methodSpecs.get(0).name, equalTo(GeneratorConstants.BEFORE));
    assertThat(typeSpec.methodSpecs.get(0).parameters, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(0).parameters.get(0).name, equalTo(GeneratorConstants.VARIABLES));
    assertThat(typeSpec.methodSpecs.get(0).parameters.get(0).type, equalTo(ClassName.get(VariableMap.class)));
    assertThat(typeSpec.methodSpecs.get(1).name, equalTo(GeneratorConstants.TEST_PATH));
    assertThat(typeSpec.methodSpecs.get(1).annotations, hasSize(2));
    assertThat(typeSpec.methodSpecs.get(1).annotations.get(0).type, equalTo(ClassName.get(Test.class)));
    assertThat(typeSpec.methodSpecs.get(1).annotations.get(1).type, equalTo(ClassName.get(Deployment.class)));
    assertThat(typeSpec.methodSpecs.get(2).name, equalTo(GeneratorConstants.AFTER));

    // check if @Deployment annotation has member resources with value "simple.bpmn"
    List<CodeBlock> deploymentMembers = typeSpec.methodSpecs.get(1).annotations.get(1).members.get("resources");
    assertThat(deploymentMembers, hasSize(1));
    assertThat(deploymentMembers.get(0).toString(), containsString(bpmnFile.getFileName().toString()));
  }

  @Test
  public void testSimpleAsync() {
    task.generate(javaFiles, bpmnFile);
    assertThat(javaFiles, hasSize(1));

    TypeSpec typeSpec = javaFiles.get(0).typeSpec;
    assertThat(typeSpec.methodSpecs, hasSize(5));
    assertThat(typeSpec.methodSpecs.get(2).name, equalTo("startEvent_after"));
    assertThat(typeSpec.methodSpecs.get(2).parameters, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(2).parameters.get(0).name, equalTo(GeneratorConstants.JOB));
    assertThat(typeSpec.methodSpecs.get(2).parameters.get(0).type, equalTo(ClassName.get(Job.class)));
    assertThat(typeSpec.methodSpecs.get(3).name, equalTo("endEvent_before"));
    assertThat(typeSpec.methodSpecs.get(3).parameters, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(3).parameters.get(0).name, equalTo(GeneratorConstants.JOB));
    assertThat(typeSpec.methodSpecs.get(3).parameters.get(0).type, equalTo(ClassName.get(Job.class)));

    String testPathCode = typeSpec.methodSpecs.get(1).code.toString();
    assertThat(testPathCode, containsString("startEvent_after(job(\"startEvent\", pi));"));
    assertThat(testPathCode, containsString("endEvent_before(job(\"endEvent\", pi));"));
  }

  @Test
  public void testSimpleCallActivity() {
    task.generate(javaFiles, bpmnFile);
    assertThat(javaFiles, hasSize(1));

    TypeSpec typeSpec = javaFiles.get(0).typeSpec;
    assertThat(typeSpec.methodSpecs, hasSize(5));
    assertThat(typeSpec.methodSpecs.get(2).name, equalTo("callActivity_input"));
    assertThat(typeSpec.methodSpecs.get(2).parameters, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(2).parameters.get(0).name, equalTo("subInstance"));
    assertThat(typeSpec.methodSpecs.get(2).parameters.get(0).type, equalTo(ClassName.get(VariableScope.class)));
    assertThat(typeSpec.methodSpecs.get(3).name, equalTo("callActivity_output"));
    assertThat(typeSpec.methodSpecs.get(3).parameters, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(3).parameters.get(0).name, equalTo("execution"));
    assertThat(typeSpec.methodSpecs.get(3).parameters.get(0).type, equalTo(ClassName.get(DelegateExecution.class)));

    String testPathCode = typeSpec.methodSpecs.get(1).code.toString();
    assertThat(testPathCode, containsString("callActivityRule.callbackI.put(\"callActivity\", this::callActivity_input);"));
    assertThat(testPathCode, containsString("callActivityRule.callbackO.put(\"callActivity\", this::callActivity_output);"));
  }

  @Test
  public void testSimpleExternalTask() {
    task.generate(javaFiles, bpmnFile);
    assertThat(javaFiles, hasSize(1));

    TypeSpec typeSpec = javaFiles.get(0).typeSpec;
    assertThat(typeSpec.methodSpecs, hasSize(4));
    assertThat(typeSpec.methodSpecs.get(2).name, equalTo("externalTask"));
    assertThat(typeSpec.methodSpecs.get(2).parameters, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(2).parameters.get(0).name, equalTo(GeneratorConstants.TOPIC_NAME));
    assertThat(typeSpec.methodSpecs.get(2).parameters.get(0).type, equalTo(ClassName.get(String.class)));

    String testPathCode = typeSpec.methodSpecs.get(1).code.toString();
    assertThat(testPathCode, containsString("externalTask(\"test-topic\");"));
  }

  @Test
  public void testSimpleMessageCatchEvent() {
    task.generate(javaFiles, bpmnFile);
    assertThat(javaFiles, hasSize(1));

    TypeSpec typeSpec = javaFiles.get(0).typeSpec;
    assertThat(typeSpec.methodSpecs, hasSize(4));
    assertThat(typeSpec.methodSpecs.get(2).name, equalTo("messageCatchEvent"));
    assertThat(typeSpec.methodSpecs.get(2).parameters, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(2).parameters.get(0).name, equalTo(GeneratorConstants.EVENT_SUBSCRIPTION));
    assertThat(typeSpec.methodSpecs.get(2).parameters.get(0).type, equalTo(ClassName.get(EventSubscription.class)));

    String testPathCode = typeSpec.methodSpecs.get(1).code.toString();
    assertThat(testPathCode, containsString("findEventSubscription(\"messageCatchEvent\", \"simpleMessage\")"));
  }

  @Test
  public void testSimpleSignalCatchEvent() {
    task.generate(javaFiles, bpmnFile);
    assertThat(javaFiles, hasSize(1));

    TypeSpec typeSpec = javaFiles.get(0).typeSpec;
    assertThat(typeSpec.methodSpecs, hasSize(4));
    assertThat(typeSpec.methodSpecs.get(2).name, equalTo("signalCatchEvent"));
    assertThat(typeSpec.methodSpecs.get(2).parameters, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(2).parameters.get(0).name, equalTo(GeneratorConstants.EVENT_SUBSCRIPTION));
    assertThat(typeSpec.methodSpecs.get(2).parameters.get(0).type, equalTo(ClassName.get(EventSubscription.class)));

    String testPathCode = typeSpec.methodSpecs.get(1).code.toString();
    assertThat(testPathCode, containsString("findEventSubscription(\"signalCatchEvent\", \"simpleSignal\")"));
  }

  @Test
  public void testSimpleSubProcess() {
    task.generate(javaFiles, bpmnFile);
    assertThat(javaFiles, hasSize(1));

    TypeSpec typeSpec = javaFiles.get(0).typeSpec;
    assertThat(typeSpec.methodSpecs, hasSize(3));

    String testPathCode = typeSpec.methodSpecs.get(1).code.toString();
    assertThat(testPathCode, containsString("// startEvent: subProcessStartEvent"));
    assertThat(testPathCode, containsString("assertThat(pi).hasPassed(\"subProcessStartEvent\");"));
    assertThat(testPathCode, containsString("// endEvent: subProcessEndEvent"));
    assertThat(testPathCode, containsString("assertThat(pi).hasPassed(\"subProcessEndEvent\");"));
  }

  @Test
  public void testSimpleTimerCatchEvent() {
    task.generate(javaFiles, bpmnFile);
    assertThat(javaFiles, hasSize(1));

    TypeSpec typeSpec = javaFiles.get(0).typeSpec;
    assertThat(typeSpec.methodSpecs, hasSize(4));
    assertThat(typeSpec.methodSpecs.get(2).name, equalTo("timerCatchEvent"));
    assertThat(typeSpec.methodSpecs.get(2).parameters, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(2).parameters.get(0).name, equalTo(GeneratorConstants.JOB));
    assertThat(typeSpec.methodSpecs.get(2).parameters.get(0).type, equalTo(ClassName.get(Job.class)));

    String testPathCode = typeSpec.methodSpecs.get(1).code.toString();
    assertThat(testPathCode, containsString("timerCatchEvent(job(\"timerCatchEvent\", pi));"));
  }

  @Test
  public void testSimpleUserTask() {
    task.generate(javaFiles, bpmnFile);
    assertThat(javaFiles, hasSize(1));

    TypeSpec typeSpec = javaFiles.get(0).typeSpec;
    assertThat(typeSpec.methodSpecs, hasSize(4));
    assertThat(typeSpec.methodSpecs.get(2).name, equalTo("userTask"));
    assertThat(typeSpec.methodSpecs.get(2).parameters, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(2).parameters.get(0).name, equalTo(GeneratorConstants.TASK));
    assertThat(typeSpec.methodSpecs.get(2).parameters.get(0).type, equalTo(ClassName.get(Task.class)));

    String testPathCode = typeSpec.methodSpecs.get(1).code.toString();
    assertThat(testPathCode, containsString("userTask(task(\"userTask\", pi));"));
  }

  /**
   * Tests the complete task execution.
   */
  @Test
  public void testExecute() {
    task.execute();

    // framework classess
    assertThat(Files.isRegularFile(task.testSourcePath.resolve("org/example/AbstractTestCase.java")), is(true));
    assertThat(Files.isRegularFile(task.testSourcePath.resolve("org/example/CallActivityParseListener.java")), is(true));
    assertThat(Files.isRegularFile(task.testSourcePath.resolve("org/example/CallActivityRule.java")), is(true));
    
    // test cases
    assertThat(Files.isRegularFile(task.testSourcePath.resolve("org/example/TC_simple__startEvent__endEvent.java")), is(true));
    assertThat(Files.isRegularFile(task.testSourcePath.resolve("org/example/TC_simpleAsync__startEvent__endEvent.java")), is(true));
    assertThat(Files.isRegularFile(task.testSourcePath.resolve("org/example/TC_simpleCallActivity__startEvent__endEvent.java")), is(true));
    assertThat(Files.isRegularFile(task.testSourcePath.resolve("org/example/TC_simpleExternalTask__startEvent__endEvent.java")), is(true));
    assertThat(Files.isRegularFile(task.testSourcePath.resolve("org/example/TC_simpleMessageCatchEvent__startEvent__endEvent.java")), is(true));
    assertThat(Files.isRegularFile(task.testSourcePath.resolve("org/example/TC_simpleSignalCatchEvent__startEvent__endEvent.java")), is(true));
    assertThat(Files.isRegularFile(task.testSourcePath.resolve("org/example/TC_simpleSubProcess__startEvent__endEvent.java")), is(true));
    assertThat(Files.isRegularFile(task.testSourcePath.resolve("org/example/TC_simpleUserTask__startEvent__endEvent.java")), is(true));

    // no test cases
    assertThat(Files.isRegularFile(task.testSourcePath.resolve("org/example/TC_noTestCases__startEvent__endEvent.java")), is(false));
  }
}
