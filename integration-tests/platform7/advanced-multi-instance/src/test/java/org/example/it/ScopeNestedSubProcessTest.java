package org.example.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.scopenestedsubprocess.TC_startEvent__endEvent;

public class ScopeNestedSubProcessTest {

  @RegisterExtension
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.handleSubProcess().verifyLoopCount(3);

    tc.createExecutor().execute();
  }
}
