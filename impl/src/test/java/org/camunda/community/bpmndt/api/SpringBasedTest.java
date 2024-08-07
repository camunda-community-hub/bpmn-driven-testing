package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.community.bpmndt.api.cfg.BpmndtProcessEnginePlugin;
import org.camunda.community.bpmndt.api.cfg.SpringConfiguration;
import org.camunda.community.bpmndt.test.TestPaths;
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SpringBasedTest.TestConfiguration.class)
public class SpringBasedTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  @Test
  public void testExecute() {
    assertThat(tc.createExecutor().execute()).isNotNull();
  }

  /**
   * Tests that process engine plugins, that are provided by overriding the {@code getProcessEnginePlugins} method, are registered at the process engine.
   */
  @Test
  public void testConfiguration() {
    ProcessEngine processEngine = tc.getProcessEngine();
    assertThat(processEngine).isNotNull();

    ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    assertThat(configuration.getProcessEnginePlugins()).hasSize(2);
    assertThat(configuration.getProcessEnginePlugins().get(0)).isInstanceOf(SpinProcessEnginePlugin.class);
    assertThat(configuration.getProcessEnginePlugins().get(1)).isInstanceOf(BpmndtProcessEnginePlugin.class);
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

  @Configuration
  public static class TestConfiguration extends SpringConfiguration {

    @Override
    protected List<ProcessEnginePlugin> getProcessEnginePlugins() {
      return Collections.singletonList(new SpinProcessEnginePlugin());
    }
  }
}
