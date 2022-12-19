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

public class EventBasedGatewayTest {

  @RegisterExtension
  public TestCase tc = new TestCase();
  @RegisterExtension
  public MessageTestCase tcMessage = new MessageTestCase();
  @RegisterExtension
  public TimerTestCase tcTimer = new TimerTestCase();

  private EventHandler messageCatchEventHandler;
  private JobHandler timerCatchEventHandler;

  @BeforeEach
  public void setUp() {
    messageCatchEventHandler = new EventHandler(tcMessage.getProcessEngine(), "messageCatchEvent", "simpleMessage");
    timerCatchEventHandler = new JobHandler(tcTimer.getProcessEngine(), "timerCatchEvent");
  }

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }

  @Test
  public void testExecuteMessage() {
    tcMessage.createExecutor().execute();
  }

  @Test
  public void testExecuteTimer() {
    tcTimer.createExecutor().execute();
  }

  private class TestCase extends AbstractJUnit5TestCase<TestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi).isNotNull();

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("eventBasedGateway");
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.simple("simpleEventBasedGateway.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "simpleEventBasedGateway";
    }

    @Override
    public String getStart() {
      return "startEvent";
    }

    @Override
    public String getEnd() {
      return "eventBasedGateway";
    }
  }

  private class MessageTestCase extends AbstractJUnit5TestCase<MessageTestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi).isNotNull();

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("eventBasedGateway");

      instance.apply(messageCatchEventHandler);

      piAssert.hasPassed("messageCatchEvent", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.simple("simpleEventBasedGateway.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "simpleEventBasedGateway";
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

  private class TimerTestCase extends AbstractJUnit5TestCase<TimerTestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi).isNotNull();

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("eventBasedGateway");

      instance.apply(timerCatchEventHandler);

      piAssert.hasPassed("timerCatchEvent", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.simple("simpleEventBasedGateway.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "simpleEventBasedGateway";
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
