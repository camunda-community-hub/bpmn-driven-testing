package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.TC_simpleSignalCatchEvent__startEvent__endEvent;

public class SimpleSignalCatchEventTest {

  @Rule
  public TC_simpleSignalCatchEvent__startEvent__endEvent tc = new TC_simpleSignalCatchEvent__startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }
}
