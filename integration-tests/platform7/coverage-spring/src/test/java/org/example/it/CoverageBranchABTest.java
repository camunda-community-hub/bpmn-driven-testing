package org.example.it;

import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import generated.example.TC_A;
import generated.example.TC_B;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CoverageConfiguration.class})
public class CoverageBranchABTest {

  @RegisterExtension
  public TC_A tcA = new TC_A();
  @RegisterExtension
  public TC_B tcB = new TC_B();

  @Test
  public void testA() {
    tcA.createExecutor().withVariable("branch", "a").verify(ProcessInstanceAssert::isEnded).execute();
  }

  @Test
  public void testB() {
    tcB.createExecutor().withVariable("branch", "b").verify(ProcessInstanceAssert::isEnded).execute();
  }
}
