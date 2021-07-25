package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.TC_simpleExternalTask__startEvent__endEvent;

public class SimpleExternalTaskTest {

  @Rule
  public TC_simpleExternalTask__startEvent__endEvent tc = new TC_simpleExternalTask__startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }
}
