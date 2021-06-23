package org.example.it;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement.CallableElementBinding;

import generated.TC_advancedCallActivityNoMapping__startEvent__endEvent;

public class CallActivityNoMappingTest extends TC_advancedCallActivityNoMapping__startEvent__endEvent {

  @Override
  protected void callActivity_input(VariableScope subInstance) {
    assertThat(callActivityRule.getBinding(), is(CallableElementBinding.LATEST));
    assertThat(callActivityRule.getBusinessKey(), equalTo("advancedBusinessKey"));
    assertThat(callActivityRule.getDefinitionKey(), equalTo("advanced"));
    assertThat(callActivityRule.getTenantId(), equalTo("advancedTenantId"));
    assertThat(callActivityRule.getVersion(), nullValue());
    assertThat(callActivityRule.getVersionTag(), nullValue());
  }
}
