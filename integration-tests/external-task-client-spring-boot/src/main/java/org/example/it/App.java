package org.example.it;

import static com.google.common.truth.Truth.assertThat;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.h2.Driver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.transaction.PlatformTransactionManager;

@SpringBootApplication
public class App {

  public static void main(String[] args) {
    try (ConfigurableApplicationContext ctx = SpringApplication.run(App.class, args)) {
      runProcessInstance(ctx);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static void runProcessInstance(ApplicationContext ctx) {
    var runtimeService = ctx.getBean(RuntimeService.class);

    var pi = runtimeService.startProcessInstanceByKey("example", Map.of("a", "text", "b", 1, "c", true));

    try {
      TimeUnit.SECONDS.sleep(10L);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    var historyService = ctx.getBean(HistoryService.class);

    var piState = historyService.createHistoricProcessInstanceQuery()
        .processInstanceId(pi.getId())
        .singleResult()
        .getState();

    assertThat(piState).isEqualTo("COMPLETED");
  }

  @Bean
  public DataSource dataSource() {
    var dataSource = new SimpleDriverDataSource();
    dataSource.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
    dataSource.setDriverClass(Driver.class);
    dataSource.setUsername("sa");
    dataSource.setPassword("pw");

    return dataSource;
  }

  @Bean
  public PlatformTransactionManager platformTransactionManager(DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }
}
