package org.camunda.community.bpmndt;

import static com.google.common.truth.Truth.assertThat;
import static org.camunda.community.bpmndt.test.FieldSpecSubject.assertThat;
import static org.camunda.community.bpmndt.test.MethodSpecSubject.assertThat;
import static org.camunda.community.bpmndt.test.TypeSpecSubject.assertThat;

import java.nio.file.Path;
import java.util.Map;

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
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class GeneratorMultiInstanceScopeTest {

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

    String fileName = testInfo.getTestMethod().get().getName().replace("test", "scope") + ".bpmn";
    bpmnFile = ctx.getMainResourcePath().resolve(StringUtils.uncapitalize(fileName));
  }

  @Test
  public void testErrorEndEvent() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(4);

    TypeSpec typeSpec;

    TypeName multiInstanceScopeHandlerType = ClassName.get("org.example.scopeerrorendevent", "SubProcessHandler1");

    typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(7);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("subProcess");
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(multiInstanceScopeHandlerType);

    assertThat(typeSpec.methodSpecs.get(6)).hasJavaDoc();
    assertThat(typeSpec.methodSpecs.get(6)).hasName("handleSubProcess");
    assertThat(typeSpec.methodSpecs.get(6)).hasReturnType(multiInstanceScopeHandlerType);

    typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec).hasFields(0);
    assertThat(typeSpec).hasMethods(2);
    assertThat(typeSpec).hasName("SubProcessHandler1");

    assertThat(typeSpec.methodSpecs.get(1)).hasAnnotation(Override.class);
    assertThat(typeSpec.methodSpecs.get(1)).hasName("apply");
    assertThat(typeSpec.methodSpecs.get(1)).hasParameters("pi", "loopIndex");
    assertThat(typeSpec.methodSpecs.get(1)).isProtected();
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("return false");

    TypeName multiInstanceScopeHandlerType1 = ClassName.get("org.example.scopeerrorendevent", "SubProcessHandler2");

    typeSpec = result.getFiles().get(2).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(7);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("subProcess");
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(multiInstanceScopeHandlerType1);

    assertThat(typeSpec.methodSpecs.get(6)).hasJavaDoc();
    assertThat(typeSpec.methodSpecs.get(6)).hasName("handleSubProcess");
    assertThat(typeSpec.methodSpecs.get(6)).hasReturnType(multiInstanceScopeHandlerType1);

    typeSpec = result.getFiles().get(3).typeSpec;
    assertThat(typeSpec).hasFields(0);
    assertThat(typeSpec).hasMethods(2);
    assertThat(typeSpec).hasName("SubProcessHandler2");
  }

  @Test
  public void testNested() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(3);
  }

  @Test
  public void testInner() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(6);

    TypeSpec typeSpec;

    TypeName multiInstanceScopeHandlerType = ClassName.get("org.example.scopeinner", "SubProcessHandler1");

    typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(8);

    assertThat(typeSpec.methodSpecs.get(3)).hasName("getEnd");
    assertThat(typeSpec.methodSpecs.get(3)).isPublic();
    assertThat(typeSpec.methodSpecs.get(3)).containsCode("return \"subProcess#multiInstanceBody\"");

    assertThat(typeSpec.methodSpecs.get(5)).hasName("getStart");
    assertThat(typeSpec.methodSpecs.get(5)).isPublic();
    assertThat(typeSpec.methodSpecs.get(5)).containsCode("return \"subProcess#multiInstanceBody\"");

    assertThat(typeSpec.methodSpecs.get(6)).hasName("isProcessEnd");
    assertThat(typeSpec.methodSpecs.get(6)).isProtected();
    assertThat(typeSpec.methodSpecs.get(6)).containsCode("return false");

    assertThat(typeSpec.methodSpecs.get(7)).hasJavaDoc();
    assertThat(typeSpec.methodSpecs.get(7)).hasName("handleSubProcess");
    assertThat(typeSpec.methodSpecs.get(7)).hasReturnType(multiInstanceScopeHandlerType);

    typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec).hasFields(0);
    assertThat(typeSpec).hasMethods(2);
    assertThat(typeSpec).hasName("SubProcessHandler1");
  }

  @Test
  public void testSequential() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(2);

    TypeSpec typeSpec;

    TypeName multiInstanceScopeHandlerType = ClassName.get("org.example.scopesequential", "MultiInstanceScopeHandler1");

    typeSpec = result.getFiles().get(0).typeSpec;
    assertThat(typeSpec).hasFields(1);
    assertThat(typeSpec).hasMethods(7);

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("multiInstanceScope");
    assertThat(typeSpec.fieldSpecs.get(0)).hasType(multiInstanceScopeHandlerType);

    assertThat(typeSpec.methodSpecs.get(6)).hasJavaDoc();
    assertThat(typeSpec.methodSpecs.get(6)).hasName("handleMultiInstanceScope");
    assertThat(typeSpec.methodSpecs.get(6)).hasReturnType(multiInstanceScopeHandlerType);

    typeSpec = result.getFiles().get(1).typeSpec;
    assertThat(typeSpec).hasFields(6);
    assertThat(typeSpec).hasMethods(26);
    assertThat(typeSpec).hasName("MultiInstanceScopeHandler1");

    ParameterizedTypeName parameterizedTypeName;

    assertThat(typeSpec.fieldSpecs.get(0)).hasName("userTaskHandlers");
    parameterizedTypeName = (ParameterizedTypeName) typeSpec.fieldSpecs.get(0).type;
    assertThat(parameterizedTypeName.rawType).isEqualTo(TypeName.get(Map.class));
    assertThat(parameterizedTypeName.typeArguments.get(0)).isEqualTo(TypeName.get(Integer.class));
    assertThat(parameterizedTypeName.typeArguments.get(1)).isEqualTo(TypeName.get(UserTaskHandler.class));

    assertThat(typeSpec.fieldSpecs.get(1)).hasName("messageCatchEventHandlers");
    parameterizedTypeName = (ParameterizedTypeName) typeSpec.fieldSpecs.get(1).type;
    assertThat(parameterizedTypeName.rawType).isEqualTo(TypeName.get(Map.class));
    assertThat(parameterizedTypeName.typeArguments.get(0)).isEqualTo(TypeName.get(Integer.class));
    assertThat(parameterizedTypeName.typeArguments.get(1)).isEqualTo(TypeName.get(EventHandler.class));

    assertThat(typeSpec.fieldSpecs.get(2)).hasName("serviceTaskHandlersBefore");
    parameterizedTypeName = (ParameterizedTypeName) typeSpec.fieldSpecs.get(2).type;
    assertThat(parameterizedTypeName.rawType).isEqualTo(TypeName.get(Map.class));
    assertThat(parameterizedTypeName.typeArguments.get(0)).isEqualTo(TypeName.get(Integer.class));
    assertThat(parameterizedTypeName.typeArguments.get(1)).isEqualTo(TypeName.get(JobHandler.class));

    assertThat(typeSpec.fieldSpecs.get(3)).hasName("serviceTaskHandlersAfter");
    parameterizedTypeName = (ParameterizedTypeName) typeSpec.fieldSpecs.get(3).type;
    assertThat(parameterizedTypeName.rawType).isEqualTo(TypeName.get(Map.class));
    assertThat(parameterizedTypeName.typeArguments.get(0)).isEqualTo(TypeName.get(Integer.class));
    assertThat(parameterizedTypeName.typeArguments.get(1)).isEqualTo(TypeName.get(JobHandler.class));

    assertThat(typeSpec.fieldSpecs.get(4)).hasName("callActivityHandlersBefore");
    parameterizedTypeName = (ParameterizedTypeName) typeSpec.fieldSpecs.get(4).type;
    assertThat(parameterizedTypeName.rawType).isEqualTo(TypeName.get(Map.class));
    assertThat(parameterizedTypeName.typeArguments.get(0)).isEqualTo(TypeName.get(Integer.class));
    assertThat(parameterizedTypeName.typeArguments.get(1)).isEqualTo(TypeName.get(JobHandler.class));

    assertThat(typeSpec.fieldSpecs.get(5)).hasName("callActivityHandlers");
    parameterizedTypeName = (ParameterizedTypeName) typeSpec.fieldSpecs.get(5).type;
    assertThat(parameterizedTypeName.rawType).isEqualTo(TypeName.get(Map.class));
    assertThat(parameterizedTypeName.typeArguments.get(0)).isEqualTo(TypeName.get(Integer.class));
    assertThat(parameterizedTypeName.typeArguments.get(1)).isEqualTo(TypeName.get(CallActivityHandler.class));

    assertThat(typeSpec.methodSpecs.get(0).isConstructor()).isTrue();

    assertThat(typeSpec.methodSpecs.get(1)).hasAnnotation(Override.class);
    assertThat(typeSpec.methodSpecs.get(1)).hasName("apply");
    assertThat(typeSpec.methodSpecs.get(1)).hasParameters("pi", "loopIndex");
    assertThat(typeSpec.methodSpecs.get(1)).isProtected();
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("registerCallActivityHandler(\"callActivity\", getCallActivityHandler(loopIndex))");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("return true");

    assertThat(typeSpec.methodSpecs.get(2)).hasName("createUserTaskHandler");
    assertThat(typeSpec.methodSpecs.get(2)).hasParameters("loopIndex");
    assertThat(typeSpec.methodSpecs.get(2)).isProtected();
    assertThat(typeSpec.methodSpecs.get(2)).containsCode("return new org.camunda.community.bpmndt.api.UserTaskHandler(getProcessEngine(), \"userTask\")");

    assertThat(typeSpec.methodSpecs.get(4)).hasName("createServiceTaskHandlerBefore");
    assertThat(typeSpec.methodSpecs.get(5)).hasName("createServiceTaskHandlerAfter");

    assertThat(typeSpec.methodSpecs.get(6)).hasName("createCallActivityHandlerBefore");
    assertThat(typeSpec.methodSpecs.get(6)).hasParameters("loopIndex");
    assertThat(typeSpec.methodSpecs.get(6)).isProtected();
    assertThat(typeSpec.methodSpecs.get(6)).containsCode("return new org.camunda.community.bpmndt.api.JobHandler(getProcessEngine(), \"callActivity\")");

    assertThat(typeSpec.methodSpecs.get(8)).hasName("getUserTaskHandler");
    assertThat(typeSpec.methodSpecs.get(8)).hasParameters("loopIndex");
    assertThat(typeSpec.methodSpecs.get(8)).isProtected();
    assertThat(typeSpec.methodSpecs.get(8)).containsCode("return userTaskHandlers.getOrDefault(loopIndex, handleUserTask())");

    assertThat(typeSpec.methodSpecs.get(10)).hasName("getServiceTaskHandlerBefore");
    assertThat(typeSpec.methodSpecs.get(11)).hasName("getServiceTaskHandlerAfter");

    assertThat(typeSpec.methodSpecs.get(12)).hasName("getCallActivityHandlerBefore");
    assertThat(typeSpec.methodSpecs.get(12)).hasParameters("loopIndex");
    assertThat(typeSpec.methodSpecs.get(12)).isProtected();
    assertThat(typeSpec.methodSpecs.get(12)).containsCode("return callActivityHandlersBefore.getOrDefault(loopIndex, handleCallActivityBefore())");

    assertThat(typeSpec.methodSpecs.get(14)).hasJavaDoc();
    assertThat(typeSpec.methodSpecs.get(14)).hasName("handleUserTask");
    assertThat(typeSpec.methodSpecs.get(14)).isPublic();
    assertThat(typeSpec.methodSpecs.get(14).parameters).hasSize(0);
    assertThat(typeSpec.methodSpecs.get(14)).containsCode("return handleUserTask(-1)");

    assertThat(typeSpec.methodSpecs.get(15)).hasJavaDoc();
    assertThat(typeSpec.methodSpecs.get(15)).hasName("handleUserTask");
    assertThat(typeSpec.methodSpecs.get(15)).hasParameters("loopIndex");
    assertThat(typeSpec.methodSpecs.get(15)).isPublic();
    assertThat(typeSpec.methodSpecs.get(15)).containsCode("return userTaskHandlers.computeIfAbsent(loopIndex, this::createUserTaskHandler)");

    assertThat(typeSpec.methodSpecs.get(18)).hasName("handleServiceTaskBefore");
    assertThat(typeSpec.methodSpecs.get(19)).hasName("handleServiceTaskBefore");
    assertThat(typeSpec.methodSpecs.get(20)).hasName("handleServiceTaskAfter");
    assertThat(typeSpec.methodSpecs.get(21)).hasName("handleServiceTaskAfter");

    assertThat(typeSpec.methodSpecs.get(22)).hasJavaDoc();
    assertThat(typeSpec.methodSpecs.get(22)).hasName("handleCallActivityBefore");
    assertThat(typeSpec.methodSpecs.get(22)).isPublic();
    assertThat(typeSpec.methodSpecs.get(22).parameters).hasSize(0);
    assertThat(typeSpec.methodSpecs.get(22)).containsCode("return handleCallActivityBefore(-1)");

    assertThat(typeSpec.methodSpecs.get(23)).hasJavaDoc();
    assertThat(typeSpec.methodSpecs.get(23)).hasName("handleCallActivityBefore");
    assertThat(typeSpec.methodSpecs.get(23)).hasParameters("loopIndex");
    assertThat(typeSpec.methodSpecs.get(23)).isPublic();
    assertThat(typeSpec.methodSpecs.get(23))
        .containsCode("return callActivityHandlersBefore.computeIfAbsent(loopIndex, this::createCallActivityHandlerBefore)");
  }
}
