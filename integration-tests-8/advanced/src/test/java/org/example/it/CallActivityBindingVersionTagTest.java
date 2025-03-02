package org.example.it;

import static com.google.common.truth.Truth.assertThat;

import org.camunda.community.bpmndt.api.CallActivityBindingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.callactivitybindingversiontag.TC_startEvent__endEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class CallActivityBindingVersionTagTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  ZeebeTestEngine engine;

  @Test
  void testExecute() {
    tc.handleCallActivity()
        .verifyBindingType(CallActivityBindingType.VERSION_TAG)
        .verifyVersionTag("v1")
        .verifyVersionTag(versionTag -> assertThat(versionTag).isEqualTo("v1"));

    tc.createExecutor(engine).simulateVersionedProcess("advanced", "v1").verify(ProcessInstanceAssert::isCompleted).execute();
  }
}
