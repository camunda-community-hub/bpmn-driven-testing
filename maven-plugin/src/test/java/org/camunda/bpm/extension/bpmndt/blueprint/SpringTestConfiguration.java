package org.camunda.bpm.extension.bpmndt.blueprint;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.spring.SpringExpressionManager;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class SpringTestConfiguration implements InitializingBean {

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired(required = false)
  private DataSource dataSource;

  @Autowired(required = false)
  private PlatformTransactionManager transactionManager;

  private ProcessEngine processEngine;

  @Override
  public void afterPropertiesSet() throws Exception {
    DataSource dataSource = initDataSource();
    PlatformTransactionManager transactionManager = initTransactionManager(dataSource);

    SpringProcessEngineConfiguration configuration = new SpringProcessEngineConfiguration();
    configuration.setApplicationContext(applicationContext);
    configuration.setCmmnEnabled(false);
    configuration.setDatabaseSchemaUpdate(ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_CREATE_DROP);
    configuration.setDataSource(dataSource);
    configuration.setDmnEnabled(false);
    configuration.setExpressionManager(new SpringExpressionManager(applicationContext, null));
    configuration.setHistoryLevel(HistoryLevel.HISTORY_LEVEL_FULL);
    configuration.setInitializeTelemetry(false);
    configuration.setJobExecutorActivate(false);
    configuration.setMetricsEnabled(false);
    configuration.setTransactionManager(transactionManager);

    processEngine = configuration.buildProcessEngine();
  }

  protected DataSource initDataSource() {
    if (dataSource != null) {
      return dataSource;
    }

    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setDriverClassName("org.h2.Driver");
    dataSource.setUrl("jdbc:h2:mem:bpmndt;DB_CLOSE_ON_EXIT=FALSE");

    return dataSource;
  }

  protected PlatformTransactionManager initTransactionManager(DataSource dataSource) {
    if (transactionManager != null) {
      return transactionManager;
    }

    return new DataSourceTransactionManager(dataSource);
  }

  @Bean
  public ProcessEngine processEngine() {
    return processEngine;
  }

  @Bean
  public ProcessEngineRule processEngineRule() {
    return new ProcessEngineRule(processEngine, true);
  }
}
