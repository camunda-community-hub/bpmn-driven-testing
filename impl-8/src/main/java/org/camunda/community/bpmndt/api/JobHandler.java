package org.camunda.community.bpmndt.api;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.camunda.community.bpmndt.api.TestCaseInstanceElement.JobElement;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;

/**
 * Fluent API to handle jobs in form of service, script, send or business rule tasks as well as intermediate message throw or message end events.
 * <br><br>
 * Please note: a job is not completed by default. The application must run an appropriate worker, which completes relates jobs or throws a BPMN error. If a
 * worker implementation is missing, {@link #complete()}, {@link #throwBpmnError(String, String)} or {@link #execute(BiConsumer)} can be used to handle the
 * job.
 */
public class JobHandler {

  private final JobElement element;

  private final Map<String, Object> variableMap = new HashMap<>();

  private Consumer<ProcessInstanceAssert> verifier;
  private BiConsumer<ZeebeClient, Long> action;
  private String errorCode;
  private String errorMessage;
  private Object variables;

  private Consumer<String> retriesExpressionConsumer;
  private Consumer<String> typeExpressionConsumer;

  private Integer expectedRetries;
  private String expectedType;

  private Consumer<Integer> retriesConsumer;
  private Consumer<String> typeConsumer;

  public JobHandler(String elementId) {
    if (elementId == null) {
      throw new IllegalArgumentException("element ID is null");
    }

    element = new JobElement();
    element.id = elementId;
  }

  public JobHandler(JobElement element) {
    if (element == null) {
      throw new IllegalArgumentException("element is null");
    }
    if (element.id == null) {
      throw new IllegalArgumentException("element ID is null");
    }

    this.element = element;
  }

  void apply(TestCaseInstance instance, long flowScopeKey) {
    if (verifier != null) {
      var processInstanceKey = instance.getProcessInstanceKey(flowScopeKey);
      verifier.accept(new ProcessInstanceAssert(processInstanceKey, BpmnAssert.getRecordStream()));
    }

    if (retriesExpressionConsumer != null) {
      retriesExpressionConsumer.accept(element.retries);
    }
    if (typeExpressionConsumer != null) {
      typeExpressionConsumer.accept(element.type);
    }

    var job = instance.getJob(flowScopeKey, element.id);

    if (expectedRetries != null && !expectedRetries.equals(job.retries)) {
      var message = "expected job %s to have a retry count of %d, but was %d";
      throw new AssertionError(String.format(message, element.id, expectedRetries, job.retries));
    }
    if (retriesConsumer != null) {
      retriesConsumer.accept(job.retries);
    }

    if (expectedType != null && !expectedType.equals(job.type)) {
      var message = "expected job %s to be of type '%s', but was '%s'";
      throw new AssertionError(String.format(message, element.id, expectedType, job.type));
    }
    if (typeConsumer != null) {
      typeConsumer.accept(job.type);
    }

    if (action != null) {
      action.accept(instance.getClient(), job.key);
    }
  }

  /**
   * Executes an action that completes the job, when the process instance is waiting at the corresponding element, using specified variables.
   *
   * @see #withVariable(String, Object)
   * @see #withVariables(Object)
   * @see #withVariableMap(Map)
   */
  public void complete() {
    action = this::complete;
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

  /**
   * Throws an BPMN error using the given error code and message as well as the specified variables.
   * <p>
   * This action can be used, if there is no related registered job worker yet.
   *
   * @see #withVariable(String, Object)
   * @see #withVariables(Object)
   * @see #withVariableMap(Map)
   */
  public void throwBpmnError(String errorCode, String errorMessage) {
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
    this.action = this::throwBpmnError;
  }

  /**
   * Verifies the job's waiting state.
   * <p>
   * <b>Please note</b>: An application specific job worker may have already completed the related job and updated some variables.
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
   * Applies no action at the wait state. This is required when waiting for events (e.g. message, signal or timer events) that are attached as boundary events
   * on the element itself or on the surrounding scope (e.g. embedded subprocess).
   */
  public void waitForBoundaryEvent() {
    action = null;
  }

  /**
   * Sets a variable that is used to complete the job.
   *
   * @param name  The name of the variable.
   * @param value The variable's value.
   * @return The handler.
   * @see #complete()
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
   * @see #complete()
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
   * @see #complete()
   */
  public JobHandler withVariableMap(Map<String, Object> variableMap) {
    if (variables != null) {
      throw new IllegalStateException("either use an object (POJO) as variables or a variable map");
    }
    this.variableMap.putAll(variableMap);
    return this;
  }

  void complete(ZeebeClient client, long jobKey) {
    if (variables != null) {
      client.newCompleteCommand(jobKey).variables(variables).send().join();
    } else {
      client.newCompleteCommand(jobKey).variables(variableMap).send().join();
    }
  }

  void throwBpmnError(ZeebeClient client, long jobKey) {
    var throwErrorCommandStep2 = client.newThrowErrorCommand(jobKey).errorCode(errorCode).errorMessage(errorMessage);

    if (variables != null) {
      throwErrorCommandStep2.variables(variables).send().join();
    } else {
      throwErrorCommandStep2.variables(variableMap).send().join();
    }
  }
}
