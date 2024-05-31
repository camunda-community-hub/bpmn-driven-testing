package org.camunda.community.bpmndt.api;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class MultiInstanceScopeSequentialTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  private MultiInstanceScopeHandler<?> handler;

  @BeforeEach
  public void setUp() {
    handler = new Handler(tc.instance, "multiInstanceScope");
  }

  @Test
  public void testExecute() {
    tc.createExecutor()
        .withBean("serviceTask", new ServiceTask())
        .withBean("callActivityMapping", new CallActivityMapping())
        .execute();
  }

  @Test
  public void testVerify() {
    handler.verifyLoopCount(3).verifySequential();

    tc.createExecutor()
        .withBean("serviceTask", new ServiceTask())
        .withBean("callActivityMapping", new CallActivityMapping())
        .execute();
  }

  private class TestCase extends AbstractJUnit5TestCase<TestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi).isNotNull();

      instance.apply(handler);

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent", "multiInstanceScope#multiInstanceBody", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.advancedMultiInstance("scopeSequential.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "scopeSequential";
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

  private static class Handler extends MultiInstanceScopeHandler<Handler> {

    private final Map<Integer, UserTaskHandler> userTaskHandlers;
    private final Map<Integer, EventHandler> messageCatchEventHandlers;
    private final Map<Integer, JobHandler> serviceTaskHandlersBefore;
    private final Map<Integer, JobHandler> serviceTaskHandlersAfter;
    private final Map<Integer, JobHandler> callActivityHandlersBefore;
    private final Map<Integer, CallActivityHandler> callActivityHandlers;

    public Handler(TestCaseInstance instance, String activityId) {
      super(instance, activityId);

      userTaskHandlers = new HashMap<>();
      messageCatchEventHandlers = new HashMap<>();
      serviceTaskHandlersBefore = new HashMap<>();
      serviceTaskHandlersAfter = new HashMap<>();
      callActivityHandlersBefore = new HashMap<>();
      callActivityHandlers = new HashMap<>();
    }

    @Override
    protected boolean apply(ProcessInstance pi, int loopIndex) {
      // callActivity: callActivity
      registerCallActivityHandler("callActivity", getCallActivityHandler(loopIndex));

      // startEvent: subProcessStartEvent
      assertThat(pi).hasPassed("subProcessStartEvent");

      // userTask: userTask
      assertThat(pi).isWaitingAt("userTask");
      instance.apply(getUserTaskHandler(loopIndex));
      assertThat(pi).hasPassed("userTask");

      // intermediateCatchEvent: messageCatchEvent
      assertThat(pi).isWaitingAt("messageCatchEvent");
      instance.apply(getMessageCatchEventHandler(loopIndex));
      assertThat(pi).hasPassed("messageCatchEvent");

      // serviceTask: serviceTask
      assertThat(pi).isWaitingAt("serviceTask");
      instance.apply(getServiceTaskHandlerBefore(loopIndex));
      assertThat(pi).isWaitingAt("serviceTask");
      instance.apply(getServiceTaskHandlerAfter(loopIndex));
      assertThat(pi).hasPassed("serviceTask");

      // callActivity: callActivity
      assertThat(pi).isWaitingAt("callActivity");
      instance.apply(getCallActivityHandlerBefore(loopIndex));
      assertThat(pi).hasPassed("callActivity");

      // endEvent: subProcessEndEvent
      assertThat(pi).hasPassed("subProcessEndEvent");

      return true;
    }

    protected UserTaskHandler createUserTaskHandler(int loopIndex) {
      return new UserTaskHandler(getProcessEngine(), "userTask");
    }

    protected EventHandler createMessageCatchEventHandler(int loopIndex) {
      return new EventHandler(getProcessEngine(), "messageCatchEvent", "advancedMessage");
    }

    protected JobHandler createServiceTaskHandlerBefore(int loopIndex) {
      return new JobHandler(getProcessEngine(), "serviceTask");
    }

    protected JobHandler createServiceTaskHandlerAfter(int loopIndex) {
      return new JobHandler(getProcessEngine(), "serviceTask");
    }

    protected JobHandler createCallActivityHandlerBefore(int loopIndex) {
      return new JobHandler(getProcessEngine(), "callActivity");
    }

    protected CallActivityHandler createCallActivityHandler(int loopIndex) {
      return new CallActivityHandler(instance, "callActivity");
    }

    protected UserTaskHandler getUserTaskHandler(int loopIndex) {
      return userTaskHandlers.getOrDefault(loopIndex, handleUserTask());
    }

    protected EventHandler getMessageCatchEventHandler(int loopIndex) {
      return messageCatchEventHandlers.getOrDefault(loopIndex, handleMessageCatchEvent());
    }

    protected JobHandler getServiceTaskHandlerBefore(int loopIndex) {
      return serviceTaskHandlersBefore.getOrDefault(loopIndex, handleServiceTaskBefore());
    }

    protected JobHandler getServiceTaskHandlerAfter(int loopIndex) {
      return serviceTaskHandlersAfter.getOrDefault(loopIndex, handleServiceTaskAfter());
    }

    protected JobHandler getCallActivityHandlerBefore(int loopIndex) {
      return callActivityHandlersBefore.getOrDefault(loopIndex, handleCallActivityBefore());
    }

    protected CallActivityHandler getCallActivityHandler(int loopIndex) {
      return callActivityHandlers.getOrDefault(loopIndex, handleCallActivity());
    }

    public UserTaskHandler handleUserTask() {
      return handleUserTask(-1);
    }

    public UserTaskHandler handleUserTask(int loopIndex) {
      return userTaskHandlers.computeIfAbsent(loopIndex, this::createUserTaskHandler);
    }

    public EventHandler handleMessageCatchEvent() {
      return handleMessageCatchEvent(-1);
    }

    public EventHandler handleMessageCatchEvent(int loopIndex) {
      return messageCatchEventHandlers.computeIfAbsent(loopIndex, this::createMessageCatchEventHandler);
    }

    public JobHandler handleServiceTaskBefore() {
      return handleServiceTaskBefore(-1);
    }

    public JobHandler handleServiceTaskBefore(int loopIndex) {
      return serviceTaskHandlersBefore.computeIfAbsent(loopIndex, this::createServiceTaskHandlerBefore);
    }

    public JobHandler handleServiceTaskAfter() {
      return handleServiceTaskAfter(-1);
    }

    public JobHandler handleServiceTaskAfter(int loopIndex) {
      return serviceTaskHandlersAfter.computeIfAbsent(loopIndex, this::createServiceTaskHandlerAfter);
    }

    public JobHandler handleCallActivityBefore() {
      return handleCallActivityBefore(-1);
    }

    public JobHandler handleCallActivityBefore(int loopIndex) {
      return callActivityHandlersBefore.computeIfAbsent(loopIndex, this::createCallActivityHandlerBefore);
    }

    public CallActivityHandler handleCallActivity() {
      return handleCallActivity(-1);
    }

    public CallActivityHandler handleCallActivity(int loopIndex) {
      return callActivityHandlers.computeIfAbsent(loopIndex, this::createCallActivityHandler);
    }
  }

  private static class ServiceTask implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
      // nothing to do here
    }
  }

  private static class CallActivityMapping implements DelegateVariableMapping {

    @Override
    public void mapInputVariables(DelegateExecution superExecution, VariableMap subVariables) {
      // nothing to do here
    }

    @Override
    public void mapOutputVariables(DelegateExecution superExecution, VariableScope subInstance) {
      // nothing to do here
    }
  }
}
