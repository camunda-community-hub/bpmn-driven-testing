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
class CallActivityBindingDeploymentTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;

  private CallActivityHandler handler;

  @BeforeEach
  void setUp() {
    var element = new CallActivityElement();
    element.id = "callActivity";
    element.bindingType = CallActivityBindingType.DEPLOYMENT;

    handler = new CallActivityHandler(element);
  }

  @Test
  void testExecute() {
    tc.createExecutor(engine).customize(this::customize).execute();
  }

  @Test
  void testVerifyBindingType() {
    handler.verifyBindingType(CallActivityBindingType.LATEST);

    var e = assertThrows(AssertionError.class, () -> tc.createExecutor(engine).customize(this::customize).withWaitTimeout(1000L).execute());
    assertThat(e).hasMessageThat().contains("binding type LATEST");
    assertThat(e).hasMessageThat().contains("but was DEPLOYMENT");

    handler.verifyBindingType(CallActivityBindingType.DEPLOYMENT);

    tc.createExecutor(engine).customize(this::customize).execute();

    handler.verifyBindingType(bindingType -> assertThat(bindingType).isEqualTo(CallActivityBindingType.LATEST));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).customize(this::customize).withWaitTimeout(1000L).execute());

    handler.verifyBindingType(bindingType -> assertThat(bindingType).isEqualTo(CallActivityBindingType.DEPLOYMENT));

    tc.createExecutor(engine).customize(this::customize).execute();
  }

  private void customize(TestCaseExecutor testCaseExecutor) {
    testCaseExecutor.simulateProcess("advanced");
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
      return "callActivityBindingDeployment";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.advanced("callActivityBindingDeployment.bpmn"));
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
