package org.example.it;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.camunda.bpm.engine.impl.core.model.BaseCallableElement.CallableElementBinding;
import org.junit.Rule;
import org.junit.Test;

import generated.TC_callActivityWithoutMapping__startEvent__endEvent;

public class CallActivityWithoutMappingTest {

  @Rule
  public TC_callActivityWithoutMapping__startEvent__endEvent tc = new TC_callActivityWithoutMapping__startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.handleCallActivity().verify((pi, callActivity) -> {
      assertThat(callActivity.getBinding(), is(CallableElementBinding.LATEST));
      assertThat(callActivity.getBusinessKey(), equalTo("advancedBusinessKey"));
      assertThat(callActivity.getDefinitionKey(), equalTo("advanced"));
      assertThat(callActivity.getDefinitionTenantId(), equalTo("advancedTenantId"));
      assertThat(callActivity.getVersion(), nullValue());
      assertThat(callActivity.getVersionTag(), nullValue());
    });

    tc.createExecutor().execute();
  }
}
