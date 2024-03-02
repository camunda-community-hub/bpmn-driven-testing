package org.camunda.community.bpmndt.platform8.api;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceElement.JobElement;
import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceMemo.JobMemo;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;

/**
 * Fluent API to handle jobs that are handled via {@code JobHandler} by worker.
 */
public class JobHandler {

  private final JobElement element;

  private final Map<String, Object> variableMap = new HashMap<>();

  private Consumer<ProcessInstanceAssert> verifier;
  private io.camunda.zeebe.client.api.worker.JobHandler action;
  private Object variables;

  private String expectedEvaluatedType;
  private Consumer<String> expectedEvaluatedTypeConsumer;
  private String expectedType;
  private Consumer<String> expectedTypeConsumer;

  JobHandler(JobElement element) {
    this.element = element;
  }

  void apply(TestCaseInstance instance, ProcessInstanceEvent processInstanceEvent) {
    if (verifier != null) {
      verifier.accept(BpmnAssert.assertThat(processInstanceEvent));
    }

    if (expectedType != null && !expectedType.equals(element.getType())) {
      String message = "expected job %s to be of type '%s', but was '%s'";
      throw new AssertionError(String.format(message, element.getId(), expectedType, element.getType()));
    }
    if (expectedTypeConsumer != null) {
      expectedTypeConsumer.accept(element.getType());
    }

    JobMemo job = instance.getJob(processInstanceEvent, element.getId());
    if (expectedEvaluatedType != null && !expectedEvaluatedType.equals(job.type)) {
      String message = "expected job %s to be of evaluated type '%s', but was '%s'";
      throw new AssertionError(String.format(message, element.getId(), expectedEvaluatedType, job.type));
    }
    if (expectedEvaluatedTypeConsumer != null) {
      expectedEvaluatedTypeConsumer.accept(job.type);
    }

    if (action != null) {
      try (JobWorker worker = instance.client.newWorker().jobType(job.type).handler(action).open()) {
        instance.hasPassed(processInstanceEvent, element.getId());
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

  void execute(JobClient client, ActivatedJob job) {
    if (variables != null) {
      client.newCompleteCommand(job).variables(variables).send();
    } else {
      client.newCompleteCommand(job).variables(variableMap).send();
    }
  }

  /**
   * Verifies the job's waiting state.
   *
   * @param verifier Verifier that accepts an {@link ProcessInstanceAssert} instance.
   * @return The handler.
   */
  public JobHandler verify(Consumer<ProcessInstanceAssert> verifier) {
    this.verifier = verifier;
    return this;
  }

  /**
   * Verifies that the job is of the given evaluated type (an evaluated FEEL expression specified in the task definition).
   *
   * @param expectedEvaluatedType The expected type.
   * @return The handler.
   */
  public JobHandler verifyEvaluatedType(String expectedEvaluatedType) {
    this.expectedEvaluatedType = expectedEvaluatedType;
    return this;
  }

  /**
   * Verifies that the job is of the given evaluated type (an evaluated FEEL expression specified in the task definition), using a consumer function.
   *
   * @param expectedEvaluatedTypeConsumer A consumer asserting the evaluated type.
   * @return The handler.
   */
  public JobHandler verifyEvaluatedType(Consumer<String> expectedEvaluatedTypeConsumer) {
    this.expectedEvaluatedTypeConsumer = expectedEvaluatedTypeConsumer;
    return this;
  }

  /**
   * Verifies that the job is of the given type (a static value or FEEL expression specified in the task definition).
   *
   * @param expectedType The expected type.
   * @return The handler.
   */
  public JobHandler verifyType(String expectedType) {
    this.expectedType = expectedType;
    return this;
  }

  /**
   * Verifies that the job is of the given type (a static value or FEEL expression specified in the task definition), using a consumer function.
   *
   * @param expectedTypeConsumer A consumer asserting the type.
   * @return The handler.
   */
  public JobHandler verifyType(Consumer<String> expectedTypeConsumer) {
    this.expectedTypeConsumer = expectedTypeConsumer;
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
   * @return The handler.
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
   * @return The handler.
   * @see #execute()
   */
  public JobHandler withVariableMap(Map<String, Object> variableMap) {
    if (variables != null) {
      throw new IllegalStateException("either use an object (POJO) as variables or a variable map");
    }
    this.variableMap.putAll(variableMap);
    return this;
  }
}
