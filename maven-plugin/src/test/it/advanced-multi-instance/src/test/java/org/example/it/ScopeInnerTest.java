package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.scopeinner.TC_startEvent__subProcessEndEvent;
import generated.scopeinner.TC_subProcessStartEvent__endEvent;
import generated.scopeinner.TC_subProcessStartEvent__subProcessEndEvent;

public class ScopeInnerTest {

  @Rule
  public TC_subProcessStartEvent__subProcessEndEvent tc = new TC_subProcessStartEvent__subProcessEndEvent();
  @Rule
  public TC_startEvent__subProcessEndEvent tcInnerEnd = new TC_startEvent__subProcessEndEvent();
  @Rule
  public TC_subProcessStartEvent__endEvent tcInnerStart = new TC_subProcessStartEvent__endEvent();

  @Test
  public void testExecute() {
    tc.handleSubProcess().verifyLoopCount(2);

    tc.createExecutor().execute();
  }

  @Test
  public void testExecuteInnerEnd() {
    tcInnerEnd.handleSubProcess().verifyLoopCount(2);

    tcInnerEnd.createExecutor().execute();
  }

  @Test
  public void testExecuteInnerStart() {
    tcInnerStart.handleSubProcess().verifyLoopCount(2);

    tcInnerStart.createExecutor().execute();
  }
}
