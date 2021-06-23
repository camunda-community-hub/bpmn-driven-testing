package org.camunda.bpm.extension.bpmndt.blueprint;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.TaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement.CallableElementBinding;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin;
import org.junit.Rule;
import org.junit.Test;

/**
 * Blueprint for testing call activities without deployment of the called element and without
 * creation of a sub process instance.
 */
public class CallActivityBlueprintTest {

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule(buildProcessEngine(), true);

  private ProcessEngine buildProcessEngine() {
    ProcessEngine processEngine = ProcessEngines.getProcessEngine("bpmndtEngine");
    if (processEngine == null) {
      List<BpmnParseListener> postBPMNParseListeners = new LinkedList<>();
      postBPMNParseListeners.add(new CustomCallActivityParseListener());

      ProcessEngineConfigurationImpl configuration = new StandaloneInMemProcessEngineConfiguration();
      configuration.setCmmnEnabled(false);
      configuration.setCustomPostBPMNParseListeners(postBPMNParseListeners);
      configuration.setDmnEnabled(false);
      configuration.setHistoryLevel(HistoryLevel.HISTORY_LEVEL_FULL);
      configuration.setInitializeTelemetry(false);
      configuration.setJobExecutorActivate(false);
      configuration.setMetricsEnabled(false);
      configuration.setProcessEngineName("bpmndtEngine");
      configuration.setProcessEnginePlugins(Arrays.asList(new SpinProcessEnginePlugin()));

      processEngine = configuration.buildProcessEngine();
    }

    return processEngine;
  }

  @Test
  @Deployment(resources = {"bpmn/simpleCallActivity.bpmn"})
  public void testBlueprint() {
    Mocks.register("callActivityMapping", new CallActivityMapping());

    ProcessInstance pi = rule.getRuntimeService().startProcessInstanceByKey("simpleCallActivity", "businessKey");
    assertThat(pi.isEnded(), is(true));

    BpmnParseListener l = rule.getProcessEngineConfiguration().getCustomPostBPMNParseListeners().get(0);
    assertThat(l, instanceOf(CustomCallActivityParseListener.class));
  }

  private class CallActivityMapping implements DelegateVariableMapping {

    @Override
    public void mapInputVariables(DelegateExecution superExecution, VariableMap subVariables) {
      subVariables.put("a", "b");
      subVariables.put("x", "y");
    }

    @Override
    public void mapOutputVariables(DelegateExecution superExecution, VariableScope subInstance) {
      Object a = subInstance.getVariable("a");
      Object x = subInstance.getVariable("x");

      superExecution.setVariable("a", x);
      superExecution.setVariable("x", a);
    }
  }
  
  private class CustomCallActivityParseListener extends AbstractBpmnParseListener {

    @Override
    public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
      CallActivityBehavior behavior = (CallActivityBehavior) activity.getActivityBehavior();
      
      activity.setActivityBehavior(new CustomCallActivityBehavior(behavior));
    }
  }

  private class CustomCallActivityBehavior extends TaskActivityBehavior {

    private final CallActivityBehavior behavior;

    private CustomCallActivityBehavior(CallActivityBehavior behavior) {
      this.behavior = behavior;
    }

    @Override
    public void execute(ActivityExecution execution) throws Exception {
      assertThat(execution.getCurrentActivityId(), equalTo("callActivity"));

      assertThat(behavior.getCallableElement().getBinding(), is(CallableElementBinding.DEPLOYMENT));
      assertThat(behavior.getCallableElement().getBusinessKey(execution), equalTo("businessKey"));
      assertThat(behavior.getCallableElement().getDefinitionKey(execution), equalTo("simple"));
      assertThat(behavior.getCallableElement().getDefinitionTenantId(execution), nullValue());
      assertThat(behavior.getCallableElement().getVersion(execution), nullValue());
      assertThat(behavior.getCallableElement().getVersion(execution), nullValue());
      assertThat(behavior.getCallableElement().getVersionTag(execution), nullValue());

      DelegateVariableMapping variableMapping = (DelegateVariableMapping) behavior.resolveDelegateClass(execution);

      VariableMap subVariables = Variables.createVariables();

      // map input
      variableMapping.mapInputVariables(execution, subVariables);
      assertThat(subVariables.get("a"), equalTo("b"));
      assertThat(subVariables.get("x"), equalTo("y"));

      // create sub execution
      ActivityExecution subInstance = execution.createExecution();
      subInstance.setVariables(subVariables);

      // map output
      variableMapping.mapOutputVariables(execution, subInstance);
      assertThat(execution.getVariable("a"), equalTo("y"));
      assertThat(execution.getVariable("x"), equalTo("b"));

      // remove sub execution
      subInstance.remove();

      super.execute(execution);
    }
  }
}
