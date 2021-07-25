package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.TC_simpleAsync__startEvent__endEvent;

public class SimpleAsyncTest {

  @Rule
  public TC_simpleAsync__startEvent__endEvent tc = new TC_simpleAsync__startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }
}
