package org.camunda.bpm.extension.bpmndt.blueprint;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

/**
 * Blueprint for Spring based testing.
 */
@ContextConfiguration(classes = SpringTestConfiguration.class)
public class SpringBlueprintTest extends SpringTestCase {

  @Test
  public void testProcessEngine() {
    assertThat(rule, notNullValue());
  }

  @Test
  @Deployment(resources = "bpmn/simple.bpmn")
  public void testDeployment() {
    assertThat(rule.getRepositoryService(), notNullValue());
    assertThat(rule.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey("simple").count(), is(1L));
  }
}
