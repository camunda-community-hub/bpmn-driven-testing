package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.usertaskerror.TC_startEvent__endEvent;

public class UserTaskErrorTest {

  @Rule
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.handleMultiInstanceUserTask().verifyLoopCount(2).handle(1).handleBpmnError("userTaskError", "userTaskErrorMessage");

    tc.createExecutor().execute();
  }
}
