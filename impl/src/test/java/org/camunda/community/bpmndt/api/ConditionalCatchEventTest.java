package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ConditionalCatchEventTest {

  @RegisterExtension
  public TestCase tc = new TestCase();

  private EventHandler handler;

  @BeforeEach
  public void setUp() {
    handler = new EventHandler(tc.getProcessEngine(), "conditionalCatchEvent", null);
  }

  @Test
  public void testExecute() {
    // setting variable x to "y" triggers the conditional catch event
    handler.withVariable("x", "y").verify((pi, eventSubscription) -> {
      assertThat(pi).isNotNull();
      assertThat(eventSubscription).isNotNull();
    });

    tc.createExecutor().execute();
  }

  private class TestCase extends AbstractJUnit5TestCase<TestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi).isNotNull();

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("conditionalCatchEvent");

      instance.apply(handler);

      piAssert.hasPassed("conditionalCatchEvent", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.simple("simpleConditionalCatchEvent.bpmn"));
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
