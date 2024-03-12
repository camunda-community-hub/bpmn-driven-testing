package org.camunda.community.bpmndt.platform8.api;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;

import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceElement.TimerEventElement;
import org.camunda.community.bpmndt.test.Platform8TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
public class TimerCatchEventTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;

  private TimerEventHandler handler;

  @BeforeEach
  public void setUp() {
    TimerEventElement element = new TimerEventElement();
    element.setId("timerCatchEvent");
    element.setTimeDate("2024-02-01T10:11:12Z");
    element.setTimeDuration("PT1H");

    handler = new TimerEventHandler(element);
  }

  @Test
  public void testExecute() {
    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  public void testVerify() {
    handler.verify(processInstanceAssert -> processInstanceAssert.hasVariableWithValue("x", "test"));

    tc.createExecutor(engine)
        .withVariable("x", "test")
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  public void testVerifyTimeDate() {
    handler.verifyTimeDate(date -> assertThat(date).isEqualTo(LocalDateTime.now()));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyTimeDate(date -> assertThat(date).isLessThan(LocalDateTime.now().plusHours(1)));

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  public void testVerifyTimeDateExpression() {
    handler.verifyTimeDateExpression(expr -> assertThat(expr).isEqualTo("wrong date"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyTimeDateExpression(expr -> assertThat(expr).isEqualTo("2024-02-01T10:11:12Z"));

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  public void testVerifyTimeDuration() {
    handler.verifyTimeDuration(duration -> assertThat(duration.toMillis()).isEqualTo(0));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyTimeDuration(duration -> assertThat(duration.toMillis()).isEqualTo(3600000));

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  public void testVerifyTimeDurationExpression() {
    handler.verifyTimeDurationExpression(expr -> assertThat(expr).isEqualTo("wrong duration"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyTimeDurationExpression(expr -> assertThat(expr).isEqualTo("PT1H"));

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, ProcessInstanceEvent processInstanceEvent) {
      instance.hasPassed(processInstanceEvent, "startEvent");
      instance.isWaitingAt(processInstanceEvent, "timerCatchEvent");
      instance.apply(processInstanceEvent, handler);
      instance.hasPassed(processInstanceEvent, "timerCatchEvent");
      instance.hasPassed(processInstanceEvent, "endEvent");
    }

    @Override
    public String getBpmnProcessId() {
      return "simpleTimerCatchEvent";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(Platform8TestPaths.simple("simpleTimerCatchEvent.bpmn"));
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
