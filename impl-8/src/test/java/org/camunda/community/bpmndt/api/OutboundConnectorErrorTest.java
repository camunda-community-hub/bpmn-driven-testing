package org.camunda.community.bpmndt.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class OutboundConnectorErrorTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;

  private OutboundConnectorHandler handler;

  @BeforeEach
  void setUp() {
    handler = new OutboundConnectorHandler("outboundConnector");
  }

  @Test
  void testThrowBpmnError() {
    handler.throwBpmnError("ADVANCED_ERROR", "test error message");

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testThrowBpmnErrorWithVariables() {
    handler
        .withVariable("x", "test")
        .withVariable("y", 1)
        .withVariable("z", true)
        .throwBpmnError("ADVANCED_ERROR", "test error message");

    tc.createExecutor(engine).verify(piAssert -> {
      piAssert.hasVariableWithValue("x", "test");
      piAssert.hasVariableWithValue("y", 1);
      piAssert.hasVariableWithValue("z", true);

      // TODO map error message via FEEL to verify throw error command included the specified error message
      // but it seems that it is currently not possible!?
      // piAssert.hasVariableWithValue("errorMessage", "test error message");

      piAssert.isCompleted();
    }).execute();
  }

  @Test
  void testExecuteAction() {
    handler.execute((client, jobKey) -> client.newThrowErrorCommand(jobKey).errorCode("ADVANCED_ERROR").send());

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "startEvent");
      instance.isWaitingAt(processInstanceKey, "outboundConnector");
      instance.apply(processInstanceKey, handler);
      instance.hasTerminated(processInstanceKey, "outboundConnector");
      instance.hasPassed(processInstanceKey, "errorBoundaryEvent");
      instance.hasPassed(processInstanceKey, "endEvent");
      instance.isCompleted(processInstanceKey);
    }

    @Override
    public String getBpmnProcessId() {
      return "outboundConnectorError";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.advanced("outboundConnectorError.bpmn"));
      } catch (IOException e) {
        return null;
      }
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
