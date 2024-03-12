package org.camunda.community.bpmndt.platform8.api;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceElement.UserTaskElement;
import org.camunda.community.bpmndt.test.Platform8TestPaths;
import org.camunda.community.bpmndt.test.TestVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.Form;
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
  private UserTaskHandler handlerWithLinkedForm;
  private UserTaskHandler handlerWithEmbeddedForm;

  private Form form;

  @BeforeEach
  public void setUp() {
    List<Form> forms = client.newDeployResourceCommand()
        .addResourceFile(Platform8TestPaths.simple("simpleUserTask.form").toAbsolutePath().toString())
        .send()
        .join()
        .getForm();

    assertThat(forms).hasSize(1);
    form = forms.get(0);

    UserTaskElement element = new UserTaskElement();
    element.setAssignee("=\"simpleAssignee\"");
    element.setCandidateGroups("=[\"simpleGroupA\", \"simpleGroupB\"]");
    element.setCandidateUsers("=[\"simpleUserA\", \"simpleUserB\"]");
    element.setDueDate("=\"2023-02-17T00:00:00Z\"");
    element.setFollowUpDate("=\"2023-02-18T00:00:00Z\"");
    element.setId("userTask");

    handler = new UserTaskHandler(element);

    UserTaskElement elementWithLinkedForm = new UserTaskElement();
    elementWithLinkedForm.setId("userTaskWithLinkedForm");

    handlerWithLinkedForm = new UserTaskHandler(elementWithLinkedForm);

    UserTaskElement elementWithEmbeddedForm = new UserTaskElement();
    elementWithEmbeddedForm.setId("userTaskWithEmbeddedForm");

    handlerWithEmbeddedForm = new UserTaskHandler(elementWithEmbeddedForm);
  }

  @Test
  public void testExecute() {
    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  public void testCompleteAction() {
    handler.complete((client, job) -> client.newCompleteCommand(job).send());

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  public void testCompleteActionWithVariables() {
    TestVariables variables = new TestVariables();
    variables.setX("test");
    variables.setY(1);
    variables.setZ(true);

    handler.withVariables(variables).complete();

    tc.createExecutor(engine).verify(piAssert -> {
      piAssert.isCompleted();

      piAssert.hasVariableWithValue("x", "test");
      piAssert.hasVariableWithValue("y", 1);
      piAssert.hasVariableWithValue("z", true);
    }).execute();
  }

  @Test
  public void testCompleteActionWithVariableMap() {
    Map<String, Object> variableMap = new HashMap<>();
    variableMap.put("y", 1);
    variableMap.put("z", true);

    handler
        .withVariable("x", "test")
        .withVariableMap(variableMap)
        .complete();

    tc.createExecutor(engine).verify(piAssert -> {
      piAssert.isCompleted();

      piAssert.hasVariableWithValue("x", "test");
      piAssert.hasVariableWithValue("y", 1);
      piAssert.hasVariableWithValue("z", true);
    }).execute();
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
  public void testVerifyDueDate() {
    handler.verifyDueDate(dueDate -> assertThat(dueDate).isEqualTo("wrong due date"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyDueDate(dueDate -> assertThat(dueDate).isEqualTo("2023-02-17T00:00Z"));

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
  public void testVerifyFollowUpDate() {
    handler.verifyFollowUpDate(followUpDate -> assertThat(followUpDate).isEqualTo("wrong follow-up date"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyFollowUpDate(followUpDate -> assertThat(followUpDate).isEqualTo("2023-02-18T00:00Z"));

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  public void testVerifyFollowUpDateExpression() {
    handler.verifyFollowUpDateExpression(expr -> assertThat(expr).isEqualTo("wrong follow-up date expression"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyFollowUpDateExpression(expr -> assertThat(expr).isEqualTo("=\"2023-02-18T00:00:00Z\""));

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  @Test
  public void testVerifyFormKey() {
    handler.verifyFormKey("wrong form key");

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyFormKey((String) null);
    handler.verifyFormKey(formKey -> assertThat(formKey).isEqualTo("wrong form key"));

    assertThrows(AssertionError.class, () -> tc.createExecutor(engine).execute());

    handler.verifyFormKey("simpleFormKey");
    handler.verifyFormKey(formKey -> assertThat(formKey).isEqualTo("simpleFormKey"));

    handlerWithLinkedForm.verifyFormKey(String.valueOf(form.getFormKey()));
    handlerWithEmbeddedForm.verifyFormKey("camunda-forms:bpmn:UserTaskForm_0e64hjp");

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }

  private class TestCase extends AbstractJUnit5TestCase {

    @Override
    protected void execute(TestCaseInstance instance, ProcessInstanceEvent processInstanceEvent) {
      instance.hasPassed(processInstanceEvent, "startEvent");
      instance.isWaitingAt(processInstanceEvent, "userTask");
      instance.apply(processInstanceEvent, handler);
      instance.hasPassed(processInstanceEvent, "userTask");
      instance.isWaitingAt(processInstanceEvent, "userTaskWithLinkedForm");
      instance.apply(processInstanceEvent, handlerWithLinkedForm);
      instance.hasPassed(processInstanceEvent, "userTaskWithLinkedForm");
      instance.isWaitingAt(processInstanceEvent, "userTaskWithEmbeddedForm");
      instance.apply(processInstanceEvent, handlerWithEmbeddedForm);
      instance.hasPassed(processInstanceEvent, "userTaskWithEmbeddedForm");
      instance.hasPassed(processInstanceEvent, "endEvent");
      instance.isCompleted(processInstanceEvent);
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
        return Files.newInputStream(Platform8TestPaths.simple("simpleUserTask.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }
  }
}
