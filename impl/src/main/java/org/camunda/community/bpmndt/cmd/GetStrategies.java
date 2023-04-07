package org.camunda.community.bpmndt.cmd;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

import org.camunda.community.bpmndt.GeneratorStrategy;
import org.camunda.community.bpmndt.TestCaseContext;
import org.camunda.community.bpmndt.model.TestCaseActivity;

/**
 * Function that provides the strategies of all activities, including scopes, that are on the same
 * nesting level during execution.
 */
public class GetStrategies implements BiFunction<TestCaseContext, List<TestCaseActivity>, List<GeneratorStrategy>> {

  private String scopeId;

  @Override
  public List<GeneratorStrategy> apply(TestCaseContext ctx, List<TestCaseActivity> activities) {
    List<GeneratorStrategy> strategies = new LinkedList<>();

    // get actual activities, including activities from sub scopes
    List<TestCaseActivity> actualActivities;
    if (ctx.getTestCase().getActivities().size() == activities.size()) {
      // test case
      actualActivities = activities;
    } else {
      // multi instance scope
      actualActivities = new LinkedList<>();

      int nestingLevel = activities.get(0).getNestingLevel();
      TestCaseActivity current = activities.get(0);
      do {
        actualActivities.add(current);

        current = current.hasNext() ? current.getNext() : null;
      } while (current != null && current.getNestingLevel() >= nestingLevel);
    }

    int nestingLevel = activities.get(0).getNestingLevel();
    scopeId = null;

    for (TestCaseActivity activity : actualActivities) {
      if (activity.getNestingLevel() > nestingLevel && activity.hasMultiInstanceParent()) {
        // remember multi instance scope ID
        scopeId = activity.getParent().getId();
        continue;
      } else if (scopeId != null && activity.getNestingLevel() > nestingLevel) {
        // skip activities of sub scopes
        continue;
      }

      String parentId = activity.hasParent() ? activity.getParent().getId() : null;
      if (scopeId != null && !Objects.equals(parentId, scopeId)) {
        // add strategy, if multi instance scope ended
        strategies.add(ctx.getStrategy(scopeId));
        scopeId = null;
      }

      strategies.add(ctx.getStrategy(activity.getId()));

      nestingLevel = activity.getNestingLevel();
    }

    return strategies;
  }
}
