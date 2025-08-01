package org.example.it;

import org.camunda.community.bpmndt.api.cfg.BpmndtProcessEnginePlugin;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class BpmndtConfiguration {

  @Bean
  public BpmndtProcessEnginePlugin bpmndtProcessEnginePlugin() {
    return new BpmndtProcessEnginePlugin();
  }
}
