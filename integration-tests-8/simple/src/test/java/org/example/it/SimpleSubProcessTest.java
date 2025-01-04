package org.example.it;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.simplesubprocess.TC_startEvent__endEvent;
import generated.simplesubprocess.TC_startEvent__subProcessEndEvent;
import generated.simplesubprocess.TC_subProcessStartEvent__endEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class SimpleSubProcessTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();
  @RegisterExtension
  TC_startEvent__subProcessEndEvent tcSubProcessEndEvent = new TC_startEvent__subProcessEndEvent();
  @RegisterExtension
  TC_subProcessStartEvent__endEvent tcSubProcessStartEvent = new TC_subProcessStartEvent__endEvent();

  ZeebeTestEngine engine;

  @Test
  void testExecute() {
    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testExecuteSubProcessEndEvent() {
    tcSubProcessEndEvent.createExecutor(engine).execute();
  }

  // currently not supported
  // element with id 'subProcessStartEvent' targets unsupported element type 'START_EVENT'
  @Test
  @Disabled
  void testExecuteSubProcessStartEvent() {
    tcSubProcessStartEvent.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }
}

