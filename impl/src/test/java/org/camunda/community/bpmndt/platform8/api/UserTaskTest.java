package org.camunda.community.bpmndt.platform8.api;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;

import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceElement.UserTaskElement;
import org.camunda.community.bpmndt.test.Platform8TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
public class UserTaskTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;

  private UserTaskHandler handler;
  private UserTaskHandler emptyHandler;

  @BeforeEach
  public void setUp() {
    UserTaskElement element = new UserTaskElement();
    element.setAssignee("=\"simpleAssignee\"");
    element.setCandidateGroups("=[\"simpleGroupA\", \"simpleGroupB\"]");
    element.setCandidateUsers("=[\"simpleUserA\", \"simpleUserB\"]");
    element.setDueDate("=\"2023-02-17T00:00:00Z\"");
    element.setFollowUpDate("=\"2023-02-18T00:00:00Z\"");
    element.setId("userTask");

    handler = new UserTaskHandler(element);

    UserTaskElement emptyElement = new UserTaskElement();
    emptyElement.setId("emptyUserTask");

    emptyHandler = new UserTaskHandler(emptyElement);
  }

  @Test
  public void testExecute() {
    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  public void testVerify() {
    handler.verify(processInstanceAssert -> processInstanceAssert.hasVariableWithValue("x", "test"));

    tc.createExecutor(engine)
        .withVariable("x", "test")
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  public void testVerifyAssignee() {
    handler.verifyAssignee("wrong assignee");

    AssertionError e = assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());
    assertThat(e).hasMessageThat().contains("'wrong assignee'");
    assertThat(e).hasMessageThat().contains("'simpleAssignee'");

    handler.verifyAssignee("simpleAssignee");

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();

    handler.verifyAssignee(assignee -> assertThat(assignee).isEqualTo("wrong assignee"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyAssignee(assignee -> assertThat(assignee).isEqualTo("simpleAssignee"));

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  public void testVerifyAssigneeExpression() {
    handler.verifyAssigneeExpression(expr -> assertThat(expr).isEqualTo("wrong assignee expression"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyAssigneeExpression(expr -> assertThat(expr).isEqualTo("=\"simpleAssignee\""));

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  public void testVerifyCandidateGroups() {
    handler.verifyCandidateGroups(Arrays.asList("wrong group 1", "wrong group 2"));

    AssertionError e = assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());
    assertThat(e).hasMessageThat().contains("candidate group #0 'wrong group 1'");
    assertThat(e).hasMessageThat().contains("'simpleGroupA'");

    handler.verifyCandidateGroups(Arrays.asList("simpleGroupA", "simpleGroupB"));

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();

    handler.verifyCandidateGroups(groups -> assertThat(groups).containsExactly("wrong group 1", "wrong group 2").inOrder());

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyCandidateGroups(groups -> assertThat(groups).containsExactly("simpleGroupA", "simpleGroupB").inOrder());

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  public void testVerifyCandidateGroupsExpression() {
    handler.verifyCandidateGroupsExpression(expr -> assertThat(expr).isEqualTo("wrong candidate groups expression"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyCandidateGroupsExpression(expr -> assertThat(expr).isEqualTo("=[\"simpleGroupA\", \"simpleGroupB\"]"));

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  public void testVerifyCandidateUsers() {
    handler.verifyCandidateUsers(Arrays.asList("wrong user 1", "wrong user 2"));

    AssertionError e = assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());
    assertThat(e).hasMessageThat().contains("candidate user #0 'wrong user 1'");
    assertThat(e).hasMessageThat().contains("'simpleUserA'");

    handler.verifyCandidateUsers(Arrays.asList("simpleUserA", "simpleUserB"));

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();

    handler.verifyCandidateUsers(users -> assertThat(users).containsExactly("wrong user 1", "wrong user 2").inOrder());

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyCandidateUsers(users -> assertThat(users).containsExactly("simpleUserA", "simpleUserB").inOrder());

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  public void testVerifyCandidateUsersExpression() {
    handler.verifyCandidateUsersExpression(expr -> assertThat(expr).isEqualTo("wrong candidate users expression"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyCandidateUsersExpression(expr -> assertThat(expr).isEqualTo("=[\"simpleUserA\", \"simpleUserB\"]"));

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  public void testVerifyDueDateExpression() {
    handler.verifyDueDateExpression(expr -> assertThat(expr).isEqualTo("wrong due date expression"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyDueDateExpression(expr -> assertThat(expr).isEqualTo("=\"2023-02-17T00:00:00Z\""));

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  public void testVerifyFollowUpDateExpression() {
    handler.verifyFollowUpDateExpression(expr -> assertThat(expr).isEqualTo("wrong follow-up date expression"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyFollowUpDateExpression(expr -> assertThat(expr).isEqualTo("=\"2023-02-18T00:00:00Z\""));

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
      instance.isWaitingAt(processInstanceEvent, "emptyUserTask");
      instance.apply(processInstanceEvent, emptyHandler);
      instance.hasPassed(processInstanceEvent, "emptyUserTask");
      instance.hasPassed(processInstanceEvent, "endEvent");
      instance.isCompleted(processInstanceEvent);
    }
  }
}
