package org.camunda.community.bpmndt.platform8.api;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceElement.JobElement;
import org.camunda.community.bpmndt.test.Platform8TestPaths;
import org.camunda.community.bpmndt.test.TestVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.client.api.worker.JobWorkerBuilderStep1.JobWorkerBuilderStep3;
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
    JobElement element = new JobElement();
    element.setId("serviceTask");
    element.setRetries("=3");
    element.setType("=\"serviceTaskType\"");

    handler = new JobHandler(element);
  }

  @Test
  public void testExecute() {
    JobWorkerBuilderStep3 workerBuilder = client.newWorker().jobType("serviceTaskType").handler((client, job) ->
        client.newCompleteCommand(job).variable("test", "123").send()
    );

    try (JobWorker ignored = workerBuilder.open()) {
      tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
    }
  }

  @Test
  public void testExecuteWithCustomAction() {
    handler.execute((client, jobKey) -> {
      client.newCompleteCommand(jobKey).send();
    });

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  public void testExecuteWithVariableMap() {
    JobWorkerBuilderStep3 workerBuilder = client.newWorker().jobType("serviceTaskType").handler((client, job) -> {
      assertThat(job.getVariables()).isEqualTo("{\"x\":\"test\",\"y\":1,\"z\":true}");

      assertThat(job.getVariable("x")).isEqualTo("test");
      assertThat(job.getVariable("y")).isEqualTo(1);
      assertThat(job.getVariable("z")).isEqualTo(true);

      assertThat(job.getVariablesAsMap()).containsExactly("x", "test", "y", 1, "z", true);

      client.newCompleteCommand(job).send();
    });

    Map<String, Object> variableMap = new HashMap<>();
    variableMap.put("y", 1);
    variableMap.put("z", true);

    try (JobWorker ignored = workerBuilder.open()) {
      tc.createExecutor(engine)
          .withVariable("x", "test")
          .withVariableMap(variableMap)
          .verify(ProcessInstanceAssert::isCompleted)
          .execute();
    }
  }

  @Test
  public void testExecuteWithVariables() {
    JobWorkerBuilderStep3 workerBuilder = client.newWorker().jobType("serviceTaskType").handler((client, job) -> {
      TestVariables variables = job.getVariablesAsType(TestVariables.class);
      assertThat(variables.getX()).isEqualTo("test");
      assertThat(variables.getY()).isEqualTo(1);
      assertThat(variables.isZ()).isTrue();

      client.newCompleteCommand(job).send();
    });

    try (JobWorker ignored = workerBuilder.open()) {
      TestVariables variables = new TestVariables();
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
  public void testVerify() {
    handler.verify(processInstanceAssert -> {
      processInstanceAssert.hasVariableWithValue("x", "test");
      processInstanceAssert.hasVariableWithValue("y", 1);
      processInstanceAssert.hasVariableWithValue("z", true);
    });

    handler.execute();

    TestVariables variables = new TestVariables();
    variables.setX("test");
    variables.setY(1);
    variables.setZ(true);

    tc.createExecutor(engine)
        .withVariables(variables)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  public void testVerifyRetries() {
    JobWorkerBuilderStep3 workerBuilder = client.newWorker().jobType("serviceTaskType").handler((client, job) ->
        client.newCompleteCommand(job).send()
    );

    handler.verifyRetries(2);

    try (JobWorker ignored = workerBuilder.open()) {
      AssertionError e = assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());
      assertThat(e).hasMessageThat().contains("but was 3");
      assertThat(e).hasMessageThat().contains("retry count of 2");
    }

    handler.verifyRetries(3);

    handler.verifyRetries(retries -> assertThat(retries).isEqualTo(2));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyRetries(retries -> assertThat(retries).isEqualTo(3));

    try (JobWorker ignored = workerBuilder.open()) {
      tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
    }
  }

  @Test
  public void testVerifyRetriesExpression() {
    JobWorkerBuilderStep3 workerBuilder = client.newWorker().jobType("serviceTaskType").handler((client, job) ->
        client.newCompleteCommand(job).send()
    );

    handler.verifyRetriesExpression(expr -> assertThat(expr).isEqualTo("wrong retries expression"));

    try (JobWorker ignored = workerBuilder.open()) {
      assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());
    }

    handler.verifyRetriesExpression(expr -> assertThat(expr).isEqualTo("=3"));

    try (JobWorker ignored = workerBuilder.open()) {
      tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
    }
  }

  @Test
  public void testVerifyType() {
    JobWorkerBuilderStep3 workerBuilder = client.newWorker().jobType("serviceTaskType").handler((client, job) ->
        client.newCompleteCommand(job).send()
    );

    handler.verifyType("wrong type");

    try (JobWorker ignored = workerBuilder.open()) {
      AssertionError e = assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());
      assertThat(e).hasMessageThat().contains("'wrong type'");
      assertThat(e).hasMessageThat().contains("'serviceTaskType'");
    }

    handler.verifyType("serviceTaskType");
    handler.verifyType(type -> assertThat(type).isEqualTo("serviceTaskType"));

    try (JobWorker ignored = workerBuilder.open()) {
      tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
    }
  }

  @Test
  public void testVerifyTypeExpression() {
    JobWorkerBuilderStep3 workerBuilder = client.newWorker().jobType("serviceTaskType").handler((client, job) ->
        client.newCompleteCommand(job).send()
    );

    handler.verifyTypeExpression(expr -> assertThat(expr).isEqualTo("wrong type expression"));

    try (JobWorker ignored = workerBuilder.open()) {
      assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());
    }

    handler.verifyTypeExpression(expr -> assertThat(expr).isEqualTo("=\"serviceTaskType\""));

    try (JobWorker ignored = workerBuilder.open()) {
      tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
    }
  }

  @Test
  public void testExecuteAction() {
    handler.execute((client, job) -> client.newCompleteCommand(job).send());

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  public void testExecuteActionWithVariables() {
    TestVariables variables = new TestVariables();
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
    Map<String, Object> variableMap = new HashMap<>();
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
    protected void execute(TestCaseInstance instance, ProcessInstanceEvent processInstanceEvent) {
      instance.hasPassed(processInstanceEvent, "startEvent");
      instance.isWaitingAt(processInstanceEvent, "serviceTask");
      instance.apply(processInstanceEvent, handler);
      instance.hasPassed(processInstanceEvent, "serviceTask");
      instance.hasPassed(processInstanceEvent, "endEvent");
      instance.isCompleted(processInstanceEvent);
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
        return Files.newInputStream(Platform8TestPaths.simple("simpleServiceTask.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }
  }
}
