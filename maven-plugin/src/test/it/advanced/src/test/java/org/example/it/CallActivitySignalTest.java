package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.TC_callActivitySignal__startEvent__endEvent;

public class CallActivitySignalTest {

  @Rule
  public TC_callActivitySignal__startEvent__endEvent tc = new TC_callActivitySignal__startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }
}
