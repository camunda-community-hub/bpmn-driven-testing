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

public class CallActivityVariablesTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  private CallActivityHandler handler;

  @BeforeEach
  public void setUp() {
    handler = new CallActivityHandler(tc, "callActivity");
  }

  @Test
  public void testExecute() {
    handler.verify((pi, callActivity) -> {
      pi.variables().containsEntry("x", 1);

      assertThat(callActivity.hasInputs()).isTrue();
      assertThat(callActivity.hasOutputs()).isTrue();
    }).verifyInput(variables -> {
      assertThat(variables.hasVariable("y")).isTrue();
      assertThat(variables.getVariable("y")).isEqualTo(1);

      variables.setVariable("y", 2);
    }).verifyOutput(variables -> {
      assertThat(variables.hasVariable("z")).isTrue();
      assertThat(variables.getVariable("z")).isEqualTo(2);
    });

    tc.createExecutor()
        .withVariable("x", 1)
        .execute();
  }

  private static class TestCase extends AbstractJUnit5TestCase<TestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi).isNotNull();

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("callActivity");

      ProcessEngineTests.execute(ProcessEngineTests.job());

      piAssert.hasPassed("callActivity", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.advanced("callActivityVariables.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "callActivityVariables";
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
