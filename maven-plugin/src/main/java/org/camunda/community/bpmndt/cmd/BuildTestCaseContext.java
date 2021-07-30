package org.camunda.community.bpmndt.cmd;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.camunda.bpm.model.bpmn.instance.EventDefinition;
import org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent;
import org.camunda.bpm.model.bpmn.instance.Message;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.camunda.bpm.model.bpmn.instance.Signal;
import org.camunda.bpm.model.bpmn.instance.SignalEventDefinition;
import org.camunda.bpm.model.bpmn.instance.TimerEventDefinition;
import org.camunda.community.bpmndt.BpmnSupport;
import org.camunda.community.bpmndt.TestCaseActivity;
import org.camunda.community.bpmndt.TestCaseActivityType;
import org.camunda.community.bpmndt.TestCaseContext;
import org.camunda.community.bpmndt.model.TestCase;

/**
 * Builds a new test case context, used for code generation.
 */
public class BuildTestCaseContext implements Function<TestCase, TestCaseContext> {

  private final BpmnSupport bpmnSupport;

  private final Set<String> testCaseNames;

  public BuildTestCaseContext(BpmnSupport bpmnSupport) {
    this.bpmnSupport = bpmnSupport;

    testCaseNames = new HashSet<>();
  }

  @Override
  public TestCaseContext apply(TestCase testCase) {
    TestCaseContext ctx = new TestCaseContext(bpmnSupport.getFile(), bpmnSupport.getProcessId(), testCase);

    if (testCaseNames.contains(ctx.getName())) {
      ctx.setDuplicateName(true);
      return ctx;
    } else {
      testCaseNames.add(ctx.getName());
    }

    List<String> flowNodeIds = testCase.getPath().getFlowNodeIds();

    for (String flowNodeId : flowNodeIds) {
      if (!bpmnSupport.has(flowNodeId)) {
        ctx.addInvalidFlowNodeId(flowNodeId);
        continue;
      }

      TestCaseActivity activity = new TestCaseActivity(bpmnSupport.get(flowNodeId));

      if (bpmnSupport.isCallActivity(flowNodeId)) {
        activity.setType(TestCaseActivityType.CALL_ACTIVITY);
      }
      if (bpmnSupport.isExternalTask(flowNodeId)) {
        activity.setType(TestCaseActivityType.EXTERNAL_TASK);
      }
      if (bpmnSupport.isIntermediateCatchEvent(flowNodeId)) {
        handleIntermediateCatchEvent(activity, flowNodeId);
      }
      if (bpmnSupport.isUserTask(flowNodeId)) {
        activity.setType(TestCaseActivityType.USER_TASK);
      }

      ctx.addActivity(activity);
    }

    return ctx;
  }

  protected void handleIntermediateCatchEvent(TestCaseActivity activity, String flowNodeId) {
    IntermediateCatchEvent event = activity.as(IntermediateCatchEvent.class);

    Optional<EventDefinition> eventDefinition = event.getEventDefinitions().stream().findFirst();
    if (!eventDefinition.isPresent()) {
      return;
    } else if (eventDefinition.get() instanceof MessageEventDefinition) {
      Message message = ((MessageEventDefinition) eventDefinition.get()).getMessage();

      activity.setType(TestCaseActivityType.MESSAGE_CATCH_EVENT);
      activity.setEventName(message != null ? message.getName() : null);
    } else if (eventDefinition.get() instanceof SignalEventDefinition) {
      Signal signal = ((SignalEventDefinition) eventDefinition.get()).getSignal();

      activity.setType(TestCaseActivityType.SIGNAL_CATCH_EVENT);
      activity.setEventName(signal != null ? signal.getName() : null);
    } else if (eventDefinition.get() instanceof TimerEventDefinition) {
      activity.setType(TestCaseActivityType.TIMER_CATCH_EVENT);
    }
  }
}
