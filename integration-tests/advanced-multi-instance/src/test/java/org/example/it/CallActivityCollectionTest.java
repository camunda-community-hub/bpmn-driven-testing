package org.example.it;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.callactivitycollection.TC_startEvent__endEvent;

public class CallActivityCollectionTest {

  @RegisterExtension
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.handleCallActivity().verifyLoopCount(3);

    tc.handleCallActivity().handle(0).verify((piAssert, callActivityDefinition) -> {
        piAssert.variables().containsKey("elements"); // assertion of process scoped variables only (e.g. collection)

        // find process instance of test case, e.g. by business key
        var pi = tc.getProcessEngine().getRuntimeService().createProcessInstanceQuery()
            .processInstanceBusinessKey("bk")
            .singleResult();

        // find historic activity instance of call activity in multi instance scope
        var historicActivityInstance = tc.getProcessEngine().getHistoryService().createHistoricActivityInstanceQuery()
              .activityId("multiInstanceCallActivity#multiInstanceBody") // BPMN element ID + #multiInstanceBody
              .singleResult();

        // get local variables (including element variable of collection) of related execution
        var localVariables = tc.getProcessEngine().getRuntimeService().getVariablesLocal(historicActivityInstance.getExecutionId());
        assertThat(localVariables.get("element"), equalTo("a"));
    });

    tc.handleCallActivity().handle(0).verifyInput(variables -> {
        assertThat(variables.getVariable("element"), equalTo("a"));
    });
    tc.handleCallActivity().handle(1).verifyInput(variables -> {
        assertThat(variables.getVariable("element"), equalTo("b"));
    });
    tc.handleCallActivity().handle(2).verifyInput(variables -> {
        assertThat(variables.getVariable("element"), equalTo("c"));
    });

    tc.createExecutor()
        .withBusinessKey("bk")
        .withVariable("elements", List.of("a", "b", "c"))
        .verify(ProcessInstanceAssert::isEnded)
        .execute();
  }
}
