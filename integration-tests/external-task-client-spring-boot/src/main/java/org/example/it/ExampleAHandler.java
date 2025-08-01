package org.example.it;

import static com.google.common.truth.Truth.assertThat;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.variable.value.BooleanValue;
import org.camunda.bpm.engine.variable.value.IntegerValue;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
@ExternalTaskSubscription("example-a")
public class ExampleAHandler implements ExternalTaskHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExampleAHandler.class);

  @Override
  public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
    LOGGER.info("A: Executing external task {}", externalTask.getId());

    assertThat(externalTask.getAllVariables()).containsKey("a");
    assertThat(externalTask.getAllVariablesTyped()).containsKey("a");
    assertThat(externalTask.getAllVariablesTyped(false)).containsKey("a");
    assertThat(externalTask.getAllVariables()).containsKey("b");
    assertThat(externalTask.getAllVariablesTyped()).containsKey("b");
    assertThat(externalTask.getAllVariablesTyped(false)).containsKey("b");
    assertThat(externalTask.getAllVariables()).containsKey("c");
    assertThat(externalTask.getAllVariablesTyped()).containsKey("c");
    assertThat(externalTask.getAllVariablesTyped(false)).containsKey("c");

    assertThat((String) externalTask.getVariable("a")).isEqualTo("text");
    assertThat(((StringValue) externalTask.getVariableTyped("a")).getValue()).isEqualTo("text");
    assertThat(((StringValue) externalTask.getVariableTyped("a", false)).getValue()).isEqualTo("text");
    assertThat((Integer) externalTask.getVariable("b")).isEqualTo(1);
    assertThat(((IntegerValue) externalTask.getVariableTyped("b")).getValue()).isEqualTo(1);
    assertThat(((IntegerValue) externalTask.getVariableTyped("b", false)).getValue()).isEqualTo(1);
    assertThat((Boolean) externalTask.getVariable("c")).isTrue();
    assertThat(((BooleanValue) externalTask.getVariableTyped("c")).getValue()).isTrue();
    assertThat(((BooleanValue) externalTask.getVariableTyped("c", false)).getValue()).isTrue();

    assertThat(externalTask.getAllVariables()).containsEntry("kA", "vA");

    assertThat(externalTask.getExtensionProperties()).isEmpty();

    externalTaskService.complete(externalTask);

    LOGGER.info("A: Completed external task {}", externalTask.getId());
  }
}
