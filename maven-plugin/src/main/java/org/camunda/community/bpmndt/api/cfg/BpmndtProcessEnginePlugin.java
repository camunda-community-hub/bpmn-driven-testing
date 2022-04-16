package org.camunda.community.bpmndt.api.cfg;

import static org.camunda.community.bpmndt.api.TestCaseInstance.PROCESS_ENGINE_NAME;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.h2.engine.Constants;

/**
 * Plugin to configure a BPMN Driven Testing conform process engine, used to execute generated test
 * cases.
 */
public class BpmndtProcessEnginePlugin extends AbstractProcessEnginePlugin {

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    List<BpmnParseListener> postParseListeners = processEngineConfiguration.getCustomPostBPMNParseListeners();
    if (postParseListeners == null) {
      postParseListeners = new LinkedList<>();
    } else {
      // must be added to a new list, since the provided list may not allow modifications
      postParseListeners = new LinkedList<>(postParseListeners);
    }

    postParseListeners.add(new BpmndtParseListener());

    processEngineConfiguration.setCmmnEnabled(false);
    processEngineConfiguration.setCustomPostBPMNParseListeners(postParseListeners);
    processEngineConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_CREATE_DROP);
    processEngineConfiguration.setHistoryLevel(HistoryLevel.HISTORY_LEVEL_FULL);
    processEngineConfiguration.setInitializeTelemetry(false);
    processEngineConfiguration.setJobExecutorActivate(false);
    processEngineConfiguration.setMetricsEnabled(false);
    processEngineConfiguration.setProcessEngineName(PROCESS_ENGINE_NAME);

    if (processEngineConfiguration.getDataSource() == null) {
      // use random database name to avoid SQL errors during schema create/drop
      String url = String.format("jdbc:h2:mem:bpmndt-%s", UUID.randomUUID().toString());

      processEngineConfiguration.setJdbcUrl(url);
    }

    if (getH2MajorVersion() > 1) {
      // ensure H2 version 2 compatibility
      DbSqlSessionFactory.databaseSpecificTrueConstant.put("h2", "true");
      DbSqlSessionFactory.databaseSpecificFalseConstant.put("h2", "false");
      DbSqlSessionFactory.databaseSpecificBitAnd2.put("h2", ",CAST(");
      DbSqlSessionFactory.databaseSpecificBitAnd3.put("h2", " AS BIGINT))");
    }
  }

  private int getH2MajorVersion() {
    return Constants.VERSION_MAJOR;
  }
}
