package org.example.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.simplesubprocess.TC_startEvent__endEvent;
import generated.simplesubprocess.TC_startEvent__subProcessEndEvent;

public class SimpleSubProcessTest {

  @RegisterExtension
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();
  @RegisterExtension
  public TC_startEvent__subProcessEndEvent tcWaitAfter = new TC_startEvent__subProcessEndEvent();

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }

  @Test
  public void testExecuteAndWaitAfter() {
    tcWaitAfter.createExecutor().verify(pi -> {
      pi.isNotEnded();
    }).execute();
  }
}
