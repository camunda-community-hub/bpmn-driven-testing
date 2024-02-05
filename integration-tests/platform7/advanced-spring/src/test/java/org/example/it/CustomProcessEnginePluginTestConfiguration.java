package org.example.it;

import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "org.example.delegate")
public class CustomProcessEnginePluginTestConfiguration {

  @Bean
  public List<ProcessEnginePlugin> getProcessEnginePlugins() {
    return Collections.singletonList(new CustomProcessEnginePlugin());
  }

  public static class CustomProcessEnginePlugin extends AbstractProcessEnginePlugin {
  }
}
