package org.camunda.community.bpmndt;

import static org.camunda.community.bpmndt.test.ContainsCode.containsCode;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import javax.lang.model.element.Modifier;

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
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class GeneratorMultiInstanceScopeTest {

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
    ctx.setMainResourcePath(Paths.get("./src/test/it/advanced-multi-instance/src/main/resources"));
    ctx.setTestSourcePath(temporaryFolder.getRoot().toPath());

    ctx.setPackageName("org.example");

    result = generator.getResult();

    String fileName = testName.getMethodName().replace("test", "scope") + ".bpmn";
    bpmnFile = ctx.getMainResourcePath().resolve(StringUtils.uncapitalize(fileName));
  }

  @Test
  public void testErrorEndEvent() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(4));

    TypeSpec typeSpec;

    TypeName multiInstanceScopeHandlerType = ClassName.get("org.example.scopeerrorendevent", "SubProcessHandler1");

    typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.fieldSpecs, hasSize(1));
    assertThat(typeSpec.fieldSpecs.get(0).name, equalTo("subProcess"));
    assertThat(typeSpec.fieldSpecs.get(0).type, equalTo(multiInstanceScopeHandlerType));
    assertThat(typeSpec.methodSpecs, hasSize(7));
    assertThat(typeSpec.methodSpecs.get(6).javadoc.isEmpty(), is(false));
    assertThat(typeSpec.methodSpecs.get(6).name, equalTo("handleSubProcess"));
    assertThat(typeSpec.methodSpecs.get(6).returnType, equalTo(multiInstanceScopeHandlerType));

    typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec.fieldSpecs, hasSize(0));
    assertThat(typeSpec.methodSpecs, hasSize(2));
    assertThat(typeSpec.name, equalTo("SubProcessHandler1"));

    assertThat(typeSpec.methodSpecs.get(1).annotations, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(1).annotations.get(0).type, equalTo(TypeName.get(Override.class)));
    assertThat(typeSpec.methodSpecs.get(1).modifiers, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(1).modifiers, hasItem(Modifier.PROTECTED));
    assertThat(typeSpec.methodSpecs.get(1).name, equalTo("apply"));
    assertThat(typeSpec.methodSpecs.get(1).parameters, hasSize(2));
    assertThat(typeSpec.methodSpecs.get(1).parameters.get(0).name, equalTo("pi"));
    assertThat(typeSpec.methodSpecs.get(1).parameters.get(1).name, equalTo("loopIndex"));
    containsCode(typeSpec.methodSpecs.get(1)).contains("return false");

    TypeName multiInstanceScopeHandlerType1 = ClassName.get("org.example.scopeerrorendevent", "SubProcessHandler2");

    typeSpec = result.getFiles().get(2).typeSpec;
    assertThat(typeSpec.fieldSpecs, hasSize(1));
    assertThat(typeSpec.fieldSpecs.get(0).name, equalTo("subProcess"));
    assertThat(typeSpec.fieldSpecs.get(0).type, equalTo(multiInstanceScopeHandlerType1));
    assertThat(typeSpec.methodSpecs, hasSize(7));
    assertThat(typeSpec.methodSpecs.get(6).javadoc.isEmpty(), is(false));
    assertThat(typeSpec.methodSpecs.get(6).name, equalTo("handleSubProcess"));
    assertThat(typeSpec.methodSpecs.get(6).returnType, equalTo(multiInstanceScopeHandlerType1));

    typeSpec = result.getFiles().get(3).typeSpec;
    assertThat(typeSpec.fieldSpecs, hasSize(0));
    assertThat(typeSpec.methodSpecs, hasSize(2));
    assertThat(typeSpec.name, equalTo("SubProcessHandler2"));
  }

  @Test
  public void testNested() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(3));
  }

  @Test
  public void testInner() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(6));

    TypeSpec typeSpec;

    TypeName multiInstanceScopeHandlerType = ClassName.get("org.example.scopeinner", "SubProcessHandler1");

    typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.fieldSpecs, hasSize(1));
    assertThat(typeSpec.methodSpecs, hasSize(8));

    assertThat(typeSpec.methodSpecs.get(3).modifiers, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(3).modifiers, hasItem(Modifier.PUBLIC));
    assertThat(typeSpec.methodSpecs.get(3).name, equalTo("getEnd"));
    containsCode(typeSpec.methodSpecs.get(3)).contains("return \"subProcess#multiInstanceBody\"");

    assertThat(typeSpec.methodSpecs.get(5).modifiers, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(5).modifiers, hasItem(Modifier.PUBLIC));
    assertThat(typeSpec.methodSpecs.get(5).name, equalTo("getStart"));
    containsCode(typeSpec.methodSpecs.get(5)).contains("return \"subProcess#multiInstanceBody\"");

    assertThat(typeSpec.methodSpecs.get(6).modifiers, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(6).modifiers, hasItem(Modifier.PROTECTED));
    assertThat(typeSpec.methodSpecs.get(6).name, equalTo("isProcessEnd"));
    containsCode(typeSpec.methodSpecs.get(6)).contains("return false");

    assertThat(typeSpec.methodSpecs.get(7).javadoc.isEmpty(), is(false));
    assertThat(typeSpec.methodSpecs.get(7).name, equalTo("handleSubProcess"));
    assertThat(typeSpec.methodSpecs.get(7).returnType, equalTo(multiInstanceScopeHandlerType));

    typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec.fieldSpecs, hasSize(0));
    assertThat(typeSpec.methodSpecs, hasSize(2));
    assertThat(typeSpec.name, equalTo("SubProcessHandler1"));

    typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec.fieldSpecs, hasSize(0));
    assertThat(typeSpec.methodSpecs, hasSize(2));
    assertThat(typeSpec.name, equalTo("SubProcessHandler1"));

    typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec.fieldSpecs, hasSize(0));
    assertThat(typeSpec.methodSpecs, hasSize(2));
    assertThat(typeSpec.name, equalTo("SubProcessHandler1"));
  }

  @Test
  public void testSequential() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles(), hasSize(2));

    TypeSpec typeSpec;

    TypeName multiInstanceScopeHandlerType = ClassName.get("org.example.scopesequential", "MultiInstanceScopeHandler1");

    typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec.fieldSpecs, hasSize(1));
    assertThat(typeSpec.fieldSpecs.get(0).name, equalTo("multiInstanceScope"));
    assertThat(typeSpec.fieldSpecs.get(0).type, equalTo(multiInstanceScopeHandlerType));
    assertThat(typeSpec.methodSpecs, hasSize(7));
    assertThat(typeSpec.methodSpecs.get(6).javadoc.isEmpty(), is(false));
    assertThat(typeSpec.methodSpecs.get(6).name, equalTo("handleMultiInstanceScope"));
    assertThat(typeSpec.methodSpecs.get(6).returnType, equalTo(multiInstanceScopeHandlerType));

    typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec.fieldSpecs, hasSize(4));
    assertThat(typeSpec.methodSpecs, hasSize(18));
    assertThat(typeSpec.name, equalTo("MultiInstanceScopeHandler1"));

    ParameterizedTypeName parameterizedTypeName;

    assertThat(typeSpec.fieldSpecs.get(0).name, equalTo("userTaskHandlers"));
    parameterizedTypeName = (ParameterizedTypeName) typeSpec.fieldSpecs.get(0).type;
    assertThat(parameterizedTypeName.rawType, equalTo(TypeName.get(Map.class)));
    assertThat(parameterizedTypeName.typeArguments.get(0), equalTo(TypeName.get(Integer.class)));
    assertThat(parameterizedTypeName.typeArguments.get(1), equalTo(TypeName.get(UserTaskHandler.class)));

    assertThat(typeSpec.fieldSpecs.get(1).name, equalTo("messageCatchEventHandlers"));
    parameterizedTypeName = (ParameterizedTypeName) typeSpec.fieldSpecs.get(1).type;
    assertThat(parameterizedTypeName.rawType, equalTo(TypeName.get(Map.class)));
    assertThat(parameterizedTypeName.typeArguments.get(0), equalTo(TypeName.get(Integer.class)));
    assertThat(parameterizedTypeName.typeArguments.get(1), equalTo(TypeName.get(EventHandler.class)));

    assertThat(typeSpec.fieldSpecs.get(2).name, equalTo("callActivityHandlersBefore"));
    parameterizedTypeName = (ParameterizedTypeName) typeSpec.fieldSpecs.get(2).type;
    assertThat(parameterizedTypeName.rawType, equalTo(TypeName.get(Map.class)));
    assertThat(parameterizedTypeName.typeArguments.get(0), equalTo(TypeName.get(Integer.class)));
    assertThat(parameterizedTypeName.typeArguments.get(1), equalTo(TypeName.get(JobHandler.class)));

    assertThat(typeSpec.fieldSpecs.get(3).name, equalTo("callActivityHandlers"));
    parameterizedTypeName = (ParameterizedTypeName) typeSpec.fieldSpecs.get(3).type;
    assertThat(parameterizedTypeName.rawType, equalTo(TypeName.get(Map.class)));
    assertThat(parameterizedTypeName.typeArguments.get(0), equalTo(TypeName.get(Integer.class)));
    assertThat(parameterizedTypeName.typeArguments.get(1), equalTo(TypeName.get(CallActivityHandler.class)));

    assertThat(typeSpec.methodSpecs.get(0).isConstructor(), is(true));

    assertThat(typeSpec.methodSpecs.get(1).annotations, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(1).annotations.get(0).type, equalTo(TypeName.get(Override.class)));
    assertThat(typeSpec.methodSpecs.get(1).modifiers, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(1).modifiers, hasItem(Modifier.PROTECTED));
    assertThat(typeSpec.methodSpecs.get(1).name, equalTo("apply"));
    assertThat(typeSpec.methodSpecs.get(1).parameters, hasSize(2));
    assertThat(typeSpec.methodSpecs.get(1).parameters.get(0).name, equalTo("pi"));
    assertThat(typeSpec.methodSpecs.get(1).parameters.get(1).name, equalTo("loopIndex"));
    containsCode(typeSpec.methodSpecs.get(1)).contains("registerCallActivityHandler(\"callActivity\", getCallActivityHandler(loopIndex))");
    containsCode(typeSpec.methodSpecs.get(1)).contains("return true");

    assertThat(typeSpec.methodSpecs.get(2).modifiers, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(2).modifiers, hasItem(Modifier.PROTECTED));
    assertThat(typeSpec.methodSpecs.get(2).name, equalTo("createUserTaskHandler"));
    assertThat(typeSpec.methodSpecs.get(2).parameters, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(2).parameters.get(0).name, equalTo("loopIndex"));
    containsCode(typeSpec.methodSpecs.get(2)).contains("return new org.camunda.community.bpmndt.api.UserTaskHandler(getProcessEngine(), \"userTask\")");

    assertThat(typeSpec.methodSpecs.get(4).modifiers, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(4).modifiers, hasItem(Modifier.PROTECTED));
    assertThat(typeSpec.methodSpecs.get(4).name, equalTo("createCallActivityHandlerBefore"));
    assertThat(typeSpec.methodSpecs.get(4).parameters, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(4).parameters.get(0).name, equalTo("loopIndex"));
    containsCode(typeSpec.methodSpecs.get(4)).contains("return new org.camunda.community.bpmndt.api.JobHandler(getProcessEngine(), \"callActivity\")");

    assertThat(typeSpec.methodSpecs.get(6).modifiers, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(6).modifiers, hasItem(Modifier.PROTECTED));
    assertThat(typeSpec.methodSpecs.get(6).name, equalTo("getUserTaskHandler"));
    assertThat(typeSpec.methodSpecs.get(6).parameters, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(6).parameters.get(0).name, equalTo("loopIndex"));
    containsCode(typeSpec.methodSpecs.get(6)).contains("return userTaskHandlers.getOrDefault(loopIndex, handleUserTask())");

    assertThat(typeSpec.methodSpecs.get(8).modifiers, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(8).modifiers, hasItem(Modifier.PROTECTED));
    assertThat(typeSpec.methodSpecs.get(8).name, equalTo("getCallActivityHandlerBefore"));
    assertThat(typeSpec.methodSpecs.get(8).parameters, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(8).parameters.get(0).name, equalTo("loopIndex"));
    containsCode(typeSpec.methodSpecs.get(8)).contains("return callActivityHandlersBefore.getOrDefault(loopIndex, handleCallActivityBefore())");

    assertThat(typeSpec.methodSpecs.get(10).javadoc.isEmpty(), is(false));
    assertThat(typeSpec.methodSpecs.get(10).modifiers, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(10).modifiers, hasItem(Modifier.PUBLIC));
    assertThat(typeSpec.methodSpecs.get(10).name, equalTo("handleUserTask"));
    assertThat(typeSpec.methodSpecs.get(10).parameters, hasSize(0));
    containsCode(typeSpec.methodSpecs.get(10)).contains("return handleUserTask(-1)");

    assertThat(typeSpec.methodSpecs.get(11).javadoc.isEmpty(), is(false));
    assertThat(typeSpec.methodSpecs.get(11).modifiers, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(11).modifiers, hasItem(Modifier.PUBLIC));
    assertThat(typeSpec.methodSpecs.get(11).name, equalTo("handleUserTask"));
    assertThat(typeSpec.methodSpecs.get(11).parameters, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(11).parameters.get(0).name, equalTo("loopIndex"));
    containsCode(typeSpec.methodSpecs.get(11)).contains("return userTaskHandlers.computeIfAbsent(loopIndex, this::createUserTaskHandler)");

    assertThat(typeSpec.methodSpecs.get(14).javadoc.isEmpty(), is(false));
    assertThat(typeSpec.methodSpecs.get(14).modifiers, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(14).modifiers, hasItem(Modifier.PUBLIC));
    assertThat(typeSpec.methodSpecs.get(14).name, equalTo("handleCallActivityBefore"));
    assertThat(typeSpec.methodSpecs.get(14).parameters, hasSize(0));
    containsCode(typeSpec.methodSpecs.get(14)).contains("return handleCallActivityBefore(-1)");

    assertThat(typeSpec.methodSpecs.get(15).javadoc.isEmpty(), is(false));
    assertThat(typeSpec.methodSpecs.get(15).modifiers, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(15).modifiers, hasItem(Modifier.PUBLIC));
    assertThat(typeSpec.methodSpecs.get(15).name, equalTo("handleCallActivityBefore"));
    assertThat(typeSpec.methodSpecs.get(15).parameters, hasSize(1));
    assertThat(typeSpec.methodSpecs.get(15).parameters.get(0).name, equalTo("loopIndex"));
    containsCode(typeSpec.methodSpecs.get(15)).contains("return callActivityHandlersBefore.computeIfAbsent(loopIndex, this::createCallActivityHandlerBefore)");
  }
}
