package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class AbstractTestCaseTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  private RepositoryService repositoryService;

  @BeforeEach
  public void setUp() {
    repositoryService = tc.getProcessEngine().getRepositoryService();
  }

  /**
   * Tests if the deployment of the related BPMN resource works.
   */
  @Test
  public void testDeployment() {
    assertThat(tc.getDeploymentId()).isNotNull();

    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService.createDeploymentQuery()
        .deploymentId(tc.getDeploymentId())
        .singleResult();

    assertThat(deployment).isNotNull();
    assertThat(deployment.getName()).isEqualTo("TestCase");

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
        .deploymentId(tc.getDeploymentId())
        .processDefinitionKey("simple")
        .singleResult();

    assertThat(processDefinition).isNotNull();
  }

  /**
   * Tests if the {@code Deployment} annotation works the same as when it is used with the {@code ProcessEngineRule} class.
   */
  @Test
  @Deployment(resources = "bpmn/noTestCases.bpmn")
  public void testDeploymentAnnotation() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("noTestCases")
        .singleResult();

    assertThat(processDefinition).isNotNull();
    assertThat(processDefinition.getDeploymentId()).isNotEqualTo(tc.getDeploymentId());

    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService.createDeploymentQuery()
        .deploymentId(processDefinition.getDeploymentId())
        .singleResult();

    assertThat(deployment).isNotNull();
    assertThat(deployment.getName()).isEqualTo("AbstractTestCaseTest.testDeploymentAnnotation");
  }

  private static class TestCase extends AbstractJUnit5TestCase<TestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi).isNotNull();

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.simple("simple.bpmn"));
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
