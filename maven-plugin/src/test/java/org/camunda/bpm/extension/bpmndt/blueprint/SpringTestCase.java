package org.camunda.bpm.extension.bpmndt.blueprint;

import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public abstract class SpringTestCase {


  @Autowired
  @Rule
  public ProcessEngineRule rule;
}
