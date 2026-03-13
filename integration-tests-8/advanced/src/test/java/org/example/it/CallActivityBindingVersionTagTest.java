package org.example.it;

import static com.google.common.truth.Truth.assertThat;

import org.camunda.community.bpmndt.api.CallActivityBindingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.callactivitybindingversiontag.TC_startEvent__endEvent;
import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class CallActivityBindingVersionTagTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  @Test
  void testExecute() {
    tc.handleCallActivity()
        .verifyBindingType(CallActivityBindingType.VERSION_TAG)
        .verifyVersionTag("v1")
        .verifyVersionTag(versionTag -> assertThat(versionTag).isEqualTo("v1"));

    tc.createExecutor(client, processTestContext)
        .simulateVersionedProcess("advanced", "v1")
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }
}
