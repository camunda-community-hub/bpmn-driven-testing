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
import org.example.app.ExampleApp;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import generated.advancedspringboot.TC_startEvent__endEvent;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ExampleApp.class, AdvancedTestConfiguration.class}, webEnvironment = WebEnvironment.NONE)
public class AdvancedTest {

  @Rule
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
