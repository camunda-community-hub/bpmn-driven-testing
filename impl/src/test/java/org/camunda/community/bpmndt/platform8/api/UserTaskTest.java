package org.camunda.community.bpmndt.platform8.api;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceElement.UserTaksElement;
import org.camunda.community.bpmndt.test.Platform8TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
public class UserTaskTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;
  ZeebeClient client;

  private UserTaskHandler handler;

  @BeforeEach
  public void setUp() {
    UserTaksElement element = new UserTaksElement();
    element.setAssignee("=\"simpleAssignee\"");
    element.setCandidateGroups("=[\"simpleGroupA\", \"simpleGroupB\"]");
    element.setCandidateUsers("=[\"simpleUserA\", \"simpleUserB\"]");
    element.setDueDate("=\"2023-02-17T00:00:00Z\"");
    element.setFollowUpDate("=\"2023-02-18T00:00:00Z\"");
    element.setFormKey("simpleFormKey");
    element.setId("userTask");

    handler = new UserTaskHandler(element);
  }

  @Test
  public void testExecute() {
    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  public void testVerify() {
    handler.verify(processInstanceAssert -> {
      processInstanceAssert.hasVariableWithValue("x", "test");
    });

    tc.createExecutor(engine)
        .withVariable("x", "test")
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  public void testVerifyAssignee() {
    handler.verifyAssigneeExpression(assignee -> assertThat(assignee).isEqualTo("wrong assignee expression"));

    AssertionError e = assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());
    assertThat(e).hasMessageThat().contains("wrong assignee expression");
    assertThat(e).hasMessageThat().contains("=\"simpleAssignee\"");

    handler.verifyAssigneeExpression(assignee -> assertThat(assignee).isEqualTo("=\"simpleAssignee\""));

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    public String getBpmnProcessId() {
      return "simpleUserTask";
    }

    @Override
    public String getEnd() {
      return "endEvent";
    }

    @Override
    public String getStart() {
      return "startEvent";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(Platform8TestPaths.simple("simpleUserTask.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    protected void execute(TestCaseInstance instance, ProcessInstanceEvent processInstanceEvent) {
      instance.hasPassed(processInstanceEvent, "startEvent");
      instance.isWaitingAt(processInstanceEvent, "userTask");
      instance.apply(processInstanceEvent, handler);
      instance.hasPassed(processInstanceEvent, "userTask");
      instance.hasPassed(processInstanceEvent, "endEvent");
      instance.isCompleted(processInstanceEvent);
    }
  }
}
