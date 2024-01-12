package org.example.it;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.community.bpmndt.api.cfg.BpmndtProcessEnginePlugin;
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import generated.BpmndtConfiguration;
import generated.advancedspring.TC_startEvent__endEvent;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {BpmndtConfiguration.class, AdvancedTestConfiguration.class})
public class AdvancedTest {

  @RegisterExtension
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.createExecutor().verify(pi -> {
      pi.isEnded();
    }).execute();
  }

  @Test
  public void testConfiguration() {
    ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl) tc.getProcessEngine().getProcessEngineConfiguration();
    assertThat(configuration.getProcessEnginePlugins(), hasSize(2));
    assertThat(configuration.getProcessEnginePlugins().get(0), instanceOf(SpinProcessEnginePlugin.class));
    assertThat(configuration.getProcessEnginePlugins().get(1), instanceOf(BpmndtProcessEnginePlugin.class));
  }
}
