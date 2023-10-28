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

public class ExternalTaskInputOutputTest {

  @RegisterExtension
  public TestCase tc = new TestCase();

  private ExternalTaskHandler handler;

  @BeforeEach
  public void setUp() {
    handler = new ExternalTaskHandler(tc.getProcessEngine(), "externalTask", "test-topic");
  }

  @Test
  public void testVerifyTask() {
    handler.verify((pi, topicName) -> {
      pi.variables().containsEntry("i", 10L);

      assertThat(topicName).isEqualTo("test-topic");
    });

    handler.verifyTask((externalTaskAssert, localVariables) -> {
      externalTaskAssert.hasTopicName("test-topic");

      assertThat(localVariables).containsEntry("increment", 11L);
    });

    tc.createExecutor()
        .withVariable("i", 10L)
        .verify(pi -> {
          pi.isEnded();

          pi.variables().containsEntry("decrement", 10L);
        })
        .execute();
  }

  private class TestCase extends AbstractJUnit5TestCase<TestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi).isNotNull();

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("externalTask");

      instance.apply(handler);

      piAssert.hasPassed("externalTask", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.advanced("externalTaskInputOutput.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "externalTaskInputOutput";
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
