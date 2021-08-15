package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.TC_externalTaskError__startEvent__endEvent;

public class ExternalTaskErrorTest {

  @Rule
  public TC_externalTaskError__startEvent__endEvent tc = new TC_externalTaskError__startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.handleExternalTask().withErrorMessage("externalTaskErrorMessage");

    tc.createExecutor().verify(pi -> {
      pi.variables().containsEntry("errorCode", "externalTaskError");
      pi.variables().containsEntry("errorMessage", "externalTaskErrorMessage");
    }).execute();
  }
}
