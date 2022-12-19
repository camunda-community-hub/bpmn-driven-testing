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

/**
 * Message throw event, that is implemented as external task.
 */
public class MessageThrowEventTest {

  @RegisterExtension
  public TestCase tc = new TestCase();

  private ExternalTaskHandler handler;

  @BeforeEach
  public void setUp() {
    handler = new ExternalTaskHandler(tc.getProcessEngine(), "messageThrowEvent", "test-message");
  }

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }

  private class TestCase extends AbstractJUnit5TestCase<TestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi).isNotNull();

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("messageThrowEvent");

      instance.apply(handler);

      piAssert.hasPassed("messageThrowEvent", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.simple("simpleMessageThrowEvent.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "simpleMessageThrowEvent";
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
