package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.TC_simpleTimerCatchEvent__startEvent__endEvent;

public class SimpleTimerCatchEventTest {

  @Rule
  public TC_simpleTimerCatchEvent__startEvent__endEvent tc = new TC_simpleTimerCatchEvent__startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }
}
