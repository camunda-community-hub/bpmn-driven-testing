package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;

import org.camunda.community.bpmndt.api.TestCaseInstanceElement.UserTaskElement;
import org.camunda.community.bpmndt.test.TestPaths;
import org.camunda.community.bpmndt.test.TestVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.Form;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class UserTaskTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

  private UserTaskHandler handler;
  private UserTaskHandler handlerWithLinkedForm;

  private Form form;

  @BeforeEach
  void setUp() {
    var forms = client.newDeployResourceCommand()
        .addResourceFile(TestPaths.simple("simpleUserTask.form").toAbsolutePath().toString())
        .send()
        .join()
        .getForm();

    assertThat(forms).hasSize(1);
    form = forms.get(0);

    var element = new UserTaskElement();
    element.id = "userTask";
    element.assignee = "=\"simpleAssignee\"";
    element.candidateGroups = "=[\"simpleGroupA\", \"simpleGroupB\"]";
    element.candidateUsers = "=[\"simpleUserA\", \"simpleUserB\"]";
    element.dueDate = "=\"2023-02-17T00:00:00Z\"";
    element.followUpDate = "=\"2023-02-18T00:00:00Z\"";

    handler = new UserTaskHandler(element);

    var elementWithLinkedForm = new UserTaskElement();
    elementWithLinkedForm.id = "userTaskWithLinkedForm";

    handlerWithLinkedForm = new UserTaskHandler(elementWithLinkedForm);

    var elementWithEmbeddedForm = new UserTaskElement();
    elementWithEmbeddedForm.id = "userTaskWithEmbeddedForm";
  }

  @Test
  void testExecute() {
    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testExecuteWithCustomAction() {
    handler.execute((client, userTaskKey) -> client.newCompleteUserTaskCommand(userTaskKey).send());

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testCompleteActionWithVariables() {
    var variables = new TestVariables();
    variables.setX("test");
    variables.setY(1);
    variables.setZ(true);

    handler.withVariables(variables).complete();

    tc.createExecutor(client, processTestContext).verify(piAssert -> {
      piAssert.isCompleted();

      piAssert.hasVariable("x", "test");
      piAssert.hasVariable("y", 1);
      piAssert.hasVariable("z", true);
    }).execute();
  }

  @Test
  void testCompleteActionWithVariableMap() {
    var variableMap = new HashMap<String, Object>();
    variableMap.put("y", 1);
    variableMap.put("z", true);

    handler
        .withVariable("x", "test")
        .withVariableMap(variableMap)
        .complete();

    tc.createExecutor(client, processTestContext).verify(piAssert -> {
      piAssert.isCompleted();

      piAssert.hasVariable("x", "test");
      piAssert.hasVariable("y", 1);
      piAssert.hasVariable("z", true);
    }).execute();
  }

  @Test
  void testVerify() {
    handler.verify(processInstanceAssert -> processInstanceAssert.hasVariable("x", "test"));

    tc.createExecutor(client, processTestContext)
        .withVariable("x", "test")
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  @Test
  void testVerifyAssignee() {
    handler.verifyAssignee("wrong assignee");

    var e = assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());
    assertThat(e).hasMessageThat().contains("'wrong assignee'");
    assertThat(e).hasMessageThat().contains("'simpleAssignee'");

    handler.verifyAssignee("simpleAssignee");

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();

    handler.verifyAssignee(assignee -> assertThat(assignee).isEqualTo("wrong assignee"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());

    handler.verifyAssignee(assignee -> assertThat(assignee).isEqualTo("simpleAssignee"));

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyAssigneeExpression() {
    handler.verifyAssigneeExpression(expr -> assertThat(expr).isEqualTo("wrong assignee expression"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());

    handler.verifyAssigneeExpression(expr -> assertThat(expr).isEqualTo("=\"simpleAssignee\""));

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyCandidateGroups() {
    handler.verifyCandidateGroups(Arrays.asList("wrong group 1", "wrong group 2"));

    var e = assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());
    assertThat(e).hasMessageThat().contains("candidate group #0 'wrong group 1'");
    assertThat(e).hasMessageThat().contains("'simpleGroupA'");

    handler.verifyCandidateGroups(Arrays.asList("simpleGroupA", "simpleGroupB"));

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();

    handler.verifyCandidateGroups(groups -> assertThat(groups).containsExactly("wrong group 1", "wrong group 2").inOrder());

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());

    handler.verifyCandidateGroups(groups -> assertThat(groups).containsExactly("simpleGroupA", "simpleGroupB").inOrder());

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyCandidateGroupsExpression() {
    handler.verifyCandidateGroupsExpression(expr -> assertThat(expr).isEqualTo("wrong candidate groups expression"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());

    handler.verifyCandidateGroupsExpression(expr -> assertThat(expr).isEqualTo("=[\"simpleGroupA\", \"simpleGroupB\"]"));

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyCandidateUsers() {
    handler.verifyCandidateUsers(Arrays.asList("wrong user 1", "wrong user 2"));

    var e = assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());
    assertThat(e).hasMessageThat().contains("candidate user #0 'wrong user 1'");
    assertThat(e).hasMessageThat().contains("'simpleUserA'");

    handler.verifyCandidateUsers(Arrays.asList("simpleUserA", "simpleUserB"));

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();

    handler.verifyCandidateUsers(users -> assertThat(users).containsExactly("wrong user 1", "wrong user 2").inOrder());

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());

    handler.verifyCandidateUsers(users -> assertThat(users).containsExactly("simpleUserA", "simpleUserB").inOrder());

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyCandidateUsersExpression() {
    handler.verifyCandidateUsersExpression(expr -> assertThat(expr).isEqualTo("wrong candidate users expression"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());

    handler.verifyCandidateUsersExpression(expr -> assertThat(expr).isEqualTo("=[\"simpleUserA\", \"simpleUserB\"]"));

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyDueDate() {
    handler.verifyDueDate(dueDate -> assertThat(dueDate).isEqualTo("wrong due date"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());

    handler.verifyDueDate(dueDate -> assertThat(dueDate.toString()).isEqualTo("2023-02-17T00:00Z"));

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyDueDateExpression() {
    handler.verifyDueDateExpression(expr -> assertThat(expr).isEqualTo("wrong due date expression"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());

    handler.verifyDueDateExpression(expr -> assertThat(expr).isEqualTo("=\"2023-02-17T00:00:00Z\""));

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyFollowUpDate() {
    handler.verifyFollowUpDate(followUpDate -> assertThat(followUpDate).isEqualTo("wrong follow-up date"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());

    handler.verifyFollowUpDate(followUpDate -> assertThat(followUpDate.toString()).isEqualTo("2023-02-18T00:00Z"));

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyFollowUpDateExpression() {
    handler.verifyFollowUpDateExpression(expr -> assertThat(expr).isEqualTo("wrong follow-up date expression"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());

    handler.verifyFollowUpDateExpression(expr -> assertThat(expr).isEqualTo("=\"2023-02-18T00:00:00Z\""));

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  void testVerifyFormKey() {
    handler.verifyFormKey("wrong form key");

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());

    handler.verifyFormKey((String) null);
    handler.verifyFormKey(formKey -> assertThat(formKey).isEqualTo("wrong form key"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(client, processTestContext).execute());

    handler.verifyFormKey("simpleFormKey");
    handler.verifyFormKey(formKey -> assertThat(formKey).isEqualTo("simpleFormKey"));

    handlerWithLinkedForm.verifyFormKey(String.valueOf(form.getFormKey()));

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, long processInstanceKey) {
      instance.hasPassed(processInstanceKey, "startEvent");
      instance.isWaitingAt(processInstanceKey, "userTask");
      instance.apply(processInstanceKey, handler);
      instance.hasPassed(processInstanceKey, "userTask");
      instance.isWaitingAt(processInstanceKey, "userTaskWithLinkedForm");
      instance.apply(processInstanceKey, handlerWithLinkedForm);
      instance.hasPassed(processInstanceKey, "userTaskWithLinkedForm");
      instance.hasPassed(processInstanceKey, "endEvent");
    }

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
        return Files.newInputStream(TestPaths.simple("simpleUserTask.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }
  }
}
