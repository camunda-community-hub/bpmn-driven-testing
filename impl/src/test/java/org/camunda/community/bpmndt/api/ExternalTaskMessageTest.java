package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ExternalTaskMessageTest {

  @RegisterExtension
  public TestCase tc = new TestCase();

  private ExternalTaskHandler handler;

  @BeforeEach
  public void setUp() {
    handler = new ExternalTaskHandler(tc.getProcessEngine(), "externalTask", "test-topic");
  }

  @Test
  public void testExecute() {
    handler.execute(task -> {});

    tc.createExecutor().execute();
  }

  private class TestCase extends AbstractJUnit5TestCase<TestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi).isNotNull();

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("externalTask");

      instance.apply(handler);
      
      RuntimeService runtimeService = tc.getProcessEngine().getRuntimeService();

      EventSubscription eventSubscription = runtimeService.createEventSubscriptionQuery().singleResult();

      runtimeService.messageEventReceived(eventSubscription.getEventName(), eventSubscription.getExecutionId());

      piAssert.hasPassed("externalTask", "messageBoundaryEvent", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.advanced("externalTaskMessage.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "externalTaskMessage";
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
