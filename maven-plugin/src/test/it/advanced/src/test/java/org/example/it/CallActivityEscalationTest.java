package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.TC_callActivityEscalation__startEvent__endEvent;

public class CallActivityEscalationTest {

  @Rule
  public TC_callActivityEscalation__startEvent__endEvent tc = new TC_callActivityEscalation__startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }
}
