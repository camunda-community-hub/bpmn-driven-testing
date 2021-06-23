package org.example.it;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement.CallableElementBinding;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.engine.variable.VariableMap;

import generated.TC_advancedCallActivity__startEvent__endEvent;

public class CallActivityTest extends TC_advancedCallActivity__startEvent__endEvent {

  @Override
  protected String before(VariableMap variables) {
    Mocks.register("callActivityMapping", new CallActivityMapping());

    return null;
  }

  @Override
  protected void callActivity_input(VariableScope subInstance) {
    assertThat(callActivityRule.getBinding(), is(CallableElementBinding.VERSION));
    assertThat(callActivityRule.getBusinessKey(), nullValue());
    assertThat(callActivityRule.getDefinitionKey(), equalTo("advanced"));
    assertThat(callActivityRule.getTenantId(), nullValue());
    assertThat(callActivityRule.getVersion(), is(1));
    assertThat(callActivityRule.getVersionTag(), nullValue());

    assertThat(subInstance.getVariable("a"), equalTo("b"));
    assertThat(subInstance.getVariable("x"), equalTo("y"));
  }

  @Override
  protected void callActivity_output(DelegateExecution execution) {
    assertThat(execution.getVariable("a"), equalTo("y"));
    assertThat(execution.getVariable("x"), equalTo("b"));
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
}
