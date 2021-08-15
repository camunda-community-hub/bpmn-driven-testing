package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.TC_callActivityTimer__startEvent__endEvent;

public class CallActivityTimerTest {

  @Rule
  public TC_callActivityTimer__startEvent__endEvent tc = new TC_callActivityTimer__startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }
}
