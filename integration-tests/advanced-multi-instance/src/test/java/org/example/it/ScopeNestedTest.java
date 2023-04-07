package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.scopenested.TC_nestedSubProcessStartEvent__endEvent;
import generated.scopenested.TC_startEvent__endEvent;
import generated.scopenested.TC_subProcessStartEvent__endEvent;

public class ScopeNestedTest {

  @Rule
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();
  @Rule
  public TC_subProcessStartEvent__endEvent tcSubProcessStart = new TC_subProcessStartEvent__endEvent();
  @Rule
  public TC_nestedSubProcessStartEvent__endEvent tcNestedSubProcessStart = new TC_nestedSubProcessStartEvent__endEvent();

  @Test
  public void testExecute() {
    tc.handleSubProcess().verifyLoopCount(3);
    tc.handleSubProcess().handleNestedSubProcess().verifyLoopCount(2);

    tc.createExecutor().execute();
  }

  @Test
  public void testExecuteSubProcessStart() {
    tcSubProcessStart.handleNestedSubProcess().verifyLoopCount(2);

    tc.createExecutor().execute();
  }

  @Test
  public void testExecuteNestedSubProcessStart() {
    tcNestedSubProcessStart.createExecutor().execute();
  }
}
