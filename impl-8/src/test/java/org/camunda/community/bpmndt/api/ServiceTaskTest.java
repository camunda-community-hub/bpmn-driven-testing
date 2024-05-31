package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;

import org.camunda.community.bpmndt.api.TestCaseInstanceElement.JobElement;
import org.camunda.community.bpmndt.test.TestPaths;
import org.camunda.community.bpmndt.test.TestVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class ServiceTaskTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;
  ZeebeClient client;

  private JobHandler handler;

  @BeforeEach
  void setUp() {
    var element = new JobElement();
    element.id = "serviceTask";
    element.retries = "=3";
    element.type = "=\"serviceTaskType\"";

    handler = new JobHandler(element);
  }

  @Test
  void testExecute() {
    var workerBuilder = client.newWorker()
        .jobType("serviceTaskType")
        .handler((client, job) -> client.newCompleteCommand(job).variable("test", "123").send());

    try (var ignored = workerBuilder.open()) {
      tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
    }
  }

  @Test
  void testExecuteWithCustomAction() {
    handler.execute((client, jobKey) -> client.newCompleteCommand(jobKey).send());

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testExecuteWithVariableMap() {
    var workerBuilder = client.newWorker().jobType("serviceTaskType").handler((client, job) -> {
      assertThat(job.getVariables()).isEqualTo("{\"x\":\"test\",\"y\":1,\"z\":true}");

      assertThat(job.getVariable("x")).isEqualTo("test");
      assertThat(job.getVariable("y")).isEqualTo(1);
      assertThat(job.getVariable("z")).isEqualTo(true);

      assertThat(job.getVariablesAsMap()).containsExactly("x", "test", "y", 1, "z", true);

      client.newCompleteCommand(job).send().join();
    });

    var variableMap = new HashMap<String, Object>();
    variableMap.put("y", 1);
    variableMap.put("z", true);

    try (var ignored = workerBuilder.open()) {
      tc.createExecutor(engine)
          .withVariable("x", "test")
          .withVariableMap(variableMap)
          .verify(ProcessInstanceAssert::isCompleted)
          .execute();
    }
  }

  @Test
  void testExecuteWithVariables() {
    var workerBuilder = client.newWorker().jobType("serviceTaskType").handler((client, job) -> {
      var variables = job.getVariablesAsType(TestVariables.class);
      assertThat(variables.getX()).isEqualTo("test");
      assertThat(variables.getY()).isEqualTo(1);
      assertThat(variables.isZ()).isTrue();

      client.newCompleteCommand(job).send().join();
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
  void testVerify() {
    handler.verify(processInstanceAssert -> {
      processInstanceAssert.hasVariableWithValue("x", "test");
      processInstanceAssert.hasVariableWithValue("y", 1);
      processInstanceAssert.hasVariableWithValue("z", true);
    });

    handler.complete();

    var variables = new TestVariables();
    variables.setX("test");
    variables.setY(1);
    variables.setZ(true);

    tc.createExecutor(engine)
        .withVariables(variables)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  void testVerifyRetries() {
    var workerBuilder = client.newWorker().jobType("serviceTaskType").handler((client, job) ->
        client.newCompleteCommand(job).send()
    );

    handler.verifyRetries(2);

    try (var ignored = workerBuilder.open()) {
      var e = assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());
      assertThat(e).hasMessageThat().contains("but was 3");
      assertThat(e).hasMessageThat().contains("retry count of 2");
    }

    handler.verifyRetries(3);

    handler.verifyRetries(retries -> assertThat(retries).isEqualTo(2));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyRetries(retries -> assertThat(retries).isEqualTo(3));

    try (var ignored = workerBuilder.open()) {
      tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
    }
  }

  @Test
  void testVerifyRetriesExpression() {
    var workerBuilder = client.newWorker().jobType("serviceTaskType").handler((client, job) ->
        client.newCompleteCommand(job).send()
    );

    handler.verifyRetriesExpression(expr -> assertThat(expr).isEqualTo("wrong retries expression"));

    try (var ignored = workerBuilder.open()) {
      assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());
    }

    handler.verifyRetriesExpression(expr -> assertThat(expr).isEqualTo("=3"));

    try (var ignored = workerBuilder.open()) {
      tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
    }
  }

  @Test
  void testVerifyType() {
    var workerBuilder = client.newWorker().jobType("serviceTaskType").handler((client, job) ->
        client.newCompleteCommand(job).send()
    );

    handler.verifyType("wrong type");

    try (var ignored = workerBuilder.open()) {
      var e = assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());
      assertThat(e).hasMessageThat().contains("'wrong type'");
      assertThat(e).hasMessageThat().contains("'serviceTaskType'");
    }

    handler.verifyType("serviceTaskType");
    handler.verifyType(type -> assertThat(type).isEqualTo("serviceTaskType"));

    try (var ignored = workerBuilder.open()) {
      tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
    }
  }

  @Test
  void testVerifyTypeExpression() {
    var workerBuilder = client.newWorker().jobType("serviceTaskType").handler((client, job) ->
        client.newCompleteCommand(job).send()
    );

    handler.verifyTypeExpression(expr -> assertThat(expr).isEqualTo("wrong type expression"));

    try (var ignored = workerBuilder.open()) {
      assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());
    }

    handler.verifyTypeExpression(expr -> assertThat(expr).isEqualTo("=\"serviceTaskType\""));

    try (var ignored = workerBuilder.open()) {
      tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
    }
  }

  @Test
  void testExecuteAction() {
    handler.execute((client, jobKey) -> client.newCompleteCommand(jobKey).send());

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testCompleteWithVariables() {
    var variables = new TestVariables();
    variables.setX("test");
    variables.setY(1);
    variables.setZ(true);

    handler.withVariables(variables).complete();

    tc.createExecutor(engine).verify(piAssert -> {
      piAssert.isCompleted();

      piAssert.hasVariableWithValue("x", "test");
      piAssert.hasVariableWithValue("y", 1);
      piAssert.hasVariableWithValue("z", true);
    }).execute();
  }

  @Test
  void testCompleteWithVariableMap() {
    var variableMap = new HashMap<String, Object>();
    variableMap.put("y", 1);
    variableMap.put("z", true);

    handler
        .withVariable("x", "test")
        .withVariableMap(variableMap)
        .complete();

    tc.createExecutor(engine).verify(piAssert -> {
      piAssert.isCompleted();

      piAssert.hasVariableWithValue("x", "test");
      piAssert.hasVariableWithValue("y", 1);
      piAssert.hasVariableWithValue("z", true);
    }).execute();
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "startEvent");
      instance.isWaitingAt(processInstanceKey, "serviceTask");
      instance.apply(processInstanceKey, handler);
      instance.hasPassed(processInstanceKey, "serviceTask");
      instance.hasPassed(processInstanceKey, "endEvent");
      instance.isCompleted(processInstanceKey);
    }

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
        return Files.newInputStream(TestPaths.simple("simpleServiceTask.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }
  }
}
