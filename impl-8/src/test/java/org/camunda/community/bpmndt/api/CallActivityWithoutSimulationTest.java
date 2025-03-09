package org.camunda.community.bpmndt.api;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.community.bpmndt.api.TestCaseInstanceElement.CallActivityElement;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class CallActivityWithoutSimulationTest {

  @RegisterExtension
  TestCase tc = new TestCase();
  @RegisterExtension
  TestCaseError tcError = new TestCaseError();
  @RegisterExtension
  TestCaseEscalation tcEscalation = new TestCaseEscalation();
  @RegisterExtension
  TestCaseTimer tcTimer = new TestCaseTimer();

  ZeebeTestEngine engine;

  private CallActivityHandler handler;

  @BeforeEach
  void setUp() {
    var element = new CallActivityElement();
    element.id = "callActivity";

    handler = new CallActivityHandler(element);
  }

  @Test
  void testExecute() {
    handler.verifyOutput(piAssert -> {
      piAssert.hasVariableWithValue("subProcessResult", "value");
    }).executeTestCase(new SubProcess_startEvent__endEvent(), it -> {
      it.handleServiceTask().withVariable("subProcessResult", "value").complete();
    });

    tc.createExecutor(engine).customize(this::customize).execute();
  }

  @Test
  void testExecuteError() {
    handler.executeTestCase(new SubProcess_startEvent__errorEndEvent(), it -> {
      it.handleServiceTask().complete();
    });

    tcError.createExecutor(engine).customize(this::customize).withVariable("end", "error").execute();
  }

  @Test
  void testExecuteEscalation() {
    handler.executeTestCase(new SubProcess_startEvent__escalationEndEvent(), it -> {
      it.handleServiceTask().complete();
    });

    tcEscalation.createExecutor(engine).customize(this::customize).withVariable("end", "escalation").execute();
  }

  @Test
  void testExecuteSignal() {
    handler.executeTestCase(new SubProcess_startEvent__signalEndEvent(), it -> {
      it.handleServiceTask().complete();
    });

    tc.createExecutor(engine).customize(this::customize).withVariable("end", "signal").execute();
  }

  @Test
  void testExecuteTerminate() {
    handler.executeTestCase(new SubProcess_startEvent__terminateEndEvent(), it -> {
      it.handleServiceTask().complete();
    });

    tc.createExecutor(engine).customize(this::customize).withVariable("end", "terminate").execute();
  }

  @Test
  void testExecuteTimer() {
    handler.waitForBoundaryEvent();

    handler.executeTestCase(new SubProcessWait(), null);

    tcTimer.createExecutor(engine).customize(this::customize).execute();
  }

  @Test
  void testExecuteTestCaseWithSimulatedVariable() {
    assertThrows(IllegalStateException.class, () -> {
      handler.simulateVariable("x", "y");
      handler.executeTestCase(new SubProcess_serviceTask__endEvent(), it -> {
        it.handleServiceTask().complete();
      });
    });
  }

  @Test
  void testExecuteTestCaseWithSimulatedVariables() {
    assertThrows(IllegalStateException.class, () -> {
      handler.simulateVariables("");
      handler.executeTestCase(new SubProcess_serviceTask__endEvent(), it -> {
        it.handleServiceTask().complete();
      });
    });
  }

  @Test
  void testExecuteTestCaseWithMessageStart() {
    assertThrows(IllegalArgumentException.class, () -> handler.executeTestCase(new SubProcess_messageStartEvent__endEvent(), it -> {
      it.handleServiceTask().complete();
    }));
  }

  @Test
  void testExecuteTestCaseWithSignalStart() {
    assertThrows(IllegalArgumentException.class, () -> handler.executeTestCase(new SubProcess_signalStartEvent__endEvent(), it -> {
      it.handleServiceTask().complete();
    }));
  }

  @Test
  void testExecuteTestCaseWithTimerStart() {
    assertThrows(IllegalArgumentException.class, () -> handler.executeTestCase(new SubProcess_timerStartEvent__endEvent(), it -> {
      it.handleServiceTask().complete();
    }));
  }

  @Test
  void testExecuteTestCaseWithNonProcessStart() {
    assertThrows(IllegalArgumentException.class, () -> handler.executeTestCase(new SubProcess_serviceTask__endEvent(), it -> {
      it.handleServiceTask().complete();
    }));
  }

  private void customize(TestCaseExecutor executor) {
    var resourceName = "callActivitySubProcess.bpmn";

    String resource;
    try {
      resource = Files.readString(TestPaths.advanced(resourceName));
    } catch (IOException e) {
      throw new RuntimeException("Failed to read resource", e);
    }

    executor.withAdditionalVersionedResource("callActivitySubProcess.bpmn", resource, "v1");
    executor.withVariable("end", "none");
    executor.verify(ProcessInstanceAssert::isCompleted);
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "startEvent");
      instance.isWaitingAt(processInstanceKey, "callActivity");
      instance.apply(processInstanceKey, handler);
      instance.hasPassed(processInstanceKey, "callActivity");
      instance.hasPassed(processInstanceKey, "endEvent");
    }

    @Override
    public String getBpmnProcessId() {
      return "callActivityWithoutSimulation";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.advanced("callActivityWithoutSimulation.bpmn"));
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

  private class TestCaseError extends TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, getStart());
      instance.isWaitingAt(processInstanceKey, "callActivity");
      instance.apply(processInstanceKey, handler);
      instance.hasTerminated(processInstanceKey, "callActivity");
      instance.hasPassed(processInstanceKey, "errorBoundaryEvent");
      instance.isActivating(processInstanceKey, getEnd());
    }

    @Override
    public String getEnd() {
      return "errorEnd";
    }
  }

  private class TestCaseEscalation extends TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, getStart());
      instance.isWaitingAt(processInstanceKey, "callActivity");
      instance.apply(processInstanceKey, handler);
      instance.hasTerminated(processInstanceKey, "callActivity");
      instance.hasPassed(processInstanceKey, "escalationBoundaryEvent");
      instance.hasPassed(processInstanceKey, getEnd());
    }

    @Override
    public String getEnd() {
      return "escalationEnd";
    }
  }

  private class TestCaseTimer extends TestCase {

    private TimerEventHandler timerBoundaryEvent;

    @Override
    protected void beforeEach() {
      timerBoundaryEvent = new TimerEventHandler("timerBoundaryEvent");
    }

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, getStart());
      instance.isWaitingAt(processInstanceKey, "callActivity");
      instance.apply(processInstanceKey, handler);
      instance.apply(processInstanceKey, timerBoundaryEvent);
      instance.hasTerminated(processInstanceKey, "callActivity");
      instance.hasPassed(processInstanceKey, "timerBoundaryEvent");
      instance.hasPassed(processInstanceKey, getEnd());
    }

    @Override
    public String getEnd() {
      return "timerEnd";
    }
  }

  private static class SubProcess_startEvent__endEvent extends AbstractJUnit5TestCase {

    private JobHandler serviceTask;

    @Override
    protected void beforeEach() {
      serviceTask = new JobHandler("serviceTask");
    }

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, getStart());
      instance.hasPassed(processInstanceKey, "join");
      instance.isWaitingAt(processInstanceKey, "serviceTask");
      instance.apply(processInstanceKey, serviceTask);
      instance.hasPassed(processInstanceKey, "serviceTask");
      instance.hasPassed(processInstanceKey, "fork");
      instance.hasPassed(processInstanceKey, getEnd());
    }

    @Override
    public String getBpmnProcessId() {
      return "callActivitySubProcess";
    }

    @Override
    public String getStart() {
      return "startEvent";
    }

    @Override
    public String getEnd() {
      return "endEvent";
    }

    public JobHandler handleServiceTask() {
      return serviceTask;
    }
  }

  private static class SubProcess_messageStartEvent__endEvent extends SubProcess_startEvent__endEvent {

    @Override
    public String getStart() {
      return "messageStartEvent";
    }

    @Override
    protected boolean isMessageStart() {
      return true;
    }
  }

  private static class SubProcess_signalStartEvent__endEvent extends SubProcess_startEvent__endEvent {

    @Override
    public String getStart() {
      return "signalStartEvent";
    }

    @Override
    protected boolean isSignalStart() {
      return true;
    }
  }

  private static class SubProcess_timerStartEvent__endEvent extends SubProcess_startEvent__endEvent {

    @Override
    public String getStart() {
      return "timerStartEvent";
    }

    @Override
    protected boolean isTimerStart() {
      return true;
    }
  }

  private static class SubProcess_serviceTask__endEvent extends SubProcess_startEvent__endEvent {

    @Override
    public String getStart() {
      return "serviceTask";
    }

    @Override
    protected boolean isProcessStart() {
      return false;
    }
  }

  private static class SubProcess_startEvent__errorEndEvent extends SubProcess_startEvent__endEvent {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, getStart());
      instance.hasPassed(processInstanceKey, "join");
      instance.isWaitingAt(processInstanceKey, "serviceTask");
      instance.apply(processInstanceKey, handleServiceTask());
      instance.hasPassed(processInstanceKey, "serviceTask");
      instance.hasPassed(processInstanceKey, "fork");
      instance.isActivating(processInstanceKey, getEnd());
    }

    @Override
    public String getEnd() {
      return "errorEndEvent";
    }
  }

  private static class SubProcess_startEvent__escalationEndEvent extends SubProcess_startEvent__endEvent {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, getStart());
      instance.hasPassed(processInstanceKey, "join");
      instance.isWaitingAt(processInstanceKey, "serviceTask");
      instance.apply(processInstanceKey, handleServiceTask());
      instance.hasPassed(processInstanceKey, "serviceTask");
      instance.hasPassed(processInstanceKey, "fork");
      instance.isActivating(processInstanceKey, getEnd());
    }

    @Override
    public String getEnd() {
      return "escalationEndEvent";
    }
  }

  private static class SubProcess_startEvent__signalEndEvent extends SubProcess_startEvent__endEvent {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, getStart());
      instance.hasPassed(processInstanceKey, "join");
      instance.isWaitingAt(processInstanceKey, "serviceTask");
      instance.apply(processInstanceKey, handleServiceTask());
      instance.hasPassed(processInstanceKey, "serviceTask");
      instance.hasPassed(processInstanceKey, "fork");
      instance.hasPassed(processInstanceKey, getEnd());
    }

    @Override
    public String getEnd() {
      return "signalEndEvent";
    }
  }

  private static class SubProcess_startEvent__terminateEndEvent extends SubProcess_startEvent__endEvent {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, getStart());
      instance.hasPassed(processInstanceKey, "join");
      instance.isWaitingAt(processInstanceKey, "serviceTask");
      instance.apply(processInstanceKey, handleServiceTask());
      instance.hasPassed(processInstanceKey, "serviceTask");
      instance.hasPassed(processInstanceKey, "fork");
      instance.hasPassed(processInstanceKey, getEnd());
    }

    @Override
    public String getEnd() {
      return "terminateEndEvent";
    }
  }

  private static class SubProcessWait extends SubProcess_startEvent__endEvent {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, getStart());
      instance.hasPassed(processInstanceKey, "join");
      instance.isWaitingAt(processInstanceKey, "serviceTask");
    }

    @Override
    public String getEnd() {
      return "join";
    }
  }
}
