package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.usertask.TC_startEvent__endEvent;

public class UserTaskTest {

  @Rule
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.handleMultiInstanceUserTask().verifyLoopCount(3).handleDefault().verify((pi, task) -> {
      task.hasName("User task");
    });

    tc.createExecutor().execute();
  }
}
