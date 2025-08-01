package org.example.it;

import java.util.Map;

import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import generated.example.TC_startEvent__endEvent;

@SpringBootTest(classes = {ExampleAHandler.class, ExampleBHandler.class, BpmndtConfiguration.class})
class ExampleTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  @Autowired
  ExampleAHandler exampleAHandler;
  @Autowired
  ExampleBHandler exampleBHandler;

  @Test
  void testProcess() {
    tc.handleExternalTaskA().executeExternalTask((externalTask, externalTaskService) -> {
      exampleAHandler.execute(externalTask, externalTaskService);
    });

    tc.handleExternalTaskB().withFetchExtensionProperties(true).withFetchLocalVariablesOnly(true).executeExternalTask((externalTask, externalTaskService) -> {
      exampleBHandler.execute(externalTask, externalTaskService);
    });

    tc.createExecutor()
        .withVariables(Map.of("a", "text", "b", 1, "c", true))
        .verify(ProcessInstanceAssert::isEnded)
        .execute();
  }
}
