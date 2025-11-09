package org.example.it;

import static com.google.common.truth.Truth.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.Map;

import org.camunda.spin.Spin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import generated.example.TC_startEvent__endEvent;

// since this is not a process application (@EnableProcessApplication)
// additional process engine plugins must be configured via pom.xml or build.gradle
@SpringBootTest(classes = {ExampleAHandler.class, ExampleBHandler.class})
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

    var input = new ComplexVariable();
    input.setVboolean(true);
    input.setVdate(LocalDate.of(2025, Month.OCTOBER, 25));
    input.setVinteger(123);
    input.setVstring("abc");

    tc.createExecutor()
        .withVariables(Map.of("a", "text", "b", 1, "c", true, "input", input, "inputJson", Spin.JSON("{}")))
        .verify(piAssert -> {
          piAssert.isEnded();

          var variables = piAssert.variables().actual();

          var outputTyped = (ComplexVariable) variables.get("outputTyped");
          assertThat(outputTyped.getVboolean()).isEqualTo(true);
          assertThat(outputTyped.getVdate()).isEqualTo(LocalDate.of(2025, Month.OCTOBER, 26));
          assertThat(outputTyped.getVinteger()).isEqualTo(456);
          assertThat(outputTyped.getVstring()).isEqualTo("def");

          var output = (ComplexVariable) variables.get("output");
          assertThat(output.getVboolean()).isEqualTo(true);
          assertThat(output.getVdate()).isEqualTo(LocalDate.of(2025, Month.OCTOBER, 26));
          assertThat(output.getVinteger()).isEqualTo(456);
          assertThat(output.getVstring()).isEqualTo("def");

          assertThat(variables.get("outputStringTyped")).isEqualTo("vstring");
          assertThat(variables.get("outputString")).isEqualTo("vstring");
          assertThat(variables.get("outputIntegerTyped")).isEqualTo(123);
          assertThat(variables.get("outputInteger")).isEqualTo(123);
        })
        .execute();
  }
}
