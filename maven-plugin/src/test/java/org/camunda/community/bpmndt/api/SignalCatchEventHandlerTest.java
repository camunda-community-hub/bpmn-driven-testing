package org.camunda.community.bpmndt.api;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class SignalCatchEventHandlerTest {

  @Rule
  public TestCase tc = new TestCase();

  private IntermediateCatchEventHandler handler;

  @Before
  public void setUp() {
    handler = new IntermediateCatchEventHandler(tc.getProcessEngine(), "signalCatchEvent", "simpleSignal");
  }

  @Test
  public void testDefaults() {
    tc.createExecutor().execute();
  }

  @Test
  public void testVerify() {
    handler.verify((pi, eventSubscription) -> {
      assertThat(pi, notNullValue());
      assertThat(eventSubscription, notNullValue());
    });

    tc.createExecutor().execute();
  }

  @Test
  public void testWithVariables() {
    handler.withVariable("a", "b").withVariableTyped("x", Variables.stringValue("y")).eventReceived();

    tc.createExecutor().verify(pi -> {
      pi.variables().containsEntry("a", "b");
      pi.variables().containsEntry("x", "y");
    }).execute();
  }

  private class TestCase extends AbstractJUnit4TestRule {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi, notNullValue());

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("signalCatchEvent");

      instance.apply(handler);

      piAssert.hasPassed("signalCatchEvent", "endEvent").isEnded();
    }

    @Override
    protected String getBpmnResourceName() {
      return "bpmn/simpleSignalCatchEvent.bpmn";
    }

    @Override
    protected String getProcessDefinitionKey() {
      return "simpleSignalCatchEvent";
    }

    @Override
    protected String getStart() {
      return "startEvent";
    }

    @Override
    protected String getEnd() {
      return "endEvent";
    }
  }
}
