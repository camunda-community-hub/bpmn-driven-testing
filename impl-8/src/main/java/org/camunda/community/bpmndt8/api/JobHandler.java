package org.camunda.community.bpmndt8.api;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;

/**
 * Fluent API to handle jobs that are handled via {@code JobHandler} by worker.
 */
public class JobHandler {

  private final String bpmnElementId;
  private final String type;

  private final Map<String, Object> variableMap = new HashMap<>();

  private io.camunda.zeebe.client.api.worker.JobHandler action;
  private String expectedEvaluatedType;
  private String expectedType;
  private Object variables;

  JobHandler(String bpmnElementId, String type) {
    this.bpmnElementId = bpmnElementId;
    this.type = type;
  }

  void apply(TestCaseInstance instance, long processInstanceKey) {
    if (expectedType != null && !expectedType.equals(type)) {
      throw new AssertionError("expected job %s to be of type %s, but was %s".formatted(bpmnElementId, expectedType, type));
    }

    var evaluatedType = instance.getJobType(processInstanceKey, bpmnElementId);
    if (expectedEvaluatedType != null && !expectedEvaluatedType.equals(evaluatedType)) {
      throw new AssertionError("expected job %s to be of evaluated type %s, but was %s".formatted(bpmnElementId, expectedEvaluatedType, evaluatedType));
    }

    if (action != null) {
      try (var ignored = instance.client.newWorker().jobType(evaluatedType).handler(action).open()) {
        instance.hasPassed(processInstanceKey, bpmnElementId);
      }
    }
  }

  /**
   * Customizes the handler, using the given {@link Consumer} function. This method can be used to apply a common customization needed for different test
   * cases.
   *
   * <pre>
   * tc.handleJob().customize(this::prepareJob);
   * </pre>
   *
   * @param customizer A function that accepts a {@link JobHandler}.
   * @return The handler.
   */
  public JobHandler customize(Consumer<JobHandler> customizer) {
    if (customizer != null) {
      customizer.accept(this);
    }
    return this;
  }

  /**
   * Executes an action that completes the job, when the process instance is waiting at the corresponding element, using specified variables.
   *
   * @see #withVariable(String, Object)
   * @see #withVariables(Object)
   * @see #withVariableMap(Map)
   */
  public void execute() {
    action = this::execute;
  }

  /**
   * Executes a custom action that handles the job, when the process instance is waiting at the corresponding element.
   *
   * @param action A specific action that accepts the related {@link JobClient} and {@link ActivatedJob} - a Zeebe job handler.
   */
  public void execute(io.camunda.zeebe.client.api.worker.JobHandler action) {
    this.action = action;
  }

  /**
   * Verifies that the job is of the given evaluated type.
   *
   * @param expectedEvaluatedType The expected type - an evaluated FEEL expression specified in the task definition.
   * @return The handler.
   */
  public JobHandler verifyEvaluatedType(String expectedEvaluatedType) {
    this.expectedEvaluatedType = expectedEvaluatedType;
    return this;
  }

  /**
   * Verifies that the job is of the given type.
   *
   * @param expectedType The expected type - a static value or FEEL expression specified in the task definition.
   * @return The handler.
   */
  public JobHandler verifyType(String expectedType) {
    this.expectedType = expectedType;
    return this;
  }

  /**
   * Sets a variable that is used to complete the job.
   *
   * @param name  The name of the variable.
   * @param value The variable's value.
   * @return The handler.
   * @see #execute()
   */
  public JobHandler withVariable(String name, Object value) {
    if (variables != null) {
      throw new IllegalStateException("either use an object (POJO) as variables or a variable map");
    }
    variableMap.put(name, value);
    return this;
  }

  /**
   * Sets an object as variables that is used to complete the job.
   *
   * @param variables The variables as POJO.
   * @return The executor.
   * @see #execute()
   */
  public JobHandler withVariables(Object variables) {
    if (!variableMap.isEmpty()) {
      throw new IllegalStateException("either use an object (POJO) as variables or a variable map");
    }
    this.variables = variables;
    return this;
  }

  /**
   * Sets variables that are used to complete the job.
   *
   * @param variableMap A map of variables.
   * @return The executor.
   * @see #execute()
   */
  public JobHandler withVariableMap(Map<String, Object> variableMap) {
    if (variables != null) {
      throw new IllegalStateException("either use an object (POJO) as variables or a variable map");
    }
    this.variableMap.putAll(variableMap);
    return this;
  }

  void execute(JobClient client, ActivatedJob job) {
    if (variables != null) {
      client.newCompleteCommand(job).variables(variables).send();
    } else {
      client.newCompleteCommand(job).variables(variableMap).send();
    }
  }
}
