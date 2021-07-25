package org.camunda.community.bpmndt.api.cfg;

import org.camunda.bpm.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.community.bpmndt.api.TestCaseInstance;

/**
 * Custom post BPMN parse listener that overrides {@link CallActivityBehavior}s to make test cases
 * independent of sub processes.
 */
public class BpmndtParseListener extends AbstractBpmnParseListener {

  /** The current test case instance. */
  private TestCaseInstance instance;

  @Override
  public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
    if (instance == null) {
      return;
    }

    CallActivityBehavior behavior = (CallActivityBehavior) activity.getActivityBehavior();

    activity.setActivityBehavior(new BpmndtCallActivityBehavior(instance, behavior));
  }

  /**
   * Sets a reference to the current test case instance.
   * 
   * @param instance The current instance.
   */
  public void setInstance(TestCaseInstance instance) {
    this.instance = instance;
  }
}
