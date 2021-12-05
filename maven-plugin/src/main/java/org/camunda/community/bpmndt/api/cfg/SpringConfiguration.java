package org.camunda.community.bpmndt.api.cfg;

import static org.camunda.community.bpmndt.api.TestCaseInstance.PROCESS_ENGINE_NAME;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.spring.SpringExpressionManager;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring test configuration, used as superclass for the generated configuration.<br>
 * If data source and/or transaction manager are not provided by the application context, this
 * configuration will initialize and provide them to the process engine configuration.<br>
 * Moreover, if the application context provides a list of process engine plugins, this list will be
 * preferred in favor of the process engine plugins that are configured on the Maven plugin
 * execution - see parameter {@code processEnginePlugins}.
 */
@Configuration
public class SpringConfiguration implements InitializingBean {

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired(required = false)
  private DataSource dataSource;
  @Autowired(required = false)
  private PlatformTransactionManager transactionManager;

  @Autowired(required = false)
  private List<ProcessEnginePlugin> processEnginePlugins;

  private ProcessEngine processEngine;

  @Override
  public void afterPropertiesSet() throws Exception {
    DataSource dataSource = initDataSource();
    PlatformTransactionManager transactionManager = initTransactionManager(dataSource);

    List<ProcessEnginePlugin> processEnginePlugins = initProcessEnginePlugins();
    // BPMN Driven Testing plugin must be added at last
    processEnginePlugins.add(new BpmndtProcessEnginePlugin());

    SpringProcessEngineConfiguration processEngineConfiguration = new SpringProcessEngineConfiguration();
    processEngineConfiguration.setApplicationContext(applicationContext);
    processEngineConfiguration.setDataSource(dataSource);
    processEngineConfiguration.setExpressionManager(new SpringExpressionManager(applicationContext, null));
    processEngineConfiguration.setProcessEngineName(PROCESS_ENGINE_NAME);
    processEngineConfiguration.setProcessEnginePlugins(processEnginePlugins);
    processEngineConfiguration.setTransactionManager(transactionManager);

    processEngine = processEngineConfiguration.buildProcessEngine();
  }

  @Bean
  public ProcessEngine getProcessEngine() {
    return processEngine;
  }

  /**
   * Returns a list of process engine plugins that are registered at the process engine. The list may
   * be empty, if there are no plugins to register. This method should be overridden by subclasses.
   * 
   * @return A list of process engine plugins.
   */
  protected List<ProcessEnginePlugin> getProcessEnginePlugins() {
    return Collections.emptyList();
  }

  protected DataSource initDataSource() {
    if (this.dataSource != null) {
      return this.dataSource;
    }

    // use random database name to avoid SQL errors during schema create/drop
    String url = String.format("jdbc:h2:mem:bpmndt-%s;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE", UUID.randomUUID().toString());

    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("org.h2.Driver");
    dataSource.setUrl(url);

    return dataSource;
  }

  protected List<ProcessEnginePlugin> initProcessEnginePlugins() {
    // must be added to a new list, since the provided list may not allow modifications
    if (this.processEnginePlugins != null) {
      return new LinkedList<>(this.processEnginePlugins);
    } else {
      return new LinkedList<>(getProcessEnginePlugins());
    }
  }

  protected PlatformTransactionManager initTransactionManager(DataSource dataSource) {
    if (this.transactionManager != null) {
      return this.transactionManager;
    }

    return new DataSourceTransactionManager(dataSource);
  }
}
