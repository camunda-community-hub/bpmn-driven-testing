package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.community.bpmndt.api.TestCaseInstanceElement.CallActivityElement;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class CallActivityTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;

  private CallActivityHandler handler;

  @BeforeEach
  void setUp() {
    var element = new CallActivityElement();
    element.id = "callActivity";
    element.processId = "=\"simple\"";
    element.propagateAllChildVariables = true;
    element.propagateAllParentVariables = true;

    handler = new CallActivityHandler(element);
  }

  @Test
  void testExecute() {
    tc.createExecutor(engine).customize(this::customize).execute();
  }

  @Test
  void testVerifyInputOutput() {
    handler
        .verify(piAssert -> piAssert.hasVariableWithValue("x", "test"))
        .verifyInput(piAssert -> {
          // all parent variables are propagated
          piAssert.hasVariableWithValue("x", "test");
        });

    handler
        .simulateVariable("x", "test123")
        .simulateVariable("y", 1)
        .simulateVariable("z", true);

    handler.verifyOutput(piAssert -> {
      // all child variables are propagated
      piAssert.hasVariableWithValue("x", "test123");
      piAssert.hasVariableWithValue("y", 1);
      piAssert.hasVariableWithValue("z", true);
    });

    tc.createExecutor(engine)
        .customize(this::customize)
        .withVariable("x", "test")
        .execute();
  }

  @Test
  void testVerifyProcessId() {
    handler.verifyProcessId("wrong process ID");

    var e = assertThrows(AssertionError.class, () -> tc.createExecutor(engine).customize(this::customize).execute());
    assertThat(e).hasMessageThat().contains("'wrong process ID'");
    assertThat(e).hasMessageThat().contains("'simple'");

    handler.verifyProcessId("simple");

    tc.createExecutor(engine).customize(this::customize).execute();

    handler.verifyProcessId(assignee -> assertThat(assignee).isEqualTo("wrong process ID"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).customize(this::customize).execute());

    handler.verifyProcessId(processId -> assertThat(processId).isEqualTo("simple"));

    tc.createExecutor(engine).customize(this::customize).execute();
  }

  @Test
  void testVerifyProcessIdExpression() {
    handler.verifyProcessIdExpression(expr -> assertThat(expr).isEqualTo("wrong process ID expression"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).customize(this::customize).execute());

    handler.verifyProcessIdExpression(expr -> assertThat(expr).isEqualTo("=\"simple\""));

    tc.createExecutor(engine).customize(this::customize).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyPropagateAllChildVariables() {
    handler.verifyPropagateAllChildVariables(false);

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).customize(this::customize).execute());

    handler.verifyPropagateAllChildVariables(true);

    tc.createExecutor(engine).customize(this::customize).execute();
  }

  @Test
  void testVerifyPropagateAllParentVariables() {
    handler.verifyPropagateAllParentVariables(false);

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).customize(this::customize).execute());

    handler.verifyPropagateAllParentVariables(true);

    tc.createExecutor(engine).customize(this::customize).execute();
  }

  @Test
  void testErrorContainsIncidents() {
    var e = assertThrows(RuntimeException.class, () -> tc.createExecutor(engine)
        .verify(ProcessInstanceAssert::isCompleted)
        .withWaitTimeout(1000L)
        .execute()
    );

    assertThat(e.getMessage()).contains("found incidents:");
    assertThat(e.getMessage()).contains("  - element callActivity: CALLED_ELEMENT_ERROR: Expected process with BPMN process id 'simple' to be deployed");
  }

  private void customize(TestCaseExecutor testCaseExecutor) {
    testCaseExecutor.simulateProcess("simple");
    testCaseExecutor.verify(ProcessInstanceAssert::isCompleted);
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "startEvent");
      instance.isWaitingAt(processInstanceKey, "callActivity");
      instance.apply(processInstanceKey, handler);
      instance.hasPassed(processInstanceKey, "callActivity");
      instance.hasPassed(processInstanceKey, "endEvent");
    }

    @Override
    public String getBpmnProcessId() {
      return "simpleCallActivity";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.simple("simpleCallActivity.bpmn"));
      } catch (IOException e) {
        return null;
      }
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
