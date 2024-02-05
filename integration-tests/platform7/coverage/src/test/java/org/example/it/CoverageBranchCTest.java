package org.example.it;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.community.process_test_coverage.junit5.platform7.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.example.TC_C;

@Deployment(resources = "example.bpmn") // needed for some reason
public class CoverageBranchCTest {

  @RegisterExtension
  public static ProcessEngineCoverageExtension coverage = CoverageExtensionProvider.get(); // must be static

  @RegisterExtension
  public TC_C tcC = new TC_C();

  @Test
  public void testC() {
    tcC.createExecutor().withVariable("branch", "c").verify(ProcessInstanceAssert::isEnded).execute();
  }
}
