package org.camunda.community.bpmndt.cmd;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_CONDITIONAL_EVENT_DEFINITION;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_ERROR_EVENT_DEFINITION;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_ESCALATION_EVENT_DEFINITION;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_MESSAGE_EVENT_DEFINITION;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_SIGNAL_EVENT_DEFINITION;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_TIMER_EVENT_DEFINITION;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.camunda.bpm.model.bpmn.instance.BoundaryEvent;
import org.camunda.bpm.model.bpmn.instance.Error;
import org.camunda.bpm.model.bpmn.instance.ErrorEventDefinition;
import org.camunda.bpm.model.bpmn.instance.Escalation;
import org.camunda.bpm.model.bpmn.instance.EscalationEventDefinition;
import org.camunda.bpm.model.bpmn.instance.EventDefinition;
import org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent;
import org.camunda.bpm.model.bpmn.instance.Message;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.camunda.bpm.model.bpmn.instance.ReceiveTask;
import org.camunda.bpm.model.bpmn.instance.Signal;
import org.camunda.bpm.model.bpmn.instance.SignalEventDefinition;
import org.camunda.community.bpmndt.BpmnSupport;
import org.camunda.community.bpmndt.TestCaseActivity;
import org.camunda.community.bpmndt.TestCaseActivityType;
import org.camunda.community.bpmndt.TestCaseContext;
import org.camunda.community.bpmndt.model.TestCase;
import org.camunda.community.bpmndt.strategy.BoundaryEventStrategy;
import org.camunda.community.bpmndt.strategy.BoundaryJobStrategy;
import org.camunda.community.bpmndt.strategy.CallActivityStrategy;
import org.camunda.community.bpmndt.strategy.DefaultStrategy;
import org.camunda.community.bpmndt.strategy.EventStrategy;
import org.camunda.community.bpmndt.strategy.ExternalTaskStrategy;
import org.camunda.community.bpmndt.strategy.JobStrategy;
import org.camunda.community.bpmndt.strategy.UserTaskStrategy;

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
    TestCaseContext ctx = new TestCaseContext(bpmnSupport, testCase);

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
      } else if (bpmnSupport.isExternalTask(flowNodeId)) {
        activity.setType(TestCaseActivityType.EXTERNAL_TASK);
      } else if (bpmnSupport.isUserTask(flowNodeId)) {
        activity.setType(TestCaseActivityType.USER_TASK);
      } else if (bpmnSupport.isIntermediateCatchEvent(flowNodeId)) {
        handleIntermediateCatchEvent(activity, flowNodeId);
      } else if (bpmnSupport.isBoundaryEvent(flowNodeId)) {
        handleBoundaryEvent(activity, flowNodeId);
      } else if (bpmnSupport.isReceiveTask(flowNodeId)) {
        // handled like a message catch event
        Message message = activity.as(ReceiveTask.class).getMessage();

        activity.setType(TestCaseActivityType.MESSAGE_CATCH);
        activity.setEventName(message != null ? message.getName() : null);
      }

      DefaultStrategy strategy = getStrategy(activity);
      if (strategy != null) {
        strategy.setActivity(activity);
        activity.setStrategy(strategy);
      }

      ctx.addActivity(activity);
    }

    return ctx;
  }

  protected DefaultStrategy getStrategy(TestCaseActivity current) {
    switch (current.getType()) {
      case CALL_ACTIVITY:
        return new CallActivityStrategy();
      case CONDITIONAL_BOUNDARY:
        return new BoundaryEventStrategy();
      case CONDITIONAL_CATCH:
        return new EventStrategy();
      case EXTERNAL_TASK:
        return new ExternalTaskStrategy();
      case MESSAGE_BOUNDARY:
        return new BoundaryEventStrategy();
      case MESSAGE_CATCH:
        return new EventStrategy();
      case SIGNAL_BOUNDARY:
        return new BoundaryEventStrategy();
      case SIGNAL_CATCH:
        return new EventStrategy();
      case TIMER_BOUNDARY:
        return new BoundaryJobStrategy();
      case TIMER_CATCH:
        return new JobStrategy();
      case USER_TASK:
        return new UserTaskStrategy();
      default:
        return new DefaultStrategy();
    }
  }

  protected void handleBoundaryEvent(TestCaseActivity activity, String flowNodeId) {
    BoundaryEvent event = activity.as(BoundaryEvent.class);

    activity.setAttachedTo(event.getAttachedTo().getId());

    Optional<EventDefinition> eventDefinition = event.getEventDefinitions().stream().findFirst();
    if (is(eventDefinition, BPMN_ELEMENT_CONDITIONAL_EVENT_DEFINITION)) {
      activity.setType(TestCaseActivityType.CONDITIONAL_CATCH);
    } else if (is(eventDefinition, BPMN_ELEMENT_ERROR_EVENT_DEFINITION)) {
      Error error = ((ErrorEventDefinition) eventDefinition.get()).getError();

      activity.setType(TestCaseActivityType.ERROR_BOUNDARY);
      activity.setEventCode(error != null ? error.getErrorCode() : null);
    } else if (is(eventDefinition, BPMN_ELEMENT_ESCALATION_EVENT_DEFINITION)) {
      Escalation escalation = ((EscalationEventDefinition) eventDefinition.get()).getEscalation();

      activity.setType(TestCaseActivityType.ESCALATION_BOUNDARY);
      activity.setEventCode(escalation != null ? escalation.getEscalationCode() : null);
    } else if (is(eventDefinition, BPMN_ELEMENT_MESSAGE_EVENT_DEFINITION)) {
      Message message = ((MessageEventDefinition) eventDefinition.get()).getMessage();

      activity.setType(TestCaseActivityType.MESSAGE_BOUNDARY);
      activity.setEventName(message != null ? message.getName() : null);
    } else if (is(eventDefinition, BPMN_ELEMENT_SIGNAL_EVENT_DEFINITION)) {
      Signal signal = ((SignalEventDefinition) eventDefinition.get()).getSignal();

      activity.setType(TestCaseActivityType.SIGNAL_BOUNDARY);
      activity.setEventName(signal != null ? signal.getName() : null);
    } else if (is(eventDefinition, BPMN_ELEMENT_TIMER_EVENT_DEFINITION)) {
      activity.setType(TestCaseActivityType.TIMER_BOUNDARY);
    }
  }

  protected void handleIntermediateCatchEvent(TestCaseActivity activity, String flowNodeId) {
    IntermediateCatchEvent event = activity.as(IntermediateCatchEvent.class);

    Optional<EventDefinition> eventDefinition = event.getEventDefinitions().stream().findFirst();
    if (is(eventDefinition, BPMN_ELEMENT_CONDITIONAL_EVENT_DEFINITION)) {
      activity.setType(TestCaseActivityType.CONDITIONAL_CATCH);
    } else if (is(eventDefinition, BPMN_ELEMENT_MESSAGE_EVENT_DEFINITION)) {
      Message message = ((MessageEventDefinition) eventDefinition.get()).getMessage();

      activity.setType(TestCaseActivityType.MESSAGE_CATCH);
      activity.setEventName(message != null ? message.getName() : null);
    } else if (is(eventDefinition, BPMN_ELEMENT_SIGNAL_EVENT_DEFINITION)) {
      Signal signal = ((SignalEventDefinition) eventDefinition.get()).getSignal();

      activity.setType(TestCaseActivityType.SIGNAL_CATCH);
      activity.setEventName(signal != null ? signal.getName() : null);
    } else if (is(eventDefinition, BPMN_ELEMENT_TIMER_EVENT_DEFINITION)) {
      activity.setType(TestCaseActivityType.TIMER_CATCH);
    }
  }

  private boolean is(Optional<EventDefinition> eventDefinition, String typeName) {
    if (eventDefinition.isPresent()) {
      return eventDefinition.get().getElementType().getTypeName().equals(typeName);
    } else {
      return false;
    }
  }
}
