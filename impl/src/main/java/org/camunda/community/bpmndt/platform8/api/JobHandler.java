package org.camunda.community.bpmndt.platform8.api;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceElement.JobElement;
import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceMemo.JobMemo;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.ThrowErrorCommandStep1.ThrowErrorCommandStep2;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;

/**
 * Fluent API to handle jobs that are handled via {@code JobHandler} by worker.
 */
public class JobHandler {

  private final JobElement element;

  private final Map<String, Object> variableMap = new HashMap<>();

  private Consumer<ProcessInstanceAssert> verifier;
  private BiConsumer<ZeebeClient, Long> action;
  private String errorMessage;
  private Object variables;

  private Consumer<String> retriesExpressionConsumer;
  private Consumer<String> typeExpressionConsumer;

  private Integer expectedRetries;
  private String expectedType;

  private Consumer<Integer> retriesConsumer;
  private Consumer<String> typeConsumer;

  JobHandler(JobElement element) {
    this.element = element;
  }

  void apply(TestCaseInstance instance, ProcessInstanceEvent processInstanceEvent) {
    if (verifier != null) {
      verifier.accept(BpmnAssert.assertThat(processInstanceEvent));
    }

    if (retriesExpressionConsumer != null) {
      retriesExpressionConsumer.accept(element.getRetries());
    }
    if (typeExpressionConsumer != null) {
      typeExpressionConsumer.accept(element.getType());
    }

    JobMemo job = instance.getJob(processInstanceEvent, element.getId());

    if (expectedRetries != null && !expectedRetries.equals(job.retries)) {
      String message = "expected job %s to have a retry count of %d, but was %d";
      throw new AssertionError(String.format(message, element.getId(), expectedRetries, job.retries));
    }
    if (retriesConsumer != null) {
      retriesConsumer.accept(job.retries);
    }

    if (expectedType != null && !expectedType.equals(job.type)) {
      String message = "expected job %s to be of type '%s', but was '%s'";
      throw new AssertionError(String.format(message, element.getId(), expectedType, job.type));
    }
    if (typeConsumer != null) {
      typeConsumer.accept(job.type);
    }

    if (action != null) {
      action.accept(instance.client, job.key);

      if (element.getErrorCode() == null) {
        instance.hasPassed(processInstanceEvent, element.getId());
      } else {
        instance.hasTerminated(processInstanceEvent, element.getId());
      }
    }
  }

  /**
   * Customizes the handler, using the given {@link Consumer} function. This method can be used to apply a common customization needed for different test
   * cases.
   *
   * <pre>
   * tc.handleJob().customize(this::prepare);
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
   * @param action A specific action that accepts a {@link ZeebeClient} and the related job key.
   * @see ZeebeClient#newCompleteCommand(long)
   */
  public void execute(BiConsumer<ZeebeClient, Long> action) {
    if (action == null) {
      throw new IllegalArgumentException("action is null");
    }
    this.action = action;
  }

  void execute(ZeebeClient client, long jobKey) {
    if (variables != null) {
      client.newCompleteCommand(jobKey).variables(variables).send().join();
    } else {
      client.newCompleteCommand(jobKey).variables(variableMap).send().join();
    }
  }

  /**
   * Throws an BPMN error using the given error message as well as the specified variables.
   * <p>
   * This action can be used, if there is no related registered job worker yet.
   *
   * @see #withVariable(String, Object)
   * @see #withVariables(Object)
   * @see #withVariableMap(Map)
   */
  public void throwBpmnError(String errorMessage) {
    if (element.getErrorCode() == null) {
      throw new IllegalStateException("the subsequent test case element is not an error boundary event");
    }

    this.errorMessage = errorMessage;
    this.action = this::throwBpmnError;
  }

  void throwBpmnError(ZeebeClient client, long jobKey) {
    ThrowErrorCommandStep2 throwErrorCommandStep2 = client.newThrowErrorCommand(jobKey)
        .errorCode(element.getErrorCode())
        .errorMessage(errorMessage);

    if (variables != null) {
      throwErrorCommandStep2.variables(variables).send();
    } else {
      throwErrorCommandStep2.variables(variableMap).send();
    }
  }

  /**
   * Verifies the job's waiting state.
   * <p>
   * <b>Please note</b>: An application specific job worker may have already completed the related job.
   *
   * @param verifier Verifier that accepts an {@link ProcessInstanceAssert} instance.
   * @return The handler.
   */
  public JobHandler verify(Consumer<ProcessInstanceAssert> verifier) {
    this.verifier = verifier;
    return this;
  }

  /**
   * Verifies that the job has a specific number of retries.
   *
   * @param expectedRetries The expected retry count.
   * @return The handler.
   */
  public JobHandler verifyRetries(Integer expectedRetries) {
    this.expectedRetries = expectedRetries;
    return this;
  }

  /**
   * Verifies that the job has a specific number of retries, using a consumer.
   *
   * @param retriesConsumer A consumer asserting the retry count.
   * @return The handler.
   */
  public JobHandler verifyRetries(Consumer<Integer> retriesConsumer) {
    this.retriesConsumer = retriesConsumer;
    return this;
  }

  /**
   * Verifies that the job has a specific "retries" FEEL expression (see "Task definition" section), using a consumer function.
   *
   * @param retriesExpressionConsumer A consumer asserting the "retries" expression.
   * @return The handler.
   */
  public JobHandler verifyRetriesExpression(Consumer<String> retriesExpressionConsumer) {
    this.retriesExpressionConsumer = retriesExpressionConsumer;
    return this;
  }

  /**
   * Verifies that the job is of the given type.
   *
   * @param expectedType The expected type.
   * @return The handler.
   */
  public JobHandler verifyType(String expectedType) {
    this.expectedType = expectedType;
    return this;
  }

  /**
   * Verifies that the job is of the given type, using a consumer.
   *
   * @param typeConsumer A consumer asserting the type.
   * @return The handler.
   */
  public JobHandler verifyType(Consumer<String> typeConsumer) {
    this.typeConsumer = typeConsumer;
    return this;
  }

  /**
   * Verifies that the job has a specific type FEEL expression (see "Task definition" section), using a consumer function.
   *
   * @param typeExpressionConsumer A consumer asserting the type expression.
   * @return The handler.
   */
  public JobHandler verifyTypeExpression(Consumer<String> typeExpressionConsumer) {
    this.typeExpressionConsumer = typeExpressionConsumer;
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
