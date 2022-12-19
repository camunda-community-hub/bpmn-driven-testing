package org.camunda.community.bpmndt.api.cfg;

import static com.google.common.truth.Truth.assertThat;

import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    SpringConfiguration.class,
    AbstractConfigurationTest.ProcessEnginePluginConfiguration.class
})
public class AbstractConfigurationTest {

  @Autowired(required = false)
  private ProcessEngine processEngine;

  /**
   * Tests that process engine plugins, that are provided via the application context, are registered
   * at the process engine.
   */
  @Test
  public void testConfiguration() {
    assertThat(processEngine).isNotNull();

    ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    assertThat(configuration.getProcessEnginePlugins()).hasSize(2);
    assertThat(configuration.getProcessEnginePlugins().get(0)).isInstanceOf(SpinProcessEnginePlugin.class);
    assertThat(configuration.getProcessEnginePlugins().get(1)).isInstanceOf(BpmndtProcessEnginePlugin.class);
  }
  
  @Configuration
  public static class ProcessEnginePluginConfiguration {

    @Bean
    public List<ProcessEnginePlugin> getProcessEnginePlugins() {
      return Collections.singletonList(new SpinProcessEnginePlugin());
    }
  }
}
