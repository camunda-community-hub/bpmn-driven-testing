package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.TC_userTaskMessage__startEvent__endEvent;

public class UserTaskMessageTest {

  @Rule
  public TC_userTaskMessage__startEvent__endEvent tc = new TC_userTaskMessage__startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }
}
