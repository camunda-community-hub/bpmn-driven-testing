package org.example.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.usertaskmessage.TC_startEvent__endEvent;

public class UserTaskMessageTest {

  @RegisterExtension
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.handleMultiInstanceUserTask().verifyLoopCount(2).handle(1).waitForBoundaryEvent();

    tc.createExecutor().execute();
  }
}
