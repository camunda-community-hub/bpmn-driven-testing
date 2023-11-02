package org.example.it;

import java.util.List;

import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.community.process_test_coverage.engine.platform7.ProcessCoverageConfigurator;
import org.camunda.community.process_test_coverage.spring_test.platform7.ProcessEngineCoverageProperties;
import org.springframework.context.annotation.Bean;

import generated.BpmndtConfiguration;

/**
 * Extends to generated configuration by adding another process engine plugin that configures the
 * engine for collecting process test coverage and provides a
 * {@code ProcessEngineCoverageProperties} bean that is required for the automatically registered
 * {@code ProcessEngineCoverageTestExecutionListener}.
 */
public class CoverageConfiguration extends BpmndtConfiguration {

  @Override
  protected List<ProcessEnginePlugin> getProcessEnginePlugins() {
    List<ProcessEnginePlugin> processEnginePlugins = super.getProcessEnginePlugins();
    processEnginePlugins.add(new CoveragePlugin());

    return processEnginePlugins;
  }

  @Bean
  public ProcessEngineCoverageProperties processEngineCoverageProperties() {
    return ProcessEngineCoverageProperties.builder()
        .assertClassCoverageAtLeast(0.75)
        .build();
  }

  private static class CoveragePlugin extends AbstractProcessEnginePlugin {

    @Override
    public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
      ProcessCoverageConfigurator.initializeProcessCoverageExtensions(processEngineConfiguration);
    }
  }
}
