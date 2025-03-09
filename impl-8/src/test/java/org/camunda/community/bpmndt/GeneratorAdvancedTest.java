package org.camunda.community.bpmndt;

import static com.google.common.truth.Truth.assertThat;
import static org.camunda.community.bpmndt.test.FieldSpecSubject.assertThat;
import static org.camunda.community.bpmndt.test.MethodSpecSubject.assertThat;
import static org.camunda.community.bpmndt.test.TypeSpecSubject.assertThat;

import java.nio.file.Path;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;
import org.camunda.community.bpmndt.api.CallActivityBindingType;
import org.camunda.community.bpmndt.strategy.DefaultStrategy;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;

class GeneratorAdvancedTest {

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
    ctx.setMainResourcePath(TestPaths.advanced());
    ctx.setTestSourcePath(temporaryDirectory);

    ctx.setPackageName("org.example");

    result = generator.getResult();

    var fileName = testInfo.getTestMethod().orElseThrow(NoSuchElementException::new).getName().replace("test", "") + ".bpmn";
    bpmnFile = ctx.getMainResourcePath().resolve(StringUtils.uncapitalize(fileName));
  }

  @Test
  void testCallActivityBindingVersionTag() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(1);

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
    expected = "callActivityElement.bindingType = %s.VERSION_TAG;";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(String.format(expected, ClassName.get(CallActivityBindingType.class)));
    expected = "callActivityElement.processId = \"advanced\";";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);
    expected = "callActivityElement.propagateAllChildVariables = true;";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);
    expected = "callActivityElement.propagateAllParentVariables = true;";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);
    expected = "callActivityElement.versionTag = \"v1\";";
    assertThat(typeSpec.methodSpecs.get(0)).containsCode(expected);

    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.isWaitingAt(flowScopeKey, \"callActivity\");");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.apply(flowScopeKey, callActivity);");
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(flowScopeKey, \"callActivity\");");
  }

  @Test
  void testCallActivitySubProcess() {
    generator.generateTestCases(ctx, bpmnFile);
    assertThat(result.getFiles()).hasSize(10);

    TypeSpec typeSpec;

    typeSpec = result.getFiles().get(1).typeSpec; // errorEndEvent
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.isActivating(flowScopeKey, \"errorEndEvent\");");

    typeSpec = result.getFiles().get(2).typeSpec; // escalationEndEvent
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.isActivating(flowScopeKey, \"escalationEndEvent\");");

    typeSpec = result.getFiles().get(3).typeSpec; // signalEndEvent
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(flowScopeKey, \"signalEndEvent\");");

    typeSpec = result.getFiles().get(4).typeSpec; // terminateEndEvent
    assertThat(typeSpec.methodSpecs.get(1)).containsCode("instance.hasPassed(flowScopeKey, \"terminateEndEvent\");");
  }
}
