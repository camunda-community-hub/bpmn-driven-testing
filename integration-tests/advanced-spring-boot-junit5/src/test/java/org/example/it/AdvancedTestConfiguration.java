package org.example.it;

import org.camunda.community.bpmndt.api.cfg.BpmndtProcessEnginePlugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdvancedTestConfiguration {

  @Bean
  public BpmndtProcessEnginePlugin bpmndtProcessEnginePlugin() {
    return new BpmndtProcessEnginePlugin();
  }
}
