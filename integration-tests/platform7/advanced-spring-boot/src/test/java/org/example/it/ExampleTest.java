package org.example.it;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import java.util.Optional;

import org.camunda.bpm.engine.impl.cfg.CompositeProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.community.bpmndt.api.cfg.BpmndtProcessEnginePlugin;
import org.example.ExampleApp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import generated.advancedspringboot.TC_startEvent__endEvent;

@SpringBootTest(classes = {ExampleApp.class, BpmndtConfiguration.class}, webEnvironment = WebEnvironment.NONE)
public class ExampleTest {

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
    assertThat(configuration.getProcessEnginePlugins(), hasSize(1));
    assertThat(configuration.getProcessEnginePlugins().get(0), instanceOf(CompositeProcessEnginePlugin.class));

    CompositeProcessEnginePlugin compositePlugin = (CompositeProcessEnginePlugin) configuration.getProcessEnginePlugins().get(0);

    Optional<ProcessEnginePlugin> foundPlugin = compositePlugin.getPlugins().stream()
        .filter(BpmndtProcessEnginePlugin.class::isInstance)
        .findFirst();

    assertThat(foundPlugin.isPresent(), is(true));
  }
}
