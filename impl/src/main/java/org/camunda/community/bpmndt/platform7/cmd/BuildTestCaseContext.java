package org.camunda.community.bpmndt.platform7.cmd;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

import org.camunda.community.bpmndt.model.platform7.TestCase;
import org.camunda.community.bpmndt.model.platform7.TestCaseActivity;
import org.camunda.community.bpmndt.model.platform7.TestCaseActivityScope;
import org.camunda.community.bpmndt.platform7.Generator;
import org.camunda.community.bpmndt.platform7.GeneratorContext;
import org.camunda.community.bpmndt.platform7.GeneratorStrategy;
import org.camunda.community.bpmndt.platform7.TestCaseContext;
import org.camunda.community.bpmndt.platform7.strategy.BoundaryEventStrategy;
import org.camunda.community.bpmndt.platform7.strategy.BoundaryJobStrategy;
import org.camunda.community.bpmndt.platform7.strategy.CallActivityStrategy;
import org.camunda.community.bpmndt.platform7.strategy.DefaultStrategy;
import org.camunda.community.bpmndt.platform7.strategy.EventStrategy;
import org.camunda.community.bpmndt.platform7.strategy.ExternalTaskStrategy;
import org.camunda.community.bpmndt.platform7.strategy.JobStrategy;
import org.camunda.community.bpmndt.platform7.strategy.MultiInstanceScopeStrategy;
import org.camunda.community.bpmndt.platform7.strategy.MultiInstanceStrategy;
import org.camunda.community.bpmndt.platform7.strategy.UserTaskStrategy;

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
      name = Generator.toLiteral(testCase.getName());
    } else {
      String a = Generator.toLiteral(testCase.getStartActivity().getId());
      String b = Generator.toLiteral(testCase.getEndActivity().getId());

      name = String.format("%s__%s", a, b);
    }

    String packageName = Generator.toJavaLiteral(testCase.getProcessId().toLowerCase(Locale.ENGLISH));

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

    int nestingLevel = testCase.getStartActivity().getNestingLevel();

    // add strategies
    for (TestCaseActivity activity : testCase.getActivities()) {
      GeneratorStrategy strategy = createStrategy(activity);
      if (activity.isMultiInstance()) {
        strategy = new MultiInstanceStrategy(strategy, ctx);
        ctx.addMultiInstanceActivity(activity);
      }

      ctx.addStrategy(strategy);

      if (activity.getNestingLevel() == nestingLevel) {
        continue;
      }

      if (activity.getNestingLevel() > nestingLevel && activity.hasMultiInstanceParent()) {
        TestCaseActivityScope scope = activity.getParent();
        ctx.addMultiInstanceScope(scope);
        ctx.addStrategy(new MultiInstanceScopeStrategy(scope, ctx));
      }

      nestingLevel = activity.getNestingLevel();
    }

    return ctx;
  }

  private GeneratorStrategy createStrategy(TestCaseActivity activity) {
    switch (activity.getType()) {
      case CALL_ACTIVITY:
        return new CallActivityStrategy(activity);
      case CONDITIONAL_BOUNDARY:
        return new BoundaryEventStrategy(activity);
      case CONDITIONAL_CATCH:
        return new EventStrategy(activity);
      case EXTERNAL_TASK:
        return new ExternalTaskStrategy(activity);
      case MESSAGE_BOUNDARY:
        return new BoundaryEventStrategy(activity);
      case MESSAGE_CATCH:
        return new EventStrategy(activity);
      case SIGNAL_BOUNDARY:
        return new BoundaryEventStrategy(activity);
      case SIGNAL_CATCH:
        return new EventStrategy(activity);
      case TIMER_BOUNDARY:
        return new BoundaryJobStrategy(activity);
      case TIMER_CATCH:
        return new JobStrategy(activity);
      case USER_TASK:
        return new UserTaskStrategy(activity);
      default:
        return new DefaultStrategy(activity);
    }
  }
}
