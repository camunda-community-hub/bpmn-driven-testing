package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.TC_externalTaskMessage__startEvent__endEvent;

public class ExternalTaskMessageTest {

  @Rule
  public TC_externalTaskMessage__startEvent__endEvent tc = new TC_externalTaskMessage__startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }
}
