package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.scopeinner.TC_startEvent__subProcessEndEvent;
import generated.scopeinner.TC_subProcessStartEvent__endEvent;
import generated.scopeinner.TC_subProcessStartEvent__subProcessEndEvent;

public class ScopeInnerTest {

  @Rule
  public TC_subProcessStartEvent__subProcessEndEvent tcInner = new TC_subProcessStartEvent__subProcessEndEvent();
  @Rule
  public TC_startEvent__subProcessEndEvent tcInnerEnd = new TC_startEvent__subProcessEndEvent();
  @Rule
  public TC_subProcessStartEvent__endEvent tcInnerStart = new TC_subProcessStartEvent__endEvent();

  @Test
  public void testExecuteInner() {
    tcInner.createExecutor().execute();
  }

  @Test
  public void testExecuteInnerEnd() {
    tcInnerEnd.createExecutor().execute();
  }

  @Test
  public void testExecuteInnerStart() {
    tcInnerStart.createExecutor().execute();
  }
}
