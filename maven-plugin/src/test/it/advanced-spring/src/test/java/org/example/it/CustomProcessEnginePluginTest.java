package org.example.it;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.community.bpmndt.api.cfg.BpmndtProcessEnginePlugin;
import org.example.it.CustomProcessEnginePluginTestConfiguration.CustomProcessEnginePlugin;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import generated.BpmndtConfiguration;
import generated.advancedspring.TC_startEvent__endEvent;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BpmndtConfiguration.class, CustomProcessEnginePluginTestConfiguration.class})
public class CustomProcessEnginePluginTest {

  @Rule
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.createExecutor().verify(pi -> {
      pi.isEnded();
    }).execute();
  }

  /**
   * Tests if process engine plugins, provided by the Spring test configuration, replace the ones
   * defined within the Maven plugin execution configuration.
   */
  @Test
  public void testConfiguration() {
    ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl) tc.getProcessEngine().getProcessEngineConfiguration();
    assertThat(configuration.getProcessEnginePlugins(), hasSize(2));
    assertThat(configuration.getProcessEnginePlugins().get(0), instanceOf(CustomProcessEnginePlugin.class));
    assertThat(configuration.getProcessEnginePlugins().get(1), instanceOf(BpmndtProcessEnginePlugin.class));
  }
}
