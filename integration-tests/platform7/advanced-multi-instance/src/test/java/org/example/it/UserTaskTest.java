package org.example.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.usertask.TC_startEvent__endEvent;

public class UserTaskTest {

  @RegisterExtension
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.handleMultiInstanceUserTask().verifyLoopCount(3).handle().verify((pi, task) -> {
      task.hasName("User task");
    });

    tc.createExecutor().execute();
  }
}
