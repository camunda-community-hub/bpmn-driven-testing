package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Multiple test cases used in one test.
 */
@Deployment(resources = "bpmn/noTestCases.bpmn")
public class MultiTestCaseTest {

  @RegisterExtension
  public TestCase1 tc1 = new TestCase1();
  @RegisterExtension
  public TestCase2 tc2 = new TestCase2();

  private CallActivityHandler handler1;
  private CallActivityHandler handler2;

  @BeforeEach
  public void setUp() {
    handler1 = new CallActivityHandler(tc1.instance, "callActivity");
    handler2 = new CallActivityHandler(tc2.instance, "callActivity");
  }

  @Test
  public void testDeployment() {
    assertThat(tc1.getDeploymentId()).isNotEqualTo(tc2.getDeploymentId());
  }

  @Test
  @Deployment(resources = "bpmn/empty.bpmn")
  public void testExecute1() {
    handler1.verify((pi, callActivity) -> {
      pi.variables().containsEntry("a", "b");
    });

    tc1.createExecutor().withVariable("a", "b").withBean("callActivityMapping", new CallActivityMapping()).execute();

    // verify annotation deployment works
    assertThat(ProcessEngineTests.processDefinition("empty")).isNotNull();
    assertThat(ProcessEngineTests.processDefinition("no-test-cases")).isNull();
  }

  @Test
  public void testExecute2() {
    handler2.verify((pi, callActivity) -> {
      pi.variables().containsEntry("x", "y");
    });

    tc2.createExecutor().withVariable("x", "y").withBean("callActivityMapping", new CallActivityMapping()).execute();

    // verify annotation deployment works
    assertThat(ProcessEngineTests.processDefinition("empty")).isNull();
    assertThat(ProcessEngineTests.processDefinition("no-test-cases")).isNotNull();
  }

  private class TestCase1 extends AbstractJUnit5TestCase<TestCase1> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi).isNotNull();

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent").isWaitingAt("callActivity");

      ProcessEngineTests.execute(ProcessEngineTests.job());

      piAssert.hasPassed("callActivity", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.simple("simpleCallActivity.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "simpleCallActivity";
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

  private class TestCase2 extends AbstractJUnit5TestCase<TestCase2> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi).isNotNull();

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);
      
      piAssert.hasPassed("startEvent").isWaitingAt("callActivity");

      ProcessEngineTests.execute(ProcessEngineTests.job());
      
      piAssert.hasPassed("callActivity", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.simple("simpleCallActivity.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public String getProcessDefinitionKey() {
      return "simpleCallActivity";
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

  private class CallActivityMapping implements DelegateVariableMapping {

    @Override
    public void mapInputVariables(DelegateExecution superExecution, VariableMap subVariables) {
      // nothing to do here
    }

    @Override
    public void mapOutputVariables(DelegateExecution superExecution, VariableScope subInstance) {
      // nothing to do here
    }
  }
}
