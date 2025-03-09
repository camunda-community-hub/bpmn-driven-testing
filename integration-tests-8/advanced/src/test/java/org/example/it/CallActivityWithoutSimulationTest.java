package org.example.it;

import static org.junit.jupiter.api.Assertions.assertThrows;

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
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class CallActivityWithoutSimulationTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();
  @RegisterExtension
  TC_startEvent__errorEnd tcError = new TC_startEvent__errorEnd();
  @RegisterExtension
  TC_startEvent__escalationEnd tcEscalation = new TC_startEvent__escalationEnd();
  @RegisterExtension
  TC_startEvent__timerEnd tcTimer = new TC_startEvent__timerEnd();

  ZeebeTestEngine engine;

  @Test
  void testExecute() {
    tc.handleCallActivity().verifyOutput(piAssert -> {
      piAssert.hasVariableWithValue("subProcessResult", "value");
    }).executeTestCase(new TC_SubProcessEnd(), it -> {
      it.handleServiceTask().withVariable("subProcessResult", "value").complete();
    });

    tc.createExecutor(engine)
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

    tcError.createExecutor(engine)
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

    tc.createExecutor(engine)
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

    tc.createExecutor(engine)
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

    tcEscalation.createExecutor(engine)
        .withAdditionalVersionedClasspathResource("callActivitySubProcess.bpmn", "v1")
        .withVariable("end", "escalation")
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  void testExecuteTimer() {
    tcTimer.handleCallActivity().executeTestCase(new TC_SubProcessWait(), null);

    tcTimer.createExecutor(engine)
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
