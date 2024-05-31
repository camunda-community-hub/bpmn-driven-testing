package org.example.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.callactivityerror.TC_startEvent__endEvent;
import generated.linkevent.TC_forkA__linkCatchEventA;
import generated.linkevent.TC_forkB__linkCatchEventB;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
public class LinkEventTest {

  @RegisterExtension
  TC_forkA__linkCatchEventA tcA = new TC_forkA__linkCatchEventA();
  @RegisterExtension
  TC_forkB__linkCatchEventB tcB = new TC_forkB__linkCatchEventB();

  ZeebeTestEngine engine;

  @Test
  void testExecuteA() {
    tcA.createExecutor(engine)
        .withVariable("forkA", true)
        .withVariable("forkB", false)
        .verify(ProcessInstanceAssert::isNotCompleted)
        .execute();
  }

  @Test
  void testExecuteB() {
    tcB.createExecutor(engine)
        .withVariable("forkA", false)
        .withVariable("forkB", true)
        .verify(ProcessInstanceAssert::isNotCompleted)
        .execute();
  }
}
