package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.callactivityerror.TC_startEvent__callActivity;
import generated.callactivityerror.TC_startEvent__endEvent;
import generated.callactivityerror.TC_startEvent__errorBoundaryEvent;

public class CallActivityErrorTest {

  @Rule
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();
  @Rule
  public TC_startEvent__callActivity tcCallActivity = new TC_startEvent__callActivity();
  @Rule
  public TC_startEvent__errorBoundaryEvent tcErrorBoundaryEvent = new TC_startEvent__errorBoundaryEvent();

  @Test
  public void testExecute() {
    tc.handleCallActivity().withErrorMessage("callActivityErrorMessage");

    tc.createExecutor().verify(pi -> {
      pi.variables().containsEntry("errorCode", "callActivityError");
      pi.variables().containsEntry("errorMessage", "callActivityErrorMessage");
    }).execute();
  }

  @Test
  public void testExecuteAndWaitAfterCallActivity() {
    tcCallActivity.createExecutor().verify(pi -> {
      pi.isNotEnded();
    }).execute();
  }

  @Test
  public void testExecuteAndWaitAfterErrorBoundaryEvent() {
    tcErrorBoundaryEvent.createExecutor().verify(pi -> {
      pi.isNotEnded();
    }).execute();
  }
}
