package org.camunda.community.bpmndt.api;

import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.community.bpmndt.api.cfg.AbstractConfiguration;
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringTestConfiguration extends AbstractConfiguration {

  @Override
  public void afterPropertiesSet() throws Exception {
    // ensure that no process engine with the same name exists
    // to avoid conflicts with other tests
    ProcessEngines.destroy();

    super.afterPropertiesSet();
  }

  @Override
  protected List<ProcessEnginePlugin> getProcessEnginePlugins() {
    return Collections.singletonList(new SpinProcessEnginePlugin());
  }
}
