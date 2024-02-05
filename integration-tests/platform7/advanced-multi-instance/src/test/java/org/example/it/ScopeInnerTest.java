package org.example.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.scopeinner.TC_startEvent__subProcessEndEvent;
import generated.scopeinner.TC_subProcessStartEvent__endEvent;
import generated.scopeinner.TC_subProcessStartEvent__subProcessEndEvent;

public class ScopeInnerTest {

  @RegisterExtension
  public TC_subProcessStartEvent__subProcessEndEvent tcInner = new TC_subProcessStartEvent__subProcessEndEvent();
  @RegisterExtension
  public TC_startEvent__subProcessEndEvent tcInnerEnd = new TC_startEvent__subProcessEndEvent();
  @RegisterExtension
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
