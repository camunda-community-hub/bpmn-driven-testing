package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Deployment(resources = "dmn/simpleBusinessRule.dmn")
public class ParameterizedTestTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  public void testExecute(int value) {
    tc.createExecutor()
        .withVariable("value", value)
        .verify(piAssert -> {
          piAssert.isEnded();

          piAssert.variables().containsEntry("squareNumber", value * value);
        })
        .execute();
  }

  private static class TestCase extends AbstractJUnit5TestCase<TestCase> {

    @Override
    protected void execute(ProcessInstance pi) {
      assertThat(pi).isNotNull();

      ProcessInstanceAssert piAssert = ProcessEngineTests.assertThat(pi);

      piAssert.hasPassed("startEvent", "endEvent").isEnded();
    }

    @Override
    protected String getBpmnResourceName() {
      return "bpmn/simpleBusinessRuleTask.bpmn";
    }

    @Override
    public String getProcessDefinitionKey() {
      return "simpleBusinessRuleTask";
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
