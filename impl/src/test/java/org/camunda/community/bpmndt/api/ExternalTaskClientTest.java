package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.client.variable.ClientValues;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.community.bpmndt.test.TestPaths;
import org.camunda.spin.Spin;
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ExternalTaskClientTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  private ExternalTaskClientHandler<?> handler;

  @BeforeEach
  public void setUp() {
    handler = new ExternalTaskClientHandler<>(tc.getProcessEngine(), "externalTask", "test-topic");
  }

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }

  @Test
  public void testExecuteExternalTask() {
    handler.executeExternalTask((externalTask, externalTaskService) -> {
      var expected = tc.getProcessEngine().getExternalTaskService().createExternalTaskQuery()
          .externalTaskId(externalTask.getId())
          .singleResult();

      assertThat(externalTask.getActivityId()).isEqualTo("externalTask");
      assertThat(externalTask.getActivityInstanceId()).isEqualTo(expected.getActivityInstanceId());
      assertThat(externalTask.getBusinessKey()).isEqualTo("executeExternalTask");
      assertThat(externalTask.getCreateTime()).isEqualTo(expected.getCreateTime());
      assertThat(externalTask.getExtensionProperties()).isEmpty();
      assertThat(externalTask.getExtensionProperty("x")).isNull();
      assertThat(externalTask.getLockExpirationTime()).isEqualTo(expected.getLockExpirationTime());
      assertThat(externalTask.getPriority()).isEqualTo(50);
      assertThat(externalTask.getProcessDefinitionId()).isEqualTo(expected.getProcessDefinitionId());
      assertThat(externalTask.getProcessDefinitionKey()).isEqualTo("simpleExternalTask");
      assertThat(externalTask.getProcessInstanceId()).isEqualTo(expected.getProcessInstanceId());
      assertThat(externalTask.getProcessDefinitionVersionTag()).isEqualTo("v1");
      assertThat(externalTask.getRetries()).isNull();
      assertThat(externalTask.getTenantId()).isEqualTo(expected.getTenantId());
      assertThat(externalTask.getTopicName()).isEqualTo("test-topic");
      assertThat(externalTask.getWorkerId()).isEqualTo(ExternalTaskHandler.WORKER_ID);

      assertThat(externalTask.getLockExpirationTime().getTime() - externalTask.getCreateTime().getTime() >= 20_000).isTrue();

      assertThat(externalTask.getAllVariables()).containsEntry("k1", "v1");
      assertThat(externalTask.getAllVariables()).containsEntry("k2", "v2");

      assertThat(externalTask.getAllVariables()).containsEntry("kl1", "vl1");

      externalTaskService.complete(externalTask);
    });

    VariableMap variables = Variables.createVariables();
    variables.put("k1", "v1");
    variables.put("k2", "v2");

    tc.createExecutor()
        .withBusinessKey("executeExternalTask")
        .withVariables(variables)
        .execute();
  }

  @Test
  public void testExecuteExternalTaskWithComplexVariable() {
    handler.executeExternalTask((externalTask, externalTaskService) -> {
      ComplexVariable input = externalTask.getVariable("input");
      assertThat(input.getVboolean()).isEqualTo(true);
      assertThat(input.getVinteger()).isEqualTo(123);
      assertThat(input.getVstring()).isEqualTo("abc");

      ComplexVariable output = new ComplexVariable();
      output.setVboolean(true);
      output.setVinteger(456);
      output.setVstring("def");

      VariableMap variables = Variables.createVariables()
          .putValue("output", ClientValues.objectValue(output).serializationDataFormat("application/json").create())
          .putValue("outputUntyped", output);

      externalTaskService.complete(externalTask, variables);
    });

    ComplexVariable input = new ComplexVariable();
    input.setVboolean(true);
    input.setVinteger(123);
    input.setVstring("abc");

    tc.createExecutor()
        .withVariable("input", Variables.objectValue(input).serializationDataFormat("application/json").create())
        .verify(piAssert -> {
          Map<String, Object> variables = piAssert.variables().actual();

          ComplexVariable output = (ComplexVariable) variables.get("output");
          assertThat(output.getVboolean()).isEqualTo(true);
          assertThat(output.getVinteger()).isEqualTo(456);
          assertThat(output.getVstring()).isEqualTo("def");

          ComplexVariable outputUntyped = (ComplexVariable) variables.get("outputUntyped");
          assertThat(outputUntyped.getVboolean()).isEqualTo(true);
          assertThat(outputUntyped.getVinteger()).isEqualTo(456);
          assertThat(outputUntyped.getVstring()).isEqualTo("def");
        })
        .execute();
  }

  @Test
  public void testExecuteExternalTaskWithJson() {
    handler.executeExternalTask((externalTask, externalTaskService) -> {
      TypedValue inputJsonTyped = externalTask.getVariableTyped("inputJson");
      assertThat(inputJsonTyped).isNotNull();
      assertThat(inputJsonTyped.getValue().toString()).isEqualTo("{}");

      Object inputJson = externalTask.getVariable("inputJson");
      assertThat(inputJson).isNotNull();
      assertThat(inputJson).isInstanceOf(Map.class);

      VariableMap variables = Variables.createVariables()
          .putValue("outputJson", ClientValues.jsonValue("{}"));

      externalTaskService.complete(externalTask, variables);
    });

    tc.createExecutor()
        .withVariable("inputJson", Spin.JSON("{}"))
        .verify(piAssert -> {
          Map<String, Object> variables = piAssert.variables().actual();

          Object outputJson = variables.get("outputJson");
          assertThat(outputJson).isNotNull();
          assertThat(outputJson.toString()).isEqualTo("{}");
        })
        .execute();
  }

  @Test
  public void testWithFetchExtensionProperties() {
    handler.withFetchExtensionProperties(true).executeExternalTask((externalTask, externalTaskService) -> {
      assertThat(externalTask.getExtensionProperties()).isNotNull();
      assertThat(externalTask.getExtensionProperties().get("x")).isEqualTo("y");

      externalTaskService.complete(externalTask);
    });

    tc.createExecutor()
        .withBusinessKey("executeExternalTask")
        .execute();
  }

  @Test
  public void testWithFetchLocalVariablesOnly() {
    handler.withFetchLocalVariablesOnly(true).executeExternalTask((externalTask, externalTaskService) -> {
      assertThat(externalTask.getAllVariables()).doesNotContainKey("k1");
      assertThat(externalTask.getAllVariables()).doesNotContainKey("k2");

      assertThat(externalTask.getAllVariables()).containsEntry("kl1", "vl1");

      externalTaskService.complete(externalTask);
    });

    VariableMap variables = Variables.createVariables();
    variables.put("k1", "v1");
    variables.put("k2", "v2");

    tc.createExecutor()
        .withVariables(variables)
        .execute();
  }

  @Test
  public void testWithLockDuration() {
    handler.withLockDuration(60_000).executeExternalTask((externalTask, externalTaskService) -> {
      assertThat(externalTask.getLockExpirationTime().getTime() - externalTask.getCreateTime().getTime() >= 60_000).isTrue();

      externalTaskService.complete(externalTask);
    });

    tc.createExecutor()
        .withBusinessKey("executeExternalTask")
        .execute();
  }

  private class TestCase extends AbstractJUnit5TestCase<TestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi).isNotNull();

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("externalTask");

      instance.apply(handler);

      piAssert.hasPassed("externalTask", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.simple("simpleExternalTask.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "simpleExternalTask";
    }

    @Override
    protected List<ProcessEnginePlugin> getProcessEnginePlugins() {
      return List.of(new SpinProcessEnginePlugin());
    }

    @Override
    public String getStart() {
      return "startEvent";
    }

    @Override
    public String getEnd() {
      return "endEvent";
    }
  }

  public static class ComplexVariable {

    private Boolean vboolean;
    private String vstring;
    private Integer vinteger;

    public Boolean getVboolean() {
      return vboolean;
    }

    public void setVboolean(Boolean vboolean) {
      this.vboolean = vboolean;
    }

    public String getVstring() {
      return vstring;
    }

    public void setVstring(String vstring) {
      this.vstring = vstring;
    }

    public Integer getVinteger() {
      return vinteger;
    }

    public void setVinteger(Integer vinteger) {
      this.vinteger = vinteger;
    }
  }
}
