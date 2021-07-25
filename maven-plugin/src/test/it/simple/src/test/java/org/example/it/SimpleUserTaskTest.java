package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.TC_simpleUserTask__startEvent__endEvent;

public class SimpleUserTaskTest {

  @Rule
  public TC_simpleUserTask__startEvent__endEvent tc = new TC_simpleUserTask__startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }
}
