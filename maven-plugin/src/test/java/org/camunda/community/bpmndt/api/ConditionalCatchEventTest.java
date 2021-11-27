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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ConditionalCatchEventTest {

  @Rule
  public TestCase tc = new TestCase();

  private EventHandler handler;

  @Before
  public void setUp() {
    handler = new EventHandler(tc.getProcessEngine(), "conditionalCatchEvent", null);
  }

  @Test
  public void testExecute() {
    // setting variable x to "y" triggers the conditional catch event
    handler.withVariable("x", "y").verify((pi, eventSubscription) -> {
      assertThat(pi, notNullValue());
      assertThat(eventSubscription, notNullValue());
    });

    tc.createExecutor().execute();
  }

  private class TestCase extends AbstractJUnit4TestCase {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi, notNullValue());

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("conditionalCatchEvent");

      instance.apply(handler);

      piAssert.hasPassed("conditionalCatchEvent", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(Paths.get("./src/test/it/simple/src/main/resources/simpleConditionalCatchEvent.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "simpleConditionalCatchEvent";
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
