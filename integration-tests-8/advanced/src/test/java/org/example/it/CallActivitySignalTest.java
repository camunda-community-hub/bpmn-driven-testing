package org.example.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.callactivitysignal.TC_startEvent__endEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
public class CallActivitySignalTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  ZeebeTestEngine engine;

  @Test
  void testExecute() {
    tc.createExecutor(engine).simulateProcess("advanced").verify(ProcessInstanceAssert::isCompleted).execute();
  }
}
