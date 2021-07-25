package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.TC_simpleSubProcess__startEvent__endEvent;

public class SimpleSubProcessTest {

  @Rule
  public TC_simpleSubProcess__startEvent__endEvent tc = new TC_simpleSubProcess__startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }
}
