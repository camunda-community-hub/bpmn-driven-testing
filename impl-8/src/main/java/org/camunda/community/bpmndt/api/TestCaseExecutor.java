package org.camunda.community.bpmndt.api;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.camunda.community.bpmndt.api.TestCaseInstance.Holder;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.command.DeployResourceCommandStep1.DeployResourceCommandStep2;
import io.camunda.client.api.response.DeploymentEvent;
import io.camunda.client.api.response.Process;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.client.api.search.response.ElementInstance;
import io.camunda.process.test.api.CamundaAssert;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;
import io.camunda.process.test.api.assertions.ProcessInstanceSelectors;
import io.camunda.process.test.impl.assertions.util.AwaitilityBehavior;

/**
 * Fluent API to prepare and start the actual test case execution.
 */
public class TestCaseExecutor {

  private final AbstractTestCase testCase;
  private final CamundaClient client;
  private final CamundaProcessTestContext processTestContext;
  private final String simulateSubProcessResource;

  private final Map<String, Object> variableMap = new HashMap<>();

  private final List<String> additionalResourceNames = new ArrayList<>(0);
  private final List<String> additionalResources = new ArrayList<>(0);
  private final List<String> additionalResourceVersionTags = new ArrayList<>(0);

  private final AwaitilityBehavior awaitilityBehavior = new AwaitilityBehavior();

  private String tenantId;
  private Object variables;
  private Consumer<ProcessInstanceAssert> verifier;

  public TestCaseExecutor(AbstractTestCase testCase, CamundaClient client, CamundaProcessTestContext processTestContext, String simulateSubProcessResource) {
    this.testCase = testCase;
    this.client = client;
    this.processTestContext = processTestContext;
    this.simulateSubProcessResource = simulateSubProcessResource;
  }

  /**
   * Customizes the executor, using the given {@link Consumer} function. This method can be used to apply a common customization needed for different test
   * cases.
   *
   * <pre>
   * tc.createExecutor().customize(this::prepareVariables).execute();
   * </pre>
   *
   * @param customizer A function that accepts a {@link TestCaseExecutor}.
   * @return The executor.
   */
  public TestCaseExecutor customize(Consumer<TestCaseExecutor> customizer) {
    if (customizer != null) {
      customizer.accept(this);
    }
    return this;
  }

  /**
   * Create a new process instance, executes the actual test case and verifies the state after.
   *
   * @return The key of the newly created process instance.
   */
  public long execute() {
    var deploymentEvent = deployResources();

    var processDefinitionKey = findProcessDefinitionKey(deploymentEvent);

    if (variables != null && !variableMap.isEmpty()) {
      throw new IllegalStateException("either use an object (POJO) as variables or a variable map");
    }

    long processInstanceKey;
    if (testCase.isMessageStart()) {
      // handle message start event
      var publishMessageCommandStep3 = client.newPublishMessageCommand()
          .messageName(findStartMessageName())
          .correlationKey(String.format("%s.%s", testCase.testClass.getSimpleName(), testCase.testMethodName));

      if (variables != null) {
        publishMessageCommandStep3 = publishMessageCommandStep3.variables(variables);
      } else {
        publishMessageCommandStep3 = publishMessageCommandStep3.variables(variableMap);
      }

      if (tenantId != null) {
        publishMessageCommandStep3 = publishMessageCommandStep3.tenantId(tenantId);
      }

      // publish message
      publishMessageCommandStep3.send().join();

      // find key of created process instance
      processInstanceKey = findProcessInstanceKey(processDefinitionKey);
    } else if (testCase.isSignalStart()) {
      // handle signal start event
      var broadcastSignalCommandStep2 = client.newBroadcastSignalCommand().signalName(findStartSignalName(processDefinitionKey));

      if (variables != null) {
        broadcastSignalCommandStep2 = broadcastSignalCommandStep2.variables(variables);
      } else {
        broadcastSignalCommandStep2 = broadcastSignalCommandStep2.variables(variableMap);
      }

      // broadcast signal
      broadcastSignalCommandStep2.send().join();

      // find key of created process instance
      processInstanceKey = findProcessInstanceKey(processDefinitionKey);
    } else if (testCase.isTimerStart()) {
      // handle timer start event
      if (variables != null || !variableMap.isEmpty()) {
        throw new IllegalStateException("not possible to create a process instance with variables, using a timer start event");
      }

      var startTimerDueDate = findStartTimerDueDate(processDefinitionKey);
      processTestContext.increaseTime(Duration.ofMillis(startTimerDueDate - System.currentTimeMillis()));

      // find key of created process instance
      processInstanceKey = findProcessInstanceKey(processDefinitionKey);
    } else {
      // handle none start event
      var createProcessInstanceCommandStep3 = client.newCreateInstanceCommand()
          .bpmnProcessId(testCase.getBpmnProcessId())
          .latestVersion();

      if (!testCase.isProcessStart()) {
        createProcessInstanceCommandStep3 = createProcessInstanceCommandStep3.startBeforeElement(testCase.getStart());
      }

      if (variables != null) {
        createProcessInstanceCommandStep3 = createProcessInstanceCommandStep3.variables(variables);
      } else {
        createProcessInstanceCommandStep3 = createProcessInstanceCommandStep3.variables(variableMap);
      }

      if (tenantId != null) {
        createProcessInstanceCommandStep3 = createProcessInstanceCommandStep3.tenantId(tenantId);
      }

      var processInstanceEvent = createProcessInstanceCommandStep3.send().join();

      processInstanceKey = processInstanceEvent.getProcessInstanceKey();
    }

    executeTestCase(processInstanceKey);

    return processInstanceKey;
  }

  /**
   * Executes the actual test case and verifies the state after, using a runnable that starts a new process instance.
   * <br>
   * This method is suitable for testing process starts with custom code, but not self-managing a process deployment.
   *
   * @param startProcessInstance A runnable that starts a process instance.
   * @return The key of the started process instance.
   */
  public long execute(Runnable startProcessInstance) {
    if (startProcessInstance == null) {
      throw new IllegalArgumentException("start process instance runnable is null");
    }
    if (variables != null || !variableMap.isEmpty()) {
      throw new IllegalStateException("variables and variable map are not supported when starting a process instance via runnable");
    }

    var deploymentEvent = deployResources();

    var processDefinitionKey = findProcessDefinitionKey(deploymentEvent);

    // start process instance
    startProcessInstance.run();

    // find process instance
    var processInstanceKey = findProcessInstanceKey(processDefinitionKey);

    executeTestCase(processInstanceKey);

    return processInstanceKey;
  }

  /**
   * Executes the actual test case and verifies the state after, using the given event.
   *
   * @param processInstanceEvent The event related to an existing process instance, used to execute the test case.
   */
  public void execute(ProcessInstanceEvent processInstanceEvent) {
    if (processInstanceEvent == null) {
      throw new IllegalArgumentException("process instance event is null");
    }
    if (variables != null || !variableMap.isEmpty()) {
      throw new IllegalStateException("variables and variable map are not supported when process instance has already been created");
    }

    executeTestCase(processInstanceEvent.getProcessInstanceKey());
  }

  /**
   * Executes the actual test case and verifies the state after, using the process instance, identified by the given key.
   *
   * @param processInstanceKey The key of an existing process instance.
   */
  public void execute(long processInstanceKey) {
    if (variables != null || !variableMap.isEmpty()) {
      throw new IllegalStateException("variables and variable map are not supported when process instance has already been created");
    }

    executeTestCase(processInstanceKey);
  }

  /**
   * Simulates the process with the given ID by adding a stub process to the resource deployment.
   *
   * @param processId The ID of the process to simulate.
   * @return The executor.
   * @see #simulateVersionedProcess(String, String)
   */
  public TestCaseExecutor simulateProcess(String processId) {
    if (processId == null || processId.isBlank()) {
      throw new IllegalArgumentException("process ID is null or blank");
    }
    var resource = simulateSubProcessResource.replace("processId", processId);
    return withAdditionalResource(processId + ".bpmn", resource);
  }

  /**
   * Simulates the process with the given ID by adding a stub process to a separate versioned resource deployment.
   *
   * @param processId  The ID of the process to simulate.
   * @param versionTag A version tag, corresponding to a call activity's version tag.
   * @return The executor.
   * @see #simulateProcess(String)
   */
  public TestCaseExecutor simulateVersionedProcess(String processId, String versionTag) {
    if (processId == null || processId.isBlank()) {
      throw new IllegalArgumentException("process ID is null or blank");
    }
    if (versionTag == null || versionTag.isBlank()) {
      throw new IllegalArgumentException("version tag is null or blank");
    }

    var resource = simulateSubProcessResource
        .replace("processId", processId)
        .replace("processVersion", versionTag);

    return withAdditionalVersionedResource(processId + ".bpmn", resource, versionTag);
  }

  /**
   * Verifies the state after the test case execution has finished.
   *
   * @param verifier Verifier that accepts an {@link ProcessInstanceAssert} instance.
   * @return The executor.
   */
  public TestCaseExecutor verify(Consumer<ProcessInstanceAssert> verifier) {
    this.verifier = verifier;
    return this;
  }

  /**
   * Adds a classpath resource to the resource deployment ({@link CamundaClient#newDeployResourceCommand()}).
   *
   * @param classpathResourceName Name of the classpath resource - e.g. "bpmn/my-process.bpmn", if the resource is under
   *                              src/main/resources/bpmn/my-process.bpmn.
   * @return The executor.
   * @see #withAdditionalVersionedClasspathResource(String, String)
   */
  public TestCaseExecutor withAdditionalClasspathResource(String classpathResourceName) {
    if (classpathResourceName == null || classpathResourceName.isBlank()) {
      throw new IllegalArgumentException("classpath resource name is null or blank");
    }
    additionalResourceNames.add(classpathResourceName);
    additionalResources.add(null);
    additionalResourceVersionTags.add(null);
    return this;
  }

  /**
   * Adds a resource to the resource deployment ({@link CamundaClient#newDeployResourceCommand()}).
   *
   * @param resourceName Name of the resource.
   * @param resource     The resource as UTF-8 string.
   * @return The executor.
   * @see #withAdditionalVersionedResource(String, String, String)
   */
  public TestCaseExecutor withAdditionalResource(String resourceName, String resource) {
    if (resourceName == null || resourceName.isBlank()) {
      throw new IllegalArgumentException("resource name is null or blank");
    }
    if (resource == null) {
      throw new IllegalArgumentException("resource is null");
    }
    additionalResourceNames.add(resourceName);
    additionalResources.add(resource);
    additionalResourceVersionTags.add(null);
    return this;
  }

  /**
   * Adds a classpath resource to a separate versioned resource deployment ({@link CamundaClient#newDeployResourceCommand()}). Versioned resources are needed to
   * test business rule tasks with a DMN decision or user tasks with a form that have the binding type "version tag".
   *
   * @param classpathResourceName Name of the classpath resource - e.g. "bpmn/my-process.bpmn", if the resource is under
   *                              src/main/resources/bpmn/my-process.bpmn.
   * @param versionTag            A specific version tag.
   * @return The executor.
   * @see #withAdditionalClasspathResource(String)
   */
  public TestCaseExecutor withAdditionalVersionedClasspathResource(String classpathResourceName, String versionTag) {
    if (classpathResourceName == null || classpathResourceName.isBlank()) {
      throw new IllegalArgumentException("classpath resource name is null or blank");
    }
    if (versionTag == null || versionTag.isBlank()) {
      throw new IllegalArgumentException("version tag is null or blank");
    }
    additionalResourceNames.add(classpathResourceName);
    additionalResources.add(null);
    additionalResourceVersionTags.add(versionTag);
    return this;
  }

  /**
   * Adds a resource to a separate versioned resource deployment ({@link CamundaClient#newDeployResourceCommand()}). Versioned resources are needed to test
   * business rule tasks with a DMN decision or user tasks with a form that have the binding type "version tag".
   *
   * @param resourceName Name of the resource.
   * @param resource     The resource as UTF-8 string.
   * @param versionTag   A specific version tag.
   * @return The executor.
   * @see #withAdditionalResource(String, String)
   */
  public TestCaseExecutor withAdditionalVersionedResource(String resourceName, String resource, String versionTag) {
    if (resourceName == null || resourceName.isBlank()) {
      throw new IllegalArgumentException("resource name is null or blank");
    }
    if (resource == null) {
      throw new IllegalArgumentException("resource is null");
    }
    if (versionTag == null || versionTag.isBlank()) {
      throw new IllegalArgumentException("version tag is null or blank");
    }
    additionalResourceNames.add(resourceName);
    additionalResources.add(resource);
    additionalResourceVersionTags.add(versionTag);
    return this;
  }

  /**
   * Set the interval between the assertion attempts - default: 100ms
   *
   * @param assertionInterval The assertion interval to use.
   * @return The executor.
   * @see CamundaAssert#DEFAULT_ASSERTION_INTERVAL
   */
  public TestCaseExecutor withAssertionInterval(Duration assertionInterval) {
    awaitilityBehavior.setAssertionInterval(assertionInterval);
    return this;
  }

  /**
   * Set the timeout of the assertion - default: 10s
   *
   * @param assertionTimeout The assertion timeout to use.
   * @return The executor.
   * @see CamundaAssert#DEFAULT_ASSERTION_TIMEOUT
   */
  public TestCaseExecutor withAssertionTimeout(Duration assertionTimeout) {
    awaitilityBehavior.setAssertionTimeout(assertionTimeout);
    return this;
  }

  /**
   * Sets the tenant ID to be used for the automatic process deployment.
   *
   * @param tenantId A specific tenant ID.
   * @return The executor.
   */
  public TestCaseExecutor withTenantId(String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  /**
   * Sets a variable that is used for the process instance creation.
   *
   * @param name  The name of the variable.
   * @param value The variable's value.
   * @return The executor.
   */
  public TestCaseExecutor withVariable(String name, Object value) {
    variableMap.put(name, value);
    return this;
  }

  /**
   * Sets an object as variables that is used for the process instance creation.
   *
   * @param variables The variables as POJO.
   * @return The executor.
   */
  public TestCaseExecutor withVariables(Object variables) {
    this.variables = variables;
    return this;
  }

  /**
   * Sets variables that are used for the process instance creation.
   *
   * @param variableMap A map of variables.
   * @return The executor.
   */
  public TestCaseExecutor withVariableMap(Map<String, Object> variableMap) {
    this.variableMap.putAll(variableMap);
    return this;
  }

  DeploymentEvent deployResources() {
    deployVersionedResources();

    var deployResourceCommandStep1 = client.newDeployResourceCommand();

    DeployResourceCommandStep2 deployResourceCommandStep2;
    if (testCase.getBpmnResourceName() != null) {
      deployResourceCommandStep2 = deployResourceCommandStep1.addResourceFromClasspath(testCase.getBpmnResourceName());
    } else {
      var resourceName = String.format("%s.%s.bpmn", testCase.testClass.getSimpleName(), testCase.testMethodName);
      deployResourceCommandStep2 = deployResourceCommandStep1.addResourceStream(testCase.getBpmnResource(), resourceName);
    }

    for (int i = 0; i < additionalResources.size(); i++) {
      var versionTag = additionalResourceVersionTags.get(i);
      if (versionTag != null) {
        // skip versioned resources
        continue;
      }

      var resourceName = additionalResourceNames.get(i);
      var resource = additionalResources.get(i);

      if (resource == null) {
        deployResourceCommandStep2 = deployResourceCommandStep2.addResourceFromClasspath(resourceName);
      } else {
        deployResourceCommandStep2 = deployResourceCommandStep2.addResourceStringUtf8(resource, resourceName);
      }
    }

    if (tenantId != null) {
      deployResourceCommandStep2 = deployResourceCommandStep2.tenantId(tenantId);
    }

    var deploymentEvent = deployResourceCommandStep2.send().join();

    var isProcessDeployed = deploymentEvent.getProcesses().stream()
        .anyMatch(process -> process.getBpmnProcessId().equals(testCase.getBpmnProcessId()));

    if (!isProcessDeployed) {
      throw new RuntimeException(String.format("expected BPMN process %s to be deployed, but is not", testCase.getBpmnProcessId()));
    }

    return deploymentEvent;
  }

  void deployVersionedResources() {
    var versionTags = additionalResourceVersionTags.stream().filter(Objects::nonNull).collect(Collectors.toSet());
    for (String versionTag : versionTags) {
      var deployResourceCommandStep1 = client.newDeployResourceCommand();

      DeployResourceCommandStep2 deployResourceCommandStep2 = null;
      for (int i = 0; i < additionalResources.size(); i++) {
        if (!versionTag.equals(additionalResourceVersionTags.get(i))) {
          continue;
        }

        var resourceName = additionalResourceNames.get(i);
        var resource = additionalResources.get(i);

        if (deployResourceCommandStep2 == null) {
          if (resource == null) {
            deployResourceCommandStep2 = deployResourceCommandStep1.addResourceFromClasspath(resourceName);
          } else {
            deployResourceCommandStep2 = deployResourceCommandStep1.addResourceStringUtf8(resource, resourceName);
          }
        } else {
          if (resource == null) {
            deployResourceCommandStep2 = deployResourceCommandStep2.addResourceFromClasspath(resourceName);
          } else {
            deployResourceCommandStep2 = deployResourceCommandStep2.addResourceStringUtf8(resource, resourceName);
          }
        }
      }

      if (deployResourceCommandStep2 == null) {
        continue;
      }

      if (tenantId != null) {
        deployResourceCommandStep2 = deployResourceCommandStep2.tenantId(tenantId);
      }

      deployResourceCommandStep2.send().join();
    }
  }

  void executeTestCase(long processInstanceKey) {
    var testCaseInstance = new TestCaseInstance(client, processTestContext, awaitilityBehavior);

    testCase.execute(testCaseInstance, processInstanceKey);

    if (verifier != null) {
      var processInstanceSelector = ProcessInstanceSelectors.byKey(processInstanceKey);
      verifier.accept(CamundaAssert.assertThat(processInstanceSelector));
    }
  }

  long findProcessDefinitionKey(DeploymentEvent deploymentEvent) {
    return deploymentEvent.getProcesses().stream()
        .filter(process -> process.getBpmnProcessId().equals(testCase.getBpmnProcessId()))
        .map(Process::getProcessDefinitionKey)
        .findFirst()
        .orElseThrow();
  }

  long findProcessInstanceKey(long processDefinitionKey) {
    var startElementInstanceHolder = new Holder<ElementInstance>();

    awaitilityBehavior.untilAsserted(() -> {
      var startElementInstance = client.newElementInstanceSearchRequest()
          .filter(filter -> filter
              .processDefinitionKey(processDefinitionKey)
              .elementId(testCase.getStart())
          )
          .execute()
          .singleItem();

      if (startElementInstance == null) {
        throw new AssertionError(String.format(
            "failed to find start element instance for process definition key %d and element ID %s",
            processDefinitionKey,
            testCase.getStart()
        ));
      }

      return startElementInstance;
    }, startElementInstanceHolder);

    return startElementInstanceHolder.get().getProcessInstanceKey();
  }

  String findStartMessageName() {
    // currently there is no message subscription created for a message start event
    throw new UnsupportedOperationException();
  }

  String findStartSignalName(long processDefinitionKey) {
    // currently the API does not provide signal subscriptions
    throw new UnsupportedOperationException();
  }

  long findStartTimerDueDate(long processDefinitionKey) {
    // currently the API does not provide timers
    throw new UnsupportedOperationException();
  }
}
