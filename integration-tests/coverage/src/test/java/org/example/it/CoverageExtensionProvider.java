package org.example.it;

import java.util.LinkedList;
import java.util.List;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.community.bpmndt.api.cfg.BpmndtProcessEnginePlugin;
import org.camunda.community.process_test_coverage.engine.platform7.ProcessCoverageConfigurator;
import org.camunda.community.process_test_coverage.junit5.platform7.ProcessEngineCoverageExtension;

public class CoverageExtensionProvider {

  private static final ProcessEngineCoverageExtension extension;

  static {
    // configure engine for running generated test cases
    List<ProcessEnginePlugin> processEnginePlugins = new LinkedList<>();
    processEnginePlugins.add(new BpmndtProcessEnginePlugin());

    ProcessEngineConfigurationImpl processEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    processEngineConfiguration.setProcessEnginePlugins(processEnginePlugins);

    // configure engine for collecting process test coverage
    ProcessCoverageConfigurator.initializeProcessCoverageExtensions(processEngineConfiguration);

    extension = ProcessEngineCoverageExtension.builder(processEngineConfiguration)
        .assertClassCoverageAtLeast(0.75)
        .build();
  }

  /**
   * Provides a cached instance of the JUnit5 coverage extension, so that the same process engine is
   * used for all tests.
   * 
   * @return The cached coverage extension.
   */
  public static ProcessEngineCoverageExtension get() {
    return extension;
  }
}
