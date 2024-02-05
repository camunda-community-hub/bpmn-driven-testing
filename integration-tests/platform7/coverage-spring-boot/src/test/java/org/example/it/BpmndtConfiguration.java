package org.example.it;

import org.camunda.community.bpmndt.api.cfg.BpmndtProcessEnginePlugin;
import org.camunda.community.process_test_coverage.spring_test.platform7.ProcessEngineCoverageProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class BpmndtConfiguration {

  @Bean
  public BpmndtProcessEnginePlugin bpmndtProcessEnginePlugin() {
    return new BpmndtProcessEnginePlugin();
  }

  // override default coverage properties, if needed
  @Bean
  public ProcessEngineCoverageProperties processEngineCoverageProperties() {
    return ProcessEngineCoverageProperties.builder()
        .assertClassCoverageAtLeast(0.75)
        .build();
  }
}
