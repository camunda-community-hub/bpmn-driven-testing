package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.TC_subProcessCallActivityError__startEvent__endEvent;

public class SubProcessCallActivityMessageTest {

  @Rule
  public TC_subProcessCallActivityError__startEvent__endEvent tc = new TC_subProcessCallActivityError__startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }
}
