package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.callactivityerror.TC_startEvent__endEvent;

public class CallActivityErrorTest {

  @Rule
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.handleCallActivity().withErrorMessage("callActivityErrorMessage");

    tc.createExecutor().verify(pi -> {
      pi.variables().containsEntry("errorCode", "callActivityError");
      pi.variables().containsEntry("errorMessage", "callActivityErrorMessage");
    }).execute();
  }
}
