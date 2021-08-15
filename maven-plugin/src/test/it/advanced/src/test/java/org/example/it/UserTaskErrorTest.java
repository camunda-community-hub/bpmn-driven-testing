package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.TC_userTaskError__startEvent__endEvent;

public class UserTaskErrorTest {

  @Rule
  public TC_userTaskError__startEvent__endEvent tc = new TC_userTaskError__startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.handleUserTask().withErrorMessage("userTaskErrorMessage");

    tc.createExecutor().verify(pi -> {
      pi.variables().containsEntry("errorCode", "userTaskError");
      pi.variables().containsEntry("errorMessage", "userTaskErrorMessage");
    }).execute();
  }
}
