package org.example.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.simplemessagestartevent.TC_messageStartEvent__endEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class SimpleMessageStartEventTest {

  @RegisterExtension
  TC_messageStartEvent__endEvent tc = new TC_messageStartEvent__endEvent();

  ZeebeTestEngine engine;

  @Test
  void testExecute() {
    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }
}
