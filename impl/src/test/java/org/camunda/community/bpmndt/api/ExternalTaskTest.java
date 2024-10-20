package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ExternalTaskTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  private ExternalTaskHandler handler;

  private ExternalTaskService externalTaskService;

  @BeforeEach
  public void setUp() {
    handler = new ExternalTaskHandler(tc.getProcessEngine(), "externalTask", "test-topic");

    externalTaskService = tc.getProcessEngine().getExternalTaskService();
  }

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }

  @Test
  public void testExecuteExternalTask() {
    handler.executeExternalTask(externalTask -> {
      assertThat(externalTask.getActivityId()).isEqualTo("externalTask");
      assertThat(externalTask.getBusinessKey()).isEqualTo("executeExternalTask");
      assertThat(externalTask.getLockExpirationTime()).isNotNull();
      assertThat(externalTask.getProcessDefinitionKey()).isEqualTo("simpleExternalTask");
      assertThat(externalTask.getTopicName()).isEqualTo("test-topic");
      assertThat(externalTask.getWorkerId()).isEqualTo(ExternalTaskHandler.WORKER_ID);

      externalTaskService.complete(externalTask.getId(), externalTask.getWorkerId());
    });

    tc.createExecutor()
        .withBusinessKey("executeExternalTask")
        .execute();
  }

  @Test
  public void testExecuteLockedExternalTask() {
    handler.executeLockedExternalTask(externalTask -> {
      assertThat(externalTask.getActivityId()).isEqualTo("externalTask");
      assertThat(externalTask.getBusinessKey()).isEqualTo("executeLockedExternalTask");
      assertThat(externalTask.getLockExpirationTime()).isNotNull();
      assertThat(externalTask.getProcessDefinitionKey()).isEqualTo("simpleExternalTask");
      assertThat(externalTask.getTopicName()).isEqualTo("test-topic");
      assertThat(externalTask.getVariables()).containsEntry("k1", "v1");
      assertThat(externalTask.getVariables()).containsEntry("k2", "v2");
      assertThat(externalTask.getWorkerId()).isEqualTo(ExternalTaskHandler.WORKER_ID);

      String errorMessage = "errorMessage";
      String errorDetails = "errorDetails";
      VariableMap variables = Variables.putValue("k3", "v3");
      VariableMap localVariables = Variables.putValue("k4", "v4");

      externalTaskService.handleFailure(externalTask.getId(), externalTask.getWorkerId(), errorMessage, errorDetails, 2, 1L, variables, localVariables);

      ExternalTask failedTask = externalTaskService.createExternalTaskQuery()
          .externalTaskId(externalTask.getId())
          .singleResult();

      LockedExternalTask wrappedTask = handler.wrap(failedTask);
      assertThat(wrappedTask.getActivityId()).isEqualTo("externalTask");
      assertThat(wrappedTask.getBusinessKey()).isEqualTo("executeLockedExternalTask");
      assertThat(wrappedTask.getErrorDetails()).isEqualTo(errorDetails);
      assertThat(wrappedTask.getErrorMessage()).isEqualTo(errorMessage);
      assertThat(wrappedTask.getLockExpirationTime()).isNotNull();
      assertThat(wrappedTask.getProcessDefinitionKey()).isEqualTo("simpleExternalTask");
      assertThat(wrappedTask.getRetries()).isEqualTo(2);
      assertThat(wrappedTask.getTopicName()).isEqualTo("test-topic");
      assertThat(wrappedTask.getVariables()).containsEntry("k1", "v1");
      assertThat(wrappedTask.getVariables()).containsEntry("k2", "v2");
      assertThat(wrappedTask.getVariables()).containsEntry("k3", "v3");
      assertThat(wrappedTask.getVariables()).containsEntry("k4", "v4");
      assertThat(wrappedTask.getWorkerId()).isEqualTo(ExternalTaskHandler.WORKER_ID);

      externalTaskService.complete(wrappedTask.getId(), wrappedTask.getWorkerId());
    });

    VariableMap variables = Variables.createVariables();
    variables.put("k1", "v1");
    variables.put("k2", "v2");

    tc.createExecutor()
        .withBusinessKey("executeLockedExternalTask")
        .withVariables(variables)
        .verify(piAssert -> {
          piAssert.variables().containsEntry("k3", "v3");
          piAssert.variables().containsEntry("k4", "v4");
        })
        .execute();
  }

  @Test
  public void testWithVariables() {
    handler.withVariable("a", "b").withVariableTyped("x", Variables.stringValue("y")).complete();

    tc.createExecutor().verify(pi -> {
      pi.variables().containsEntry("a", "b");
      pi.variables().containsEntry("x", "y");
    }).execute();
  }

  @Test
  public void testWithWorkerId() {
    final String workerId = "test-worker";

    handler.withWorkerId(workerId);

    handler.executeLockedExternalTask(externalTask -> {
      assertThat(externalTask.getWorkerId()).isEqualTo(workerId);

      externalTaskService.complete(externalTask.getId(), externalTask.getWorkerId());
    });

    tc.createExecutor().execute();
  }

  @Test
  public void testVerify() {
    handler.withVariable("a", "b").verify((pi, topicName) -> {
      assertThat(pi).isNotNull();
      assertThat(topicName).isEqualTo("test-topic");
    });

    tc.createExecutor().execute();
  }

  /**
   * Tests that the external task is not completed, when it should wait for a boundary event.
   */
  @Test
  public void testWaitForBoundaryEvent() {
    assertThat(handler.isWaitingForBoundaryEvent()).isFalse();
    handler.waitForBoundaryEvent();
    assertThat(handler.isWaitingForBoundaryEvent()).isTrue();

    AssertionError e = assertThrows(AssertionError.class, () -> tc.createExecutor().execute());

    // has not passed
    assertThat(e.getMessage()).contains("to have passed activities [externalTask, endEvent]");
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
    public String getStart() {
      return "startEvent";
    }

    @Override
    public String getEnd() {
      return "endEvent";
    }
  }
}
