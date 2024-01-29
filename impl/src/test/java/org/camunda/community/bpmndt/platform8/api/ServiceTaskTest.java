package org.camunda.community.bpmndt.platform8.api;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.camunda.community.bpmndt.test.Platform8TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
public class ServiceTaskTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;
  ZeebeClient client;

  private JobHandler handler;

  @BeforeEach
  public void setUp() {
    handler = new JobHandler("serviceTask", "= \"serviceTaskType\"");
  }

  @Test
  public void testExecute() {
    var workerBuilder = client.newWorker().jobType("serviceTaskType").handler((client, job) ->
        client.newCompleteCommand(job).variable("test", "123").send()
    );

    try (var ignored = workerBuilder.open()) {
      tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
    }
  }

  @Test
  public void testExecuteWithVariableMap() {
    var workerBuilder = client.newWorker().jobType("serviceTaskType").handler((client, job) -> {
      assertThat(job.getVariables()).isEqualTo("{\"x\":\"test\",\"y\":1,\"z\":true}");

      assertThat(job.getVariable("x")).isEqualTo("test");
      assertThat(job.getVariable("y")).isEqualTo(1);
      assertThat(job.getVariable("z")).isEqualTo(true);

      assertThat(job.getVariablesAsMap()).containsExactly("x", "test", "y", 1, "z", true);

      client.newCompleteCommand(job).send();
    });

    try (var ignored = workerBuilder.open()) {
      tc.createExecutor(engine)
          .withVariable("x", "test")
          .withVariableMap(Map.of("y", 1, "z", true))
          .verify(ProcessInstanceAssert::isCompleted)
          .execute();
    }
  }

  @Test
  public void testExecuteWithVariables() {
    var workerBuilder = client.newWorker().jobType("serviceTaskType").handler((client, job) -> {
      var variables = job.getVariablesAsType(TestVariables.class);
      assertThat(variables.getX()).isEqualTo("test");
      assertThat(variables.getY()).isEqualTo(1);
      assertThat(variables.isZ()).isTrue();

      client.newCompleteCommand(job).send();
    });

    try (var ignored = workerBuilder.open()) {
      var variables = new TestVariables();
      variables.setX("test");
      variables.setY(1);
      variables.setZ(true);

      tc.createExecutor(engine)
          .withVariables(variables)
          .verify(ProcessInstanceAssert::isCompleted)
          .execute();
    }
  }

  @Test
  public void testVerifyEvaluatedType() {
    var workerBuilder = client.newWorker().jobType("serviceTaskType").handler((client, job) ->
        client.newCompleteCommand(job).send()
    );

    handler.verifyEvaluatedType("wrongType");

    try (var ignored = workerBuilder.open()) {
      var e = assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

      assertThat(e).hasMessageThat().contains("wrongType");
      assertThat(e).hasMessageThat().contains("serviceTaskType");
    }

    handler.verifyEvaluatedType("serviceTaskType");

    try (var ignored = workerBuilder.open()) {
      tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
    }
  }

  @Test
  public void testVerifyType() {
    var workerBuilder = client.newWorker().jobType("serviceTaskType").handler((client, job) ->
        client.newCompleteCommand(job).send()
    );

    handler.verifyType("wrongType");

    try (var ignored = workerBuilder.open()) {
      var e = assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

      assertThat(e).hasMessageThat().contains("wrongType");
      assertThat(e).hasMessageThat().contains("= \"serviceTaskType\"");
    }

    handler.verifyType("= \"serviceTaskType\"");

    try (var ignored = workerBuilder.open()) {
      tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
    }
  }

  @Test
  public void testExecuteAction() {
    handler.execute((client, job) ->
        client.newCompleteCommand(job).send()
    );

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  public void testExecuteActionWithVariables() {
    var variables = new TestVariables();
    variables.setX("test");
    variables.setY(1);
    variables.setZ(true);

    handler.withVariables(variables).execute();

    tc.createExecutor(engine).verify(piAssert -> {
      piAssert.isCompleted();

      piAssert.hasVariableWithValue("x", "test");
      piAssert.hasVariableWithValue("y", 1);
      piAssert.hasVariableWithValue("z", true);
    }).execute();
  }

  @Test
  public void testExecuteActionWithVariableMap() {
    var variableMap = new HashMap<String, Object>();
    variableMap.put("y", 1);
    variableMap.put("z", true);

    handler
        .withVariable("x", "test")
        .withVariableMap(variableMap)
        .execute();

    tc.createExecutor(engine).verify(piAssert -> {
      piAssert.isCompleted();

      piAssert.hasVariableWithValue("x", "test");
      piAssert.hasVariableWithValue("y", 1);
      piAssert.hasVariableWithValue("z", true);
    }).execute();
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    public String getBpmnProcessId() {
      return "simpleServiceTask";
    }

    @Override
    public String getEnd() {
      return "endEvent";
    }

    @Override
    public String getStart() {
      return "startEvent";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(Platform8TestPaths.simple("simpleServiceTask.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "startEvent");
      instance.isWaitingAt(processInstanceKey, "serviceTask");
      instance.apply(processInstanceKey, handler);
      instance.hasPassed(processInstanceKey, "serviceTask");
      instance.hasPassed(processInstanceKey, "endEvent");
      instance.isCompleted(processInstanceKey);
    }
  }

  public static class TestVariables {

    private String x;
    private int y;
    private boolean z;

    public String getX() {
      return x;
    }

    public int getY() {
      return y;
    }

    public boolean isZ() {
      return z;
    }

    public void setX(String x) {
      this.x = x;
    }

    public void setY(int y) {
      this.y = y;
    }

    public void setZ(boolean z) {
      this.z = z;
    }
  }
}
