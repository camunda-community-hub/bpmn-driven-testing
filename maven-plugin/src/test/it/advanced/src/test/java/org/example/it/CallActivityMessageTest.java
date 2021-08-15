package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.TC_callActivityMessage__startEvent__endEvent;

public class CallActivityMessageTest {

  @Rule
  public TC_callActivityMessage__startEvent__endEvent tc = new TC_callActivityMessage__startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }
}
