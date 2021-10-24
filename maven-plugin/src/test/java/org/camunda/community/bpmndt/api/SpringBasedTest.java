package org.camunda.community.bpmndt.api;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = SpringBasedTest.TestConfiguration.class)
public class SpringBasedTest {

  @Rule
  public TestCase tc = new TestCase();

  @Test
  public void testExecute() {
    assertThat(tc.createExecutor().execute(), notNullValue());
  }

  /**
   * Tests that process engine plugins, that are provided by overriding the
   * {@code getProcessEnginePlugins} method, are registered at the process engine.
   */
  @Test
  public void testConfiguration() {
    ProcessEngine processEngine = tc.getProcessEngine();
    assertThat(processEngine, notNullValue());

    ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    assertThat(configuration.getProcessEnginePlugins(), hasSize(2));
    assertThat(configuration.getProcessEnginePlugins().get(0), instanceOf(SpinProcessEnginePlugin.class));
    assertThat(configuration.getProcessEnginePlugins().get(1), instanceOf(BpmndtProcessEnginePlugin.class));
  }

  private class TestCase extends AbstractJUnit4SpringBasedTestRule {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi, notNullValue());

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(Paths.get("./src/test/it/simple/src/main/resources/simple.bpmn"));
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
  }

  @Configuration
  public static class TestConfiguration extends SpringConfiguration {

    @Override
    protected List<ProcessEnginePlugin> getProcessEnginePlugins() {
      return Collections.singletonList(new SpinProcessEnginePlugin());
    }
  }
}
