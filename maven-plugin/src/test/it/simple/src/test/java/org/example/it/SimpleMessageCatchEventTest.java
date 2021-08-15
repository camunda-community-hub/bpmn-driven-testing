package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.TC_simpleReceiveTask__startEvent__endEvent;

public class SimpleMessageCatchEventTest {

  @Rule
  public TC_simpleReceiveTask__startEvent__endEvent tc = new TC_simpleReceiveTask__startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }
}
