package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.TC_userTaskEscalation__startEvent__endEvent;

public class UserTaskEscalationTest {

  @Rule
  public TC_userTaskEscalation__startEvent__endEvent tc = new TC_userTaskEscalation__startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }
}
