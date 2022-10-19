package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.scopenested.TC_startEvent__endEvent;

public class ScopeNestedSubProcessTest {

  @Rule
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.handleSubProcess().verifyLoopCount(3);
    tc.handleSubProcess().handleNestedSubProcess().verifyLoopCount(2);

    tc.createExecutor().execute();
  }
}
