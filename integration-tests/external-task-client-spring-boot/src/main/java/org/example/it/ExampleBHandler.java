package org.example.it;

import static com.google.common.truth.Truth.assertThat;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
@ExternalTaskSubscription(value = "example-b", includeExtensionProperties = true, localVariables = true)
public class ExampleBHandler implements ExternalTaskHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExampleBHandler.class);

  @Override
  public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
    LOGGER.info("B: Executing external task {}", externalTask.getId());

    assertThat(externalTask.getAllVariables()).doesNotContainKey("a");
    assertThat(externalTask.getAllVariables()).doesNotContainKey("b");
    assertThat(externalTask.getAllVariables()).doesNotContainKey("c");

    assertThat(externalTask.getAllVariables()).containsEntry("kB", "vB");

    assertThat(externalTask.getExtensionProperties()).isNotEmpty();
    assertThat(externalTask.getExtensionProperty("x")).isEqualTo("y");

    externalTaskService.complete(externalTask);

    LOGGER.info("B: Completed external task {}", externalTask.getId());
  }
}
