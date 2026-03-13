package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;

import org.camunda.community.bpmndt.api.TestCaseInstanceElement.TimerEventElement;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class TimerCatchEventTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  private TimerEventHandler handler;

  @BeforeEach
  void setUp() {
    var element = new TimerEventElement();
    element.id = "timerCatchEvent";
    element.timeDate = "2024-02-01T10:11:12Z";
    element.timeDuration = "PT1H";

    handler = new TimerEventHandler(element);
  }

  @Test
  void testExecute() {
    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerify() {
    handler.verify(processInstanceAssert -> processInstanceAssert.hasVariable("x", "test"));

    tc.createExecutor(client, processTestContext)
        .withVariable("x", "test")
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  void testVerifyTimeDate() {
    handler.verifyTimeDate(date -> assertThat(date).isEqualTo(LocalDateTime.now()));

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());

    handler.verifyTimeDate(date -> assertThat(date).isLessThan(LocalDateTime.now().plusHours(1)));

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyTimeDateExpression() {
    handler.verifyTimeDateExpression(expr -> assertThat(expr).isEqualTo("wrong date"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());

    handler.verifyTimeDateExpression(expr -> assertThat(expr).isEqualTo("2024-02-01T10:11:12Z"));

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyTimeDuration() {
    handler.verifyTimeDuration(duration -> assertThat(duration.toMillis()).isEqualTo(0));

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());

    handler.verifyTimeDuration(duration -> assertThat(duration.toMillis()).isEqualTo(3600000));

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyTimeDurationExpression() {
    handler.verifyTimeDurationExpression(expr -> assertThat(expr).isEqualTo("wrong duration"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());

    handler.verifyTimeDurationExpression(expr -> assertThat(expr).isEqualTo("PT1H"));

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "startEvent");
      instance.isWaitingAt(processInstanceKey, "timerCatchEvent");
      instance.apply(processInstanceKey, handler);
      instance.hasPassed(processInstanceKey, "timerCatchEvent");
      instance.hasPassed(processInstanceKey, "endEvent");
    }

    @Override
    public String getBpmnProcessId() {
      return "simpleTimerCatchEvent";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.simple("simpleTimerCatchEvent.bpmn"));
      } catch (IOException e) {
        return null;
      }
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
