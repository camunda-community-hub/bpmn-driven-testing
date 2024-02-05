package org.example.it;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement.CallableElementBinding;
import org.camunda.bpm.engine.variable.VariableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.callactivitywithmapping.TC_startEvent__endEvent;

public class CallActivityWithMappingTest {

  @RegisterExtension
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.handleCallActivity().verify((pi, callActivity) -> {
      assertThat(callActivity.getBinding(), is(CallableElementBinding.VERSION));
      assertThat(callActivity.getBusinessKey(), nullValue());
      assertThat(callActivity.getDefinitionKey(), equalTo("advanced"));
      assertThat(callActivity.getDefinitionTenantId(), equalTo("tenant-1"));
      assertThat(callActivity.getVersion(), is(1));
      assertThat(callActivity.getVersionTag(), nullValue());
      assertThat(callActivity.hasInputs(), is(false));
      assertThat(callActivity.hasOutputs(), is(false));
    }).verifyInput(variables -> {
      assertThat(variables.getVariable("a"), equalTo("b"));
      assertThat(variables.getVariable("x"), equalTo("y"));
    }).verifyOutput(variables -> {
      assertThat(variables.getVariable("a"), equalTo("y"));
      assertThat(variables.getVariable("x"), equalTo("b"));
    });

    tc.createExecutor().withBean("callActivityMapping", new CallActivityMapping()).execute();
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
