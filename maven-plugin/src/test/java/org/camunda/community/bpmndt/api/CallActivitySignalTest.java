package org.camunda.community.bpmndt.api;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class CallActivitySignalTest {

  @Rule
  public TestCase tc = new TestCase();

  private CallActivityHandler handler;

  @Before
  public void setUp() {
    handler = new CallActivityHandler(tc.instance, "callActivity");
  }

  @Test
  public void testExecute() {
    handler.waitForBoundaryEvent();

    tc.createExecutor().execute();
  }

  private class TestCase extends AbstractJUnit4TestCase {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi, notNullValue());

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("callActivity");

      // async before
      ProcessEngineTests.execute(ProcessEngineTests.job());

      piAssert.isWaitingAt("callActivity");

      RuntimeService runtimeService = tc.getProcessEngine().getRuntimeService();

      EventSubscription eventSubscription = runtimeService.createEventSubscriptionQuery().singleResult();

      runtimeService.signalEventReceived(eventSubscription.getEventName(), eventSubscription.getExecutionId());

      piAssert.hasPassed("callActivity", "signalBoundaryEvent", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(Paths.get("./src/test/it/advanced/src/main/resources/callActivitySignal.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "callActivitySignal";
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
