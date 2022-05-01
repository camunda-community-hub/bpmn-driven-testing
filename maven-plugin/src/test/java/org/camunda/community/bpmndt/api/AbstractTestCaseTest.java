package org.camunda.community.bpmndt.api;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class AbstractTestCaseTest {

  @Rule
  public TestCase tc = new TestCase();

  private RepositoryService repositoryService;

  @Before
  public void setUp() {
    repositoryService = tc.getProcessEngine().getRepositoryService();
  }

  /**
   * Tests if the deployment of the related BPMN resource works.
   */
  @Test
  public void testDeployment() {
    assertThat(tc.getDeploymentId(), notNullValue());

    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService.createDeploymentQuery()
        .deploymentId(tc.getDeploymentId())
        .singleResult();

    assertThat(deployment, notNullValue());
    assertThat(deployment.getName(), equalTo("TestCase"));

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
        .deploymentId(tc.getDeploymentId())
        .processDefinitionKey("simple")
        .singleResult();

    assertThat(processDefinition, notNullValue());
  }

  /**
   * Tests if the {@code Deployment} annotation works the same as when it is used with the
   * {@code ProcessEngineRule} class.
   */
  @Test
  @Deployment(resources = "bpmn/noTestCases.bpmn")
  public void testDeploymentAnnotation() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("no-test-cases")
        .singleResult();

    assertThat(processDefinition, notNullValue());
    assertThat(processDefinition.getDeploymentId(), not(equalTo(tc.getDeploymentId())));

    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService.createDeploymentQuery()
        .deploymentId(processDefinition.getDeploymentId())
        .singleResult();

    assertThat(deployment, notNullValue());
    assertThat(deployment.getName(), equalTo("AbstractTestCaseTest.testDeploymentAnnotation"));
  }

  private class TestCase extends AbstractJUnit4TestCase<TestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi, notNullValue());

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(Paths.get("./src/test/it/simple/src/main/resources/simple.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "simple";
    }

    @Override
    public String getStart() {
      return "startEvent";
    }

    @Override
    public String getEnd() {
      return "endEvent";
    }
  }
}
