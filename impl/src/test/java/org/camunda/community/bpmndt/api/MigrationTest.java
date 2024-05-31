package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Tests if a process that is started with another process definition ID is successfully migrated to process definition, which has been deployed (and
 * instrumented) by the test case.
 */
public class MigrationTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  private JobHandler startEventAfter;

  @BeforeEach
  public void setUp() {
    startEventAfter = new JobHandler(tc.getProcessEngine(), "startEvent");
  }

  @Test
  public void testExecute() {
    // simulate deployment of another test case
    tc.getProcessEngine().getRepositoryService().createDeployment()
        .name("TC_Other")
        .addInputStream(String.format("%s.bpmn", tc.getProcessDefinitionKey()), tc.getBpmnResource())
        .enableDuplicateFiltering(false)
        .deploy();

    // start process instance using latest deployment
    VariableMap variables = Variables.putValue("k1", "v1");
    ProcessInstance pi = tc.getProcessEngine().getRuntimeService().startProcessInstanceByKey(tc.getProcessDefinitionKey(), variables);

    tc.createExecutor()
        .withBean("doA", new DoA())
        .verify(piAssert -> {
          piAssert.variables().containsEntry("k1", "v1");

          piAssert.isNotEnded();
        })
        .execute(pi);

    // assert migration
    ProcessInstance migratedPi = tc.getProcessEngine().getRuntimeService().createProcessInstanceQuery()
        .processInstanceId(pi.getId())
        .singleResult();

    assertThat(migratedPi).isNotNull();
    assertThat(migratedPi.getProcessDefinitionId()).isNotEqualTo(pi.getProcessDefinitionId());
  }

  private class TestCase extends AbstractJUnit5TestCase<TestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi).isNotNull();

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.isWaitingAt("startEvent");

      startEventAfter.apply(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("doA").isNotEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.advanced("serviceTask.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "serviceTask";
    }

    @Override
    public String getStart() {
      return "startEvent";
    }

    @Override
    public String getEnd() {
      return "doA";
    }

    @Override
    protected boolean isProcessEnd() {
      return false;
    }
  }

  private static class DoA implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
      // nothing to do here
    }
  }
}
