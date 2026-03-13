package org.example.it;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.callactivitysubprocess.TC_SubProcessEnd;
import generated.callactivitysubprocess.TC_SubProcessErrorEnd;
import generated.callactivitysubprocess.TC_SubProcessEscalationEnd;
import generated.callactivitysubprocess.TC_SubProcessMessageStart;
import generated.callactivitysubprocess.TC_SubProcessServiceTask;
import generated.callactivitysubprocess.TC_SubProcessSignalEnd;
import generated.callactivitysubprocess.TC_SubProcessSignalStart;
import generated.callactivitysubprocess.TC_SubProcessTerminateEnd;
import generated.callactivitysubprocess.TC_SubProcessTimerStart;
import generated.callactivitysubprocess.TC_SubProcessWait;
import generated.callactivitywithoutsimulation.TC_startEvent__endEvent;
import generated.callactivitywithoutsimulation.TC_startEvent__errorEnd;
import generated.callactivitywithoutsimulation.TC_startEvent__escalationEnd;
import generated.callactivitywithoutsimulation.TC_startEvent__timerEnd;
import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class CallActivityWithoutSimulationTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();
  @RegisterExtension
  TC_startEvent__errorEnd tcError = new TC_startEvent__errorEnd();
  @RegisterExtension
  TC_startEvent__escalationEnd tcEscalation = new TC_startEvent__escalationEnd();
  @RegisterExtension
  TC_startEvent__timerEnd tcTimer = new TC_startEvent__timerEnd();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  @Test
  void testExecute() {
    tc.handleCallActivity().verifyOutput(piAssert -> {
      piAssert.hasVariable("subProcessResult", "value");
    }).executeTestCase(new TC_SubProcessEnd(), it -> {
      it.handleServiceTask().withVariable("subProcessResult", "value").complete();
    });

    tc.createExecutor(client, processTestContext)
        .withAdditionalVersionedClasspathResource("callActivitySubProcess.bpmn", "v1")
        .withVariable("end", "none")
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  void testExecuteError() {
    tcError.handleCallActivity().executeTestCase(new TC_SubProcessErrorEnd(), it -> {
      it.handleServiceTask().complete();
    });

    tcError.createExecutor(client, processTestContext)
        .withAdditionalVersionedClasspathResource("callActivitySubProcess.bpmn", "v1")
        .withVariable("end", "error")
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  void testExecuteSignal() {
    tc.handleCallActivity().executeTestCase(new TC_SubProcessSignalEnd(), it -> {
      it.handleServiceTask().complete();
    });

    tc.createExecutor(client, processTestContext)
        .withAdditionalVersionedClasspathResource("callActivitySubProcess.bpmn", "v1")
        .withVariable("end", "signal")
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  void testExecuteTerminate() {
    tc.handleCallActivity().executeTestCase(new TC_SubProcessTerminateEnd(), it -> {
      it.handleServiceTask().complete();
    });

    tc.createExecutor(client, processTestContext)
        .withAdditionalVersionedClasspathResource("callActivitySubProcess.bpmn", "v1")
        .withVariable("end", "terminate")
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  void testExecuteEscalation() {
    tcEscalation.handleCallActivity().executeTestCase(new TC_SubProcessEscalationEnd(), it -> {
      it.handleServiceTask().complete();
    });

    tcEscalation.createExecutor(client, processTestContext)
        .withAdditionalVersionedClasspathResource("callActivitySubProcess.bpmn", "v1")
        .withVariable("end", "escalation")
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  void testExecuteTimer() {
    tcTimer.handleCallActivity().executeTestCase(new TC_SubProcessWait(), null);

    tcTimer.handleTimerBoundaryEvent().execute(() -> processTestContext.increaseTime(Duration.ofHours(1)));

    tcTimer.createExecutor(client, processTestContext)
        .withAdditionalVersionedClasspathResource("callActivitySubProcess.bpmn", "v1")
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  void testExecuteTestCaseWithMessageStart() {
    assertThrows(IllegalArgumentException.class, () -> {
      tc.handleCallActivity().executeTestCase(new TC_SubProcessMessageStart(), null);
    });
  }

  @Test
  void testExecuteTestCaseWithSignalStart() {
    assertThrows(IllegalArgumentException.class, () -> {
      tc.handleCallActivity().executeTestCase(new TC_SubProcessSignalStart(), null);
    });
  }

  @Test
  void testExecuteTestCaseWithTimerStart() {
    assertThrows(IllegalArgumentException.class, () -> {
      tc.handleCallActivity().executeTestCase(new TC_SubProcessTimerStart(), null);
    });
  }

  @Test
  void testExecuteTestCaseWithNonProcessStart() {
    assertThrows(IllegalArgumentException.class, () -> {
      tc.handleCallActivity().executeTestCase(new TC_SubProcessServiceTask(), null);
    });
  }
}
