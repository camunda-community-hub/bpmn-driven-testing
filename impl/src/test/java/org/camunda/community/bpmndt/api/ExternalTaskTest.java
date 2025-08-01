package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.externaltask.ExternalTask;
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

  private ExternalTaskHandler<?> handler;

  private ExternalTaskService externalTaskService;

  @BeforeEach
  public void setUp() {
    handler = new ExternalTaskHandler<>(tc.getProcessEngine(), "externalTask", "test-topic");

    externalTaskService = tc.getProcessEngine().getExternalTaskService();
  }

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }

  @Test
  public void testExecuteExternalTask() {
    handler.executeExternalTask(externalTask -> {
      ExternalTask expected = externalTaskService.createExternalTaskQuery()
          .externalTaskId(externalTask.getId())
          .singleResult();

      assertThat(externalTask.getActivityId()).isEqualTo(expected.getActivityId());
      assertThat(externalTask.getActivityId()).isEqualTo("externalTask");
      assertThat(externalTask.getActivityInstanceId()).isEqualTo(expected.getActivityInstanceId());
      assertThat(externalTask.getBusinessKey()).isEqualTo(expected.getBusinessKey());
      assertThat(externalTask.getBusinessKey()).isEqualTo("executeExternalTask");
      assertThat(externalTask.getCreateTime()).isEqualTo(expected.getCreateTime());
      assertThat(externalTask.getExtensionProperties()).isEmpty();
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

      externalTaskService.complete(externalTask.getId(), externalTask.getWorkerId());
    });

    tc.createExecutor()
        .withBusinessKey("executeExternalTask")
        .execute();
  }

  @Test
  public void testExecuteLockedExternalTask() {
    handler.executeLockedExternalTask(lockedExternalTask -> {
      assertThat(lockedExternalTask.getVariables()).containsEntry("k1", "v1");
      assertThat(lockedExternalTask.getVariables()).containsEntry("k2", "v2");

      assertThat(lockedExternalTask.getVariables()).containsEntry("kl1", "vl1");

      String errorMessage = "errorMessage";
      String errorDetails = "errorDetails";
      VariableMap variables = Variables.putValue("k3", "v3");
      VariableMap localVariables = Variables.putValue("k4", "v4");

      externalTaskService.handleFailure(
          lockedExternalTask.getId(),
          lockedExternalTask.getWorkerId(),
          errorMessage,
          errorDetails,
          2,
          1L,
          variables,
          localVariables
      );

      externalTaskService.complete(lockedExternalTask.getId(), lockedExternalTask.getWorkerId());
    });

    VariableMap variables = Variables.createVariables();
    variables.put("k1", "v1");
    variables.put("k2", "v2");

    tc.createExecutor()
        .withVariables(variables)
        .verify(piAssert -> {
          piAssert.variables().containsEntry("k3", "v3");
          piAssert.variables().containsEntry("k4", "v4");
        })
        .execute();
  }

  @Test
  public void testWithFetchExtensionProperties() {
    handler.withFetchExtensionProperties(true).executeExternalTask(externalTask -> {
      assertThat(externalTask.getExtensionProperties()).isNotNull();
      assertThat(externalTask.getExtensionProperties().get("x")).isEqualTo("y");

      externalTaskService.complete(externalTask.getId(), externalTask.getWorkerId());
    });

    tc.createExecutor()
        .withBusinessKey("executeExternalTask")
        .execute();
  }

  @Test
  public void testWithFetchLocalVariablesOnly() {
    handler.withFetchLocalVariablesOnly(true).executeLockedExternalTask(lockedExternalTask -> {
      assertThat(lockedExternalTask.getVariables()).doesNotContainKey("k1");
      assertThat(lockedExternalTask.getVariables()).doesNotContainKey("k2");

      assertThat(lockedExternalTask.getVariables()).containsEntry("kl1", "vl1");

      externalTaskService.complete(lockedExternalTask.getId(), lockedExternalTask.getWorkerId());
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
    handler.withLockDuration(60_000).executeExternalTask(externalTask -> {
      assertThat(externalTask.getLockExpirationTime().getTime() - externalTask.getCreateTime().getTime() >= 60_000).isTrue();

      externalTaskService.complete(externalTask.getId(), externalTask.getWorkerId());
    });

    tc.createExecutor()
        .withBusinessKey("executeExternalTask")
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
