package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.simplemessagecatchevent.TC_startEvent__endEvent;

public class SimpleMessageCatchEventTest {

  @Rule
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }
}
