package org.example.it;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.callactivityvariables.TC_startEvent__endEvent;

public class CallActivityVariablesTest {

  @RegisterExtension
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.handleCallActivity().verify((pi, callActivity) -> {
      pi.variables().containsEntry("x", 1);

      assertThat(callActivity.hasInputs(), is(true));
      assertThat(callActivity.hasOutputs(), is(true));
    }).verifyInput(variables -> {
      assertThat(variables.hasVariable("y"), is(true));
      assertThat(variables.getVariable("y"), equalTo(1));

      variables.setVariable("y", 2);
    }).verifyOutput(variables -> {
      assertThat(variables.hasVariable("z"), is(true));
      assertThat(variables.getVariable("z"), equalTo(2));
    });

    tc.createExecutor().withVariable("x", 1).execute();
  }
}
