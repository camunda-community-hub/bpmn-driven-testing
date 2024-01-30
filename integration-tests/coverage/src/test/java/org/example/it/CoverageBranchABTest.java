package org.example.it;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.community.process_test_coverage.junit5.platform7.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.example.TC_A;
import generated.example.TC_B;

@Deployment(resources = "example.bpmn") // needed for some reason
public class CoverageBranchABTest {

  @RegisterExtension
  public static ProcessEngineCoverageExtension coverage = CoverageExtensionProvider.get(); // must be static

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
