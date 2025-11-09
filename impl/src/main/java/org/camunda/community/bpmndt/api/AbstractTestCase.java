package org.camunda.community.bpmndt.api;

import static org.camunda.community.bpmndt.api.TestCaseInstance.PROCESS_ENGINE_NAME;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.community.bpmndt.api.cfg.BpmndtProcessEnginePlugin;

/**
 * Abstract superclass for test cases.
 *
 * @param <T> The generated test case type.
 */
public abstract class AbstractTestCase<T extends AbstractTestCase<?>> {

  /**
   * The related test class - must be provided by the concrete implementation.
   */
  protected Class<?> testClass;
  /**
   * The test method, which will be executed - must be provided by the concrete implementation.
   */
  protected Method testMethod;

  protected TestCaseInstance instance;

  /**
   * Determines if the test case is executed within a call activity.
   */
  protected boolean executedWithinCallActivity;

  /**
   * ID of optional tenant that is used for the process definition deployment.
   */
  private String tenantId;

  private List<AbstractTestCase<?>> subTestCases;

  /**
   * Performs the setup for a test case execution by creating a {@link TestCaseInstance} and deploying the related BPMN resources. This method must be invoked
   * before each test!
   */
  protected void beforeEach() {
    ProcessEngine processEngine = ProcessEngines.getProcessEngine(PROCESS_ENGINE_NAME);

    if (processEngine == null && isSpringEnabled()) {
      String message = String.format("Spring application context must provide a process engine with name '%s'", PROCESS_ENGINE_NAME);
      throw new IllegalStateException(message);
    }
    if (processEngine == null) {
      processEngine = buildProcessEngine();
    }

    ProcessEngineTests.init(processEngine);

    instance = new TestCaseInstance();
    instance.end = getEnd();
    instance.processDefinitionKey = getProcessDefinitionKey();
    instance.processEnd = isProcessEnd();
    instance.processEngine = processEngine;
    instance.start = getStart();
    instance.tenantId = tenantId;

    DeploymentBuilder deploymentBuilder = processEngine.getRepositoryService().createDeployment();

    // add BPMN resource
    if (getBpmnResourceName() != null) {
      deploymentBuilder.addClasspathResource(getBpmnResourceName());
    } else {
      deploymentBuilder.addInputStream(String.format("%s.bpmn", getProcessDefinitionKey()), getBpmnResource());
    }

    if (testMethod != null) {
      // try to find Deployment annotation on test method
      Deployment deploymentAnnotation = testMethod.getAnnotation(Deployment.class);
      if (deploymentAnnotation != null) {
        // add additional classpath resources
        for (String resource : deploymentAnnotation.resources()) {
          deploymentBuilder.addClasspathResource(resource);
        }
      }
    }

    if (testClass != null) {
      // try to find Deployment annotation on test class
      Class<?> deploymentAnnotationClass = testClass;
      while (deploymentAnnotationClass != Object.class) {
        Deployment deploymentAnnotation = deploymentAnnotationClass.getAnnotation(Deployment.class);
        if (deploymentAnnotation != null) {
          // add additional classpath resources
          for (String resource : deploymentAnnotation.resources()) {
            deploymentBuilder.addClasspathResource(resource);
          }

          break;
        }
        deploymentAnnotationClass = deploymentAnnotationClass.getSuperclass();
      }
    }

    String deploymentName;
    if (testMethod != null) {
      deploymentName = String.format("%s.%s", this.getClass().getName(), testMethod.getName());
    } else {
      deploymentName = String.format("%s.%s", this.getClass().getName(), UUID.randomUUID()); // test case, executed by a call activity
    }

    // deploy resources
    instance.deploy(deploymentBuilder, deploymentName);
  }

  /**
   * Performs the teardown for a test case execution. This method must be invoked after each test!
   */
  protected void afterEach() {
    Mocks.reset();

    if (instance == null) {
      // skip undeployment, if process engine was not built
      // or Spring application context did not provide the desired process engine
      return;
    }

    // undeploy resources
    instance.undeploy();

    if (subTestCases != null) {
      subTestCases.forEach(AbstractTestCase::afterEach);
    }
  }

  /**
   * Adds a call activity related test case for a teardown after test case execution.
   *
   * @param subTestCase A test cased, used to execute a called sub process.
   */
  protected void addSubTestCase(AbstractTestCase<?> subTestCase) {
    if (subTestCases == null) {
      subTestCases = new ArrayList<>();
    }
    subTestCases.add(subTestCase);
  }

  /**
   * Builds the process engine, used to execute the test case. The method registers custom {@link ProcessEnginePlugin}s as well as the
   * {@link BpmndtProcessEnginePlugin}, which is required to configure a conform process engine.
   *
   * @return The newly created process engine.
   */
  protected ProcessEngine buildProcessEngine() {
    // must be added to a new list, since the provided list may not allow modifications
    List<ProcessEnginePlugin> processEnginePlugins = new LinkedList<>(getProcessEnginePlugins());
    // BPMN Driven Testing plugin must be added at last
    processEnginePlugins.add(new BpmndtProcessEnginePlugin());

    ProcessEngineConfigurationImpl processEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    processEngineConfiguration.setProcessEnginePlugins(processEnginePlugins);

    return processEngineConfiguration.buildProcessEngine();
  }

  /**
   * Creates a new executor, used to specify variables, business key and/or mocks that are considered during test case execution. After the specification,
   * {@link TestCaseExecutor#execute()} is called to create a new {@link ProcessInstance} and execute the test case.
   *
   * @return The newly created executor.
   */
  public TestCaseExecutor createExecutor() {
    if (executedWithinCallActivity) {
      throw new UnsupportedOperationException("Sub test cases cannot be executed directly");
    }

    return new TestCaseExecutor(instance, this::execute);
  }

  /**
   * Executes the test case.
   *
   * @param pi A process instance, created especially for the test case.
   */
  protected abstract void execute(ProcessInstance pi);

  /**
   * Returns an input stream that provides the BPMN resource with the process definition to be tested - either this method or {@link #getBpmnResourceName()}
   * must be overridden!
   *
   * @return The BPMN resource as stream.
   */
  protected InputStream getBpmnResource() {
    return null;
  }

  /**
   * Returns the name of the BPMN resource, that provides the process definition to be tested - either this method or {@link #getBpmnResource()} must be
   * overridden!
   *
   * @return The BPMN resource name, within {@code src/main/resources}.
   */
  protected String getBpmnResourceName() {
    return null;
  }

  /**
   * Returns the ID of the process definition deployment.
   *
   * @return The deployment ID.
   */
  public String getDeploymentId() {
    return instance.getDeploymentId();
  }

  /**
   * Returns the ID of the test case's end activity.
   *
   * @return The end activity ID.
   */
  public abstract String getEnd();

  /**
   * Returns the key of the process definition that is tested.
   *
   * @return The process definition key.
   */
  public abstract String getProcessDefinitionKey();

  /**
   * Returns the process engine, used to execute the test case.
   *
   * @return The process engine.
   */
  public ProcessEngine getProcessEngine() {
    return instance.getProcessEngine();
  }

  /**
   * Provides custom {@link ProcessEnginePlugin}s to be registered when the process engine is built. By default, this method return an empty list. It can be
   * overridden by any extending class.
   *
   * @return A list of process engine plugins to register.
   * @see #buildProcessEngine()
   */
  protected List<ProcessEnginePlugin> getProcessEnginePlugins() {
    return Collections.emptyList();
  }

  /**
   * Returns the ID of the test case's start activity.
   *
   * @return The start activity ID.
   */
  public abstract String getStart();

  /**
   * Determines if Spring based testing is enabled or not. This method returns {@code false}, if not overridden.
   *
   * @return {@code true}, if the testing is Spring based. Otherwise {@code false}.
   */
  protected boolean isSpringEnabled() {
    return false;
  }

  /**
   * Determines if the test case's end activity ends the process or not. This is the case if the activity is an end event and if the activity's parent scope is
   * the process. This method returns {@code true}, if not overridden.
   *
   * @return {@code true}, if the test case's end activity ends the process. Otherwise {@code false}.
   */
  protected boolean isProcessEnd() {
    return true;
  }

  /**
   * Sets the tenant ID to be used for the automatic process definition deployment.
   *
   * @param tenantId A specific tenant ID.
   * @return The test case.
   */
  @SuppressWarnings("unchecked")
  public T withTenantId(String tenantId) {
    this.tenantId = tenantId;
    return (T) this;
  }
}
