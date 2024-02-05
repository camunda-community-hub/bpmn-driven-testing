package org.example.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.usertaskerror.TC_startEvent__endEvent;

public class UserTaskErrorTest {

  @RegisterExtension
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.handleUserTask().withErrorMessage("userTaskErrorMessage");

    tc.createExecutor().verify(pi -> {
      pi.variables().containsEntry("errorCode", "userTaskError");
      pi.variables().containsEntry("errorMessage", "userTaskErrorMessage");
    }).execute();
  }
}
