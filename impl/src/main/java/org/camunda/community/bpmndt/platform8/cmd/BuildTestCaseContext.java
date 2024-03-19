package org.camunda.community.bpmndt.platform8.cmd;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

import org.camunda.community.bpmndt.GeneratorContextBase;
import org.camunda.community.bpmndt.Literal;
import org.camunda.community.bpmndt.model.platform8.BpmnElement;
import org.camunda.community.bpmndt.model.platform8.TestCase;
import org.camunda.community.bpmndt.platform8.GeneratorStrategy;
import org.camunda.community.bpmndt.platform8.TestCaseContext;
import org.camunda.community.bpmndt.platform8.strategy.DefaultStrategy;
import org.camunda.community.bpmndt.platform8.strategy.JobStrategy;
import org.camunda.community.bpmndt.platform8.strategy.MessageEventStrategy;
import org.camunda.community.bpmndt.platform8.strategy.SignalEventStrategy;
import org.camunda.community.bpmndt.platform8.strategy.TimerEventStrategy;
import org.camunda.community.bpmndt.platform8.strategy.UserTaskStrategy;

/**
 * Builds a new test case context, used for code generation. An instance of this class needs to be reused for all test cases of one BPMN process.
 */
public class BuildTestCaseContext implements Function<TestCase, TestCaseContext> {

  private final GeneratorContextBase gCtx;
  private final Path bpmnFile;

  private final Set<String> testCaseNames;

  public BuildTestCaseContext(GeneratorContextBase gCtx, Path bpmnFile) {
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

    String packageName = Literal.toJavaLiteral(testCase.getProcessId().toLowerCase(Locale.ENGLISH));

    TestCaseContext ctx = new TestCaseContext();
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

    // add strategies
    testCase.getElements().stream().map(this::createStrategy).forEach(ctx::addStrategy);

    return ctx;
  }

  private GeneratorStrategy createStrategy(BpmnElement element) {
    switch (element.getType()) {
      case CALL_ACTIVITY:
        return null;
      case MESSAGE_BOUNDARY:
      case MESSAGE_CATCH:
        return new MessageEventStrategy(element);
      case SERVICE_TASK:
        return new JobStrategy(element);
      case SIGNAL_BOUNDARY:
      case SIGNAL_CATCH:
        return new SignalEventStrategy(element);
      case TIMER_BOUNDARY:
      case TIMER_CATCH:
        return new TimerEventStrategy(element);
      case USER_TASK:
        return new UserTaskStrategy(element);
      default:
        return new DefaultStrategy(element);
    }
  }
}
