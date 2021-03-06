package org.camunda.community.bpmndt.api;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class MessageCatchEventTest {

  @Rule
  public TestCase tc = new TestCase();

  private EventHandler handler;

  @Before
  public void setUp() {
    handler = new EventHandler(tc.getProcessEngine(), "messageCatchEvent", "simpleMessage");
  }

  @Test
  public void testExecute() {
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

  private class TestCase extends AbstractJUnit4TestCase<TestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi, notNullValue());

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("messageCatchEvent");

      instance.apply(handler);

      piAssert.hasPassed("messageCatchEvent", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(Paths.get("./src/test/it/simple/src/main/resources/simpleMessageCatchEvent.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "simpleMessageCatchEvent";
    }

    @Override
    public String getStart() {
      return "startEvent";
    }

    @Override
    public String getEnd() {
      return "endEvent";
    }
  }
}
