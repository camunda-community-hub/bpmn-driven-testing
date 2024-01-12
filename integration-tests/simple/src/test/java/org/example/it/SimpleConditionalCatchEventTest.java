package org.example.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.simpleconditionalcatchevent.TC_startEvent__endEvent;

public class SimpleConditionalCatchEventTest {

  @RegisterExtension
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  @Test
  public void testExecute() {
    // setting variable x to "y" triggers the conditional catch event
    tc.handleConditionalCatchEvent().withVariable("x", "y");

    tc.createExecutor().execute();
  }
}
