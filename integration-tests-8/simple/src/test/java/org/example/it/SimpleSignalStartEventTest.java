package org.example.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.simplesignalstartevent.TC_signalStartEvent__endEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class SimpleSignalStartEventTest {

  @RegisterExtension
  TC_signalStartEvent__endEvent tc = new TC_signalStartEvent__endEvent();

  ZeebeTestEngine engine;

  @Test
  void testExecute() {
    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }
}
