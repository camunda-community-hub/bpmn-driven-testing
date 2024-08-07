package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;
import static org.camunda.community.bpmndt.api.TestCaseInstance.PROCESS_ENGINE_NAME;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Optional;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SpringBasedNoProcessEngineTest.TestConfiguration.class)
public class SpringBasedNoProcessEngineTest {

  @BeforeEach
  public void setUp() {
    // make test independent
    Optional.ofNullable(ProcessEngines.getProcessEngine(PROCESS_ENGINE_NAME)).ifPresent(ProcessEngines::unregister);
  }

  /**
   * Tests if an {@code IllegalStateException} is thrown, when a Spring based test does not provide the desired process engine.
   */
  @Test
  public void testException() {
    IllegalStateException e = assertThrows(IllegalStateException.class, () -> {
      TestCase testCase = new TestCase();
      testCase.testClass = this.getClass();
      testCase.testMethod = SpringBasedNoProcessEngineTest.class.getMethod("testException");

      testCase.beforeEach();
    });

    assertThat(e.getMessage()).contains(PROCESS_ENGINE_NAME);
  }

  private static class TestCase extends AbstractJUnit5TestCase<TestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi).isNotNull();

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.simple("simple.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "simple";
    }

    @Override
    public String getStart() {
      return "startEvent";
    }

    @Override
    public String getEnd() {
      return "endEvent";
    }

    @Override
    protected boolean isSpringEnabled() {
      return true;
    }
  }

  /**
   * Empty test configuration without {@link ProcessEngine}.
   */
  @Configuration
  public static class TestConfiguration {

  }
}
