package org.example.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.usertaskerror.TC_startEvent__endEvent;

public class UserTaskErrorTest {

  @RegisterExtension
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.handleMultiInstanceUserTask().verifyLoopCount(2).handle(1).handleBpmnError("userTaskError", "userTaskErrorMessage");

    tc.createExecutor().execute();
  }
}
