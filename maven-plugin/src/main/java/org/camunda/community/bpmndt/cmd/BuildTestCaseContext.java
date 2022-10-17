package org.camunda.community.bpmndt.cmd;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.instance.BoundaryEvent;
import org.camunda.bpm.model.bpmn.instance.Error;
import org.camunda.bpm.model.bpmn.instance.Escalation;
import org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent;
import org.camunda.bpm.model.bpmn.instance.Message;
import org.camunda.bpm.model.bpmn.instance.ReceiveTask;
import org.camunda.bpm.model.bpmn.instance.Signal;
import org.camunda.community.bpmndt.BpmnEventSupport;
import org.camunda.community.bpmndt.BpmnSupport;
import org.camunda.community.bpmndt.GeneratorContext;
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
import org.camunda.community.bpmndt.strategy.MultiInstanceStrategy;
import org.camunda.community.bpmndt.strategy.UserTaskStrategy;

import com.squareup.javapoet.ClassName;

/**
 * Builds a new test case context, used for code generation.
 */
public class BuildTestCaseContext implements Function<TestCase, TestCaseContext> {

  private final GeneratorContext gCtx;
  private final BpmnSupport bpmnSupport;

  private final Set<String> testCaseNames;

  public BuildTestCaseContext(GeneratorContext gCtx, BpmnSupport bpmnSupport) {
    this.gCtx = gCtx;
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
    for (int i = 0; i < flowNodeIds.size(); i++) {
      String flowNodeId = flowNodeIds.get(i);

      if (!bpmnSupport.has(flowNodeId)) {
        ctx.addInvalidFlowNodeId(flowNodeId);
        continue;
      }

      TestCaseActivity activity = new TestCaseActivity(bpmnSupport.get(flowNodeId), bpmnSupport.getMultiInstance(flowNodeId));

      if (bpmnSupport.isCallActivity(flowNodeId)) {
        activity.setType(TestCaseActivityType.CALL_ACTIVITY);
      } else if (bpmnSupport.isEventBasedGateway(flowNodeId)) {
        activity.setType(TestCaseActivityType.EVENT_BASED_GATEWAY);
      } else if (bpmnSupport.isExternalTask(flowNodeId)) {
        activity.setType(TestCaseActivityType.EXTERNAL_TASK);
        activity.setTopicName(bpmnSupport.getTopicName(flowNodeId));
      } else if (bpmnSupport.isUserTask(flowNodeId)) {
        activity.setType(TestCaseActivityType.USER_TASK);
      } else if (bpmnSupport.isIntermediateCatchEvent(flowNodeId)) {
        handleIntermediateCatchEvent(activity, flowNodeId);
      } else if (bpmnSupport.isBoundaryEvent(flowNodeId)) {
        handleBoundaryEvent(activity, flowNodeId);
      } else if (bpmnSupport.isReceiveTask(flowNodeId)) {
        // handle receive task as message catch event
        Message message = activity.as(ReceiveTask.class).getMessage();

        activity.setType(TestCaseActivityType.MESSAGE_CATCH);
        activity.setEventName(message != null ? message.getName() : null);
      }

      DefaultStrategy strategy = getStrategy(activity);
      if (strategy != null) {
        strategy.setActivity(activity);
        activity.setStrategy(strategy);
      }
      if (strategy != null && activity.isMultiInstance()) {
        handleMultiInstance(ctx, activity);
      }

      if (i == flowNodeIds.size() - 1) {
        activity.setProcessEnd(bpmnSupport.isProcessEnd(flowNodeId));
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

    BpmnEventSupport eventSupport = new BpmnEventSupport(event);

    if (eventSupport.isConditional()) {
      activity.setType(TestCaseActivityType.CONDITIONAL_BOUNDARY);
    } else if (eventSupport.isError()) {
      Error error = eventSupport.getError();

      activity.setType(TestCaseActivityType.ERROR_BOUNDARY);
      activity.setEventCode(error != null ? error.getErrorCode() : null);
    } else if (eventSupport.isEscalation()) {
      Escalation escalation = eventSupport.getEscalation();

      activity.setType(TestCaseActivityType.ESCALATION_BOUNDARY);
      activity.setEventCode(escalation != null ? escalation.getEscalationCode() : null);
    } else if (eventSupport.isMessage()) {
      Message message = eventSupport.getMessage();

      activity.setType(TestCaseActivityType.MESSAGE_BOUNDARY);
      activity.setEventName(message != null ? message.getName() : null);
    } else if (eventSupport.isSignal()) {
      Signal signal = eventSupport.getSignal();

      activity.setType(TestCaseActivityType.SIGNAL_BOUNDARY);
      activity.setEventName(signal != null ? signal.getName() : null);
    } else if (eventSupport.isTimer()) {
      activity.setType(TestCaseActivityType.TIMER_BOUNDARY);
    }
  }

  protected void handleIntermediateCatchEvent(TestCaseActivity activity, String flowNodeId) {
    IntermediateCatchEvent event = activity.as(IntermediateCatchEvent.class);

    BpmnEventSupport eventSupport = new BpmnEventSupport(event);

    if (eventSupport.isConditional()) {
      activity.setType(TestCaseActivityType.CONDITIONAL_CATCH);
    } else if (eventSupport.isMessage()) {
      Message message = eventSupport.getMessage();

      activity.setType(TestCaseActivityType.MESSAGE_CATCH);
      activity.setEventName(message != null ? message.getName() : null);
    } else if (eventSupport.isSignal()) {
      Signal signal = eventSupport.getSignal();

      activity.setType(TestCaseActivityType.SIGNAL_CATCH);
      activity.setEventName(signal != null ? signal.getName() : null);
    } else if (eventSupport.isTimer()) {
      activity.setType(TestCaseActivityType.TIMER_CATCH);
    }
  }

  protected void handleMultiInstance(TestCaseContext ctx, TestCaseActivity activity) {
    String packageName = String.format("%s.%s", gCtx.getPackageName(), ctx.getPackageName());
    String name = String.format("%sHandler", StringUtils.capitalize(activity.getLiteral()));

    MultiInstanceStrategy multiInstanceStrategy = new MultiInstanceStrategy(activity.getStrategy(), ClassName.get(packageName, name));
    multiInstanceStrategy.setActivity(activity);
    activity.setStrategy(multiInstanceStrategy);
  }
}
