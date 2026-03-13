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

import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class CallActivityBindingVersionTagTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  private CallActivityHandler handler;

  @BeforeEach
  void setUp() {
    var element = new CallActivityElement();
    element.id = "callActivity";
    element.bindingType = CallActivityBindingType.VERSION_TAG;
    element.versionTag = "v1";

    handler = new CallActivityHandler(element);
  }

  @Test
  void testExecute() {
    tc.createExecutor(client, processTestContext).customize(this::customize).execute();
  }

  @Test
  void testVerifyVersionTag() {
    handler.verifyVersionTag("wrong version");

    var e = assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).customize(this::customize).execute());
    assertThat(e).hasMessageThat().contains("'wrong version'");
    assertThat(e).hasMessageThat().contains("'v1'");

    handler.verifyVersionTag("v1");

    tc.createExecutor(client, processTestContext).customize(this::customize).execute();

    handler.verifyVersionTag(versionTag -> assertThat(versionTag).isEqualTo("wrong version"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).customize(this::customize).execute());

    handler.verifyVersionTag(versionTag -> assertThat(versionTag).isEqualTo("v1"));

    tc.createExecutor(client, processTestContext).customize(this::customize).execute();
  }

  private void customize(TestCaseExecutor testCaseExecutor) {
    testCaseExecutor.simulateVersionedProcess("advanced", "v1");
    testCaseExecutor.simulateVersionedProcess("advanced", "v2");
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
      return "callActivityBindingVersionTag";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(TestPaths.advanced("callActivityBindingVersionTag.bpmn"));
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
