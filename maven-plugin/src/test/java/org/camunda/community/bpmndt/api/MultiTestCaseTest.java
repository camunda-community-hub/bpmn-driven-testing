package org.camunda.community.bpmndt.api;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.bpm.engine.variable.VariableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Multiple test cases used in one test.
 */
@Deployment(resources = "bpmn/noTestCases.bpmn")
public class MultiTestCaseTest {

  @Rule
  public TestCase tc1 = new TestCase();
  @Rule
  public TestCase tc2 = new TestCase();

  private CallActivityHandler handler1;
  private CallActivityHandler handler2;

  @Before
  public void setUp() {
    handler1 = new CallActivityHandler(tc1.instance, "callActivity");
    handler2 = new CallActivityHandler(tc2.instance, "callActivity");
  }

  @Test
  @Deployment(resources = "bpmn/empty.bpmn")
  public void testExecute1() {
    handler1.verify((pi, callActivity) -> {
      pi.variables().containsEntry("a", "b");
    });

    tc1.createExecutor().withVariable("a", "b").withBean("callActivityMapping", new CallActivityMapping()).execute();

    // verify annotation deployment works
    assertThat(ProcessEngineTests.processDefinition("empty"), notNullValue());
    assertThat(ProcessEngineTests.processDefinition("no-test-cases"), nullValue());
  }

  @Test
  public void testExecute2() {
    handler2.verify((pi, callActivity) -> {
      pi.variables().containsEntry("x", "y");
    });

    tc2.createExecutor().withVariable("x", "y").withBean("callActivityMapping", new CallActivityMapping()).execute();

    // verify annotation deployment works
    assertThat(ProcessEngineTests.processDefinition("empty"), nullValue());
    assertThat(ProcessEngineTests.processDefinition("no-test-cases"), notNullValue());
  }

  private class TestCase extends AbstractJUnit4TestCase {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi, notNullValue());

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);
      
      piAssert.hasPassed("startEvent").isWaitingAt("callActivity");

      ProcessEngineTests.execute(ProcessEngineTests.job());
      
      piAssert.hasPassed("callActivity", "endEvent").isEnded();
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(Paths.get("./src/test/it/simple/src/main/resources/simpleCallActivity.bpmn"));
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
