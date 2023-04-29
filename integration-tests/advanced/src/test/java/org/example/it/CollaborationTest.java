package org.example.it;

import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.junit.Rule;
import org.junit.Test;

import generated.processa.TC_startEventA__endEventA;
import generated.processc.TC_startEventC__endEventC;
import generated.processc.TC_startEventC__subProcessEndC;
import generated.processc.TC_subProcessStartC__endEventC;

public class CollaborationTest {

  @Rule
  public TC_startEventA__endEventA tcA = new TC_startEventA__endEventA();

  @Rule
  public TC_startEventC__endEventC tcC = new TC_startEventC__endEventC();
  @Rule
  public TC_startEventC__subProcessEndC tcSubprocessEndC = new TC_startEventC__subProcessEndC();
  @Rule
  public TC_subProcessStartC__endEventC tcSubprocessStartC = new TC_subProcessStartC__endEventC();

  @Test
  public void testExecuteA() {
    tcA.createExecutor().verify(ProcessInstanceAssert::isEnded).execute();
  }

  @Test
  public void testExecuteC() {
    tcC.createExecutor().verify(ProcessInstanceAssert::isEnded).execute();
  }

  @Test
  public void testExecuteSubprocessEndC() {
    tcSubprocessEndC.createExecutor().verify(ProcessInstanceAssert::isNotEnded).execute();
  }

  @Test
  public void testExecuteSubProcessStartC() {
    tcSubprocessStartC.createExecutor().verify(ProcessInstanceAssert::isEnded).execute();
  }
}
