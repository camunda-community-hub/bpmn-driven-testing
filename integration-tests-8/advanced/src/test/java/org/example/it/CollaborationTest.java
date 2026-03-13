package org.example.it;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.processa.TC_startEventA__endEventA;
import generated.processc.TC_startEventC__endEventC;
import generated.processc.TC_startEventC__subProcessEndC;
import generated.processc.TC_subProcessStartC__endEventC;
import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
public class CollaborationTest {

  @RegisterExtension
  TC_startEventA__endEventA tcA = new TC_startEventA__endEventA();

  @RegisterExtension
  TC_startEventC__endEventC tcC1 = new TC_startEventC__endEventC();
  @RegisterExtension
  TC_startEventC__subProcessEndC tcC2 = new TC_startEventC__subProcessEndC();
  @RegisterExtension
  TC_subProcessStartC__endEventC tcC3 = new TC_subProcessStartC__endEventC();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  @Test
  void testExecuteA() {
    tcA.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testExecuteC1() {
    tcC1.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testExecuteC2() {
    tcC2.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  // currently not supported
  // element with id 'subProcessStartC' targets unsupported element type 'START_EVENT'
  @Test
  @Disabled
  void testExecuteC3() {
    tcC3.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }
}
