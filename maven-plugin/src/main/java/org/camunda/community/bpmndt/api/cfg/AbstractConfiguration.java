package org.camunda.community.bpmndt.api.cfg;

import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.spring.SpringExpressionManager;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.community.bpmndt.api.TestCaseInstance;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Abstract Spring test configuration, used as superclass for the generated configuration.
 */
@Configuration
public abstract class AbstractConfiguration implements InitializingBean {

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

    List<ProcessEnginePlugin> processEnginePlugins = new LinkedList<>();
    processEnginePlugins.addAll(getProcessEnginePlugins());

    // BPMN Driven Testing plugin must be added last
    processEnginePlugins.add(new BpmndtProcessEnginePlugin());

    SpringProcessEngineConfiguration processEngineConfiguration = new SpringProcessEngineConfiguration();
    processEngineConfiguration.setApplicationContext(applicationContext);
    processEngineConfiguration.setDataSource(dataSource);
    processEngineConfiguration.setExpressionManager(new SpringExpressionManager(applicationContext, null));
    processEngineConfiguration.setProcessEngineName(TestCaseInstance.PROCESS_ENGINE_NAME);
    processEngineConfiguration.setProcessEnginePlugins(processEnginePlugins);
    processEngineConfiguration.setTransactionManager(transactionManager);

    processEngine = processEngineConfiguration.buildProcessEngine();
  }

  @Bean
  public ProcessEngine getProcessEngine() {
    return processEngine;
  }

  /**
   * Returns a list of process engine plugins that are registered at the process engine.
   * 
   * @return A list of process engine plugins.
   */
  protected abstract List<ProcessEnginePlugin> getProcessEnginePlugins();

  protected DataSource initDataSource() {
    if (this.dataSource != null) {
      return this.dataSource;
    }
    
    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setDriverClassName("org.h2.Driver");
    dataSource.setUrl("jdbc:h2:mem:bpmndt;DB_CLOSE_ON_EXIT=FALSE");

    return dataSource;
  }

  protected PlatformTransactionManager initTransactionManager(DataSource dataSource) {
    if (this.transactionManager != null) {
      return this.transactionManager;
    }

    return new DataSourceTransactionManager(dataSource);
  }
}
