package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.TC_simple__startEvent__endEvent;

public class SimpleTest {

  @Rule
  public TC_simple__startEvent__endEvent tc = new TC_simple__startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }
}
