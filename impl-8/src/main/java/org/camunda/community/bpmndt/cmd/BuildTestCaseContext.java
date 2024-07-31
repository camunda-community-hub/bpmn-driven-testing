package org.camunda.community.bpmndt.cmd;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

import org.camunda.community.bpmndt.GeneratorContext;
import org.camunda.community.bpmndt.GeneratorStrategy;
import org.camunda.community.bpmndt.Literal;
import org.camunda.community.bpmndt.TestCaseContext;
import org.camunda.community.bpmndt.model.BpmnElement;
import org.camunda.community.bpmndt.model.BpmnElementScope;
import org.camunda.community.bpmndt.model.TestCase;
import org.camunda.community.bpmndt.strategy.CallActivityStrategy;
import org.camunda.community.bpmndt.strategy.CustomMultiInstanceScopeStrategy;
import org.camunda.community.bpmndt.strategy.CustomMultiInstanceStrategy;
import org.camunda.community.bpmndt.strategy.DefaultStrategy;
import org.camunda.community.bpmndt.strategy.JobStrategy;
import org.camunda.community.bpmndt.strategy.MessageBoundaryEventStrategy;
import org.camunda.community.bpmndt.strategy.MessageEventStrategy;
import org.camunda.community.bpmndt.strategy.OutboundConnectorStrategy;
import org.camunda.community.bpmndt.strategy.ReceiveTaskStrategy;
import org.camunda.community.bpmndt.strategy.SignalBoundaryEventStrategy;
import org.camunda.community.bpmndt.strategy.SignalEventStrategy;
import org.camunda.community.bpmndt.strategy.TimerBoundaryEventStrategy;
import org.camunda.community.bpmndt.strategy.TimerEventStrategy;
import org.camunda.community.bpmndt.strategy.UserTaskStrategy;

/**
 * Builds a new test case context, used for code generation. An instance of this class needs to be reused for all test cases of one BPMN process.
 */
public class BuildTestCaseContext implements Function<TestCase, TestCaseContext> {

  private final GeneratorContext gCtx;
  private final Path bpmnFile;

  private final Set<String> testCaseNames;

  public BuildTestCaseContext(GeneratorContext gCtx, Path bpmnFile) {
    this.gCtx = gCtx;
    this.bpmnFile = bpmnFile;

    testCaseNames = new HashSet<>();
  }

  @Override
  public TestCaseContext apply(TestCase testCase) {
    // build test case name
    String name;
    if (testCase.getName() != null) {
      name = Literal.toLiteral(testCase.getName());
    } else {
      String a = Literal.toLiteral(testCase.getStartElement().getId());
      String b = Literal.toLiteral(testCase.getEndElement().getId());

      name = String.format("%s__%s", a, b);
    }

    var packageName = Literal.toJavaLiteral(testCase.getProcessId().toLowerCase(Locale.ENGLISH));

    var ctx = new TestCaseContext();
    ctx.setClassName(String.format("TC_%s", name));
    ctx.setName(name);
    ctx.setPackageName(String.format("%s.%s", gCtx.getPackageName(), packageName));
    ctx.setResourceName(gCtx.getMainResourcePath().relativize(bpmnFile).toString().replace('\\', '/'));
    ctx.setTestCase(testCase);

    if (testCaseNames.contains(name)) {
      ctx.setDuplicateName(true);
      return ctx;
    } else {
      testCaseNames.add(name);
    }

    int nestingLevel = testCase.getStartElement().getNestingLevel();
    BpmnElementScope scope = null;

    // add strategies
    for (BpmnElement element : testCase.getElements()) {
      if (element.getNestingLevel() < nestingLevel) {
        scope = null;
      }

      if (scope != null) {
        continue;
      }

      if (scope == null && element.getNestingLevel() > nestingLevel && element.hasMultiInstanceParent()) {
        scope = element.getParent();
        ctx.addMultiInstanceScope(scope);
        ctx.addStrategy(new CustomMultiInstanceScopeStrategy(scope));

        nestingLevel = element.getNestingLevel();
        continue;
      }

      var strategy = createStrategy(element);
      if (element.isMultiInstance()) {
        strategy = new CustomMultiInstanceStrategy(strategy);
        ctx.addMultiInstance(element);
      }

      ctx.addStrategy(strategy);

      nestingLevel = element.getNestingLevel();
    }

    return ctx;
  }

  private GeneratorStrategy createStrategy(BpmnElement element) {
    switch (element.getType()) {
      case CALL_ACTIVITY:
        return new CallActivityStrategy(element);
      case MESSAGE_BOUNDARY:
        return new MessageBoundaryEventStrategy(element);
      case MESSAGE_CATCH:
        return new MessageEventStrategy(element);
      case OUTBOUND_CONNECTOR:
        return new OutboundConnectorStrategy(element);
      case RECEIVE_TASK:
        return new ReceiveTaskStrategy(element);
      case SERVICE_TASK:
        return new JobStrategy(element);
      case SIGNAL_BOUNDARY:
        return new SignalBoundaryEventStrategy(element);
      case SIGNAL_CATCH:
        return new SignalEventStrategy(element);
      case TIMER_BOUNDARY:
        return new TimerBoundaryEventStrategy(element);
      case TIMER_CATCH:
        return new TimerEventStrategy(element);
      case USER_TASK:
        return new UserTaskStrategy(element);
      default:
        return new DefaultStrategy(element);
    }
  }
}
