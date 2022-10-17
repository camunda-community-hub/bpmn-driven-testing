package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.scopezero.TC_startEvent__endEvent;

public class ScopeZeroTest {

  @Rule
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.handleSubProcess().verifyLoopCount(0);

    tc.createExecutor().execute();
  }
}
