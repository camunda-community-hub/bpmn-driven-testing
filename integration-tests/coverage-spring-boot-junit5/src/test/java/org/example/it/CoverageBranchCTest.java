package org.example.it;

import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.example.ExampleApp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import generated.example.TC_C;

@SpringBootTest(classes = {ExampleApp.class, BpmndtConfiguration.class}, webEnvironment = WebEnvironment.NONE)
public class CoverageBranchCTest {

  @RegisterExtension
  public TC_C tcC = new TC_C();

  @Test
  public void testC() {
    tcC.createExecutor().withVariable("branch", "c").verify(ProcessInstanceAssert::isEnded).execute();
  }
}
