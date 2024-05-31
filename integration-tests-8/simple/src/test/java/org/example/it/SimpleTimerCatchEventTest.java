package org.example.it;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.simpletimercatchevent.TC_startEvent__endEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class SimpleTimerCatchEventTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  ZeebeTestEngine engine;

  @Test
  void testExecute() {
    tc.handleTimerCatchEvent()
        .verifyTimeDuration(duration -> assertThat(duration.toMillis()).isEqualTo(3600000))
        .verifyTimeDurationExpression(expr -> assertThat(expr).isEqualTo("PT1H"));

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }
}
