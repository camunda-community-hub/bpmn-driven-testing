package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.simpleconditionalcatchevent.TC_startEvent__endEvent;

public class SimpleConditionalCatchEventTest {

  @Rule
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  @Test
  public void testExecute() {
    // setting variable x to "y" triggers the conditional catch event
    tc.handleConditionalCatchEvent().withVariable("x", "y");

    tc.createExecutor().execute();
  }
}
