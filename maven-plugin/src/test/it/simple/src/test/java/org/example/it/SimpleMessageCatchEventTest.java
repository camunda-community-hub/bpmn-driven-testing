package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.TC_simpleMessageCatchEvent__startEvent__endEvent;

public class SimpleMessageCatchEventTest {

  @Rule
  public TC_simpleMessageCatchEvent__startEvent__endEvent tc = new TC_simpleMessageCatchEvent__startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }
}
