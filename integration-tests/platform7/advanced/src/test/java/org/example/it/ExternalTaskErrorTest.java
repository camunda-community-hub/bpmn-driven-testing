package org.example.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.externaltaskerror.TC_startEvent__endEvent;

public class ExternalTaskErrorTest {

  @RegisterExtension
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.handleExternalTask().withErrorMessage("externalTaskErrorMessage");

    tc.createExecutor().verify(pi -> {
      pi.variables().containsEntry("errorCode", "externalTaskError");
      pi.variables().containsEntry("errorMessage", "externalTaskErrorMessage");
    }).execute();
  }
}
