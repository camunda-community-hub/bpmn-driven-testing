package org.example.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.externaltaskerror.TC_startEvent__endEvent;

public class ExternalTaskErrorTest {

  @RegisterExtension
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.handleMultiInstanceExternalTask().verifyLoopCount(2).handle(1).handleBpmnError("externalTaskError", "externalTaskErrorMessage");

    tc.createExecutor().execute();
  }
}
