package org.example.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.simplesubprocessnested.TC_startEvent__endEvent;
import generated.simplesubprocessnested.TC_userTask__endEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class SimpleSubProcessNestedTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();
  @RegisterExtension
  TC_userTask__endEvent tcUserTask = new TC_userTask__endEvent();

  ZeebeTestEngine engine;

  @Test
  void testExecute() {
    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testExecuteUserTask() {
    tcUserTask.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }
}
