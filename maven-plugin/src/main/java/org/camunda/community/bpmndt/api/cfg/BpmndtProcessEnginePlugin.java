package org.camunda.community.bpmndt.api.cfg;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.HistoryLevel;

/**
 * Plugin to configure a BPMN Driven Testing conform process engine, used to execute generated test
 * cases.
 */
public class BpmndtProcessEnginePlugin extends AbstractProcessEnginePlugin {

  private final boolean springEnabled;

  public BpmndtProcessEnginePlugin() {
    this(false);
  }

  public BpmndtProcessEnginePlugin(boolean springEnabled) {
    this.springEnabled = springEnabled;
  }

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    List<BpmnParseListener> postParseListeners = processEngineConfiguration.getCustomPostBPMNParseListeners();
    if (postParseListeners == null) {
      postParseListeners = new LinkedList<>();

      processEngineConfiguration.setCustomPostBPMNParseListeners(postParseListeners);
    }

    postParseListeners.add(new BpmndtParseListener());

    processEngineConfiguration.setCmmnEnabled(false);
    processEngineConfiguration.setCustomPostBPMNParseListeners(postParseListeners);
    processEngineConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_CREATE_DROP);
    processEngineConfiguration.setHistoryLevel(HistoryLevel.HISTORY_LEVEL_FULL);
    processEngineConfiguration.setInitializeTelemetry(false);
    processEngineConfiguration.setJobExecutorActivate(false);
    processEngineConfiguration.setMetricsEnabled(false);

    if (!springEnabled) {
      // use random database name to avoid SQL errors during schema create/drop
      String url = String.format("jdbc:h2:mem:bpmndt-%s", UUID.randomUUID().toString());

      // if Spring is not enabled, a custom JDBC url is set
      // otherwise a data source is used
      processEngineConfiguration.setJdbcUrl(url);
    }
  }
}
