package org.example.it;

import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import generated.example.TC_C;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CoverageConfiguration.class})
public class CoverageBranchCTest {

  @RegisterExtension
  public TC_C tcC = new TC_C();

  @Test
  public void testC() {
    tcC.createExecutor().withVariable("branch", "c").verify(ProcessInstanceAssert::isEnded).execute();
  }
}
