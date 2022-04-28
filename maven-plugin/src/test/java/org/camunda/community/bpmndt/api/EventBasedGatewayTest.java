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

public class EventBasedGatewayTest {

  @Rule
  public TestCase tc = new TestCase();
  @Rule
  public MessageTestCase tcMessage = new MessageTestCase();
  @Rule
  public TimerTestCase tcTimer = new TimerTestCase();

  private EventHandler messageCatchEventHandler;
  private JobHandler timerCatchEventHandler;

  @Before
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

  private class TestCase extends AbstractJUnit4TestCase {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi, notNullValue());

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("eventBasedGateway");
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(Paths.get("./src/test/it/simple/src/main/resources/simpleEventBasedGateway.bpmn"));
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

  private class MessageTestCase extends AbstractJUnit4TestCase {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi, notNullValue());

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("eventBasedGateway");

      instance.apply(messageCatchEventHandler);

      piAssert.hasPassed("messageCatchEvent", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(Paths.get("./src/test/it/simple/src/main/resources/simpleEventBasedGateway.bpmn"));
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

  private class TimerTestCase extends AbstractJUnit4TestCase {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi, notNullValue());

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("eventBasedGateway");

      instance.apply(timerCatchEventHandler);

      piAssert.hasPassed("timerCatchEvent", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(Paths.get("./src/test/it/simple/src/main/resources/simpleEventBasedGateway.bpmn"));
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
