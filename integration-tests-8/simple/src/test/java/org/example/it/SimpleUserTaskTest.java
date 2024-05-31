package org.example.it;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.simpleusertask.TC_startEvent__endEvent;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class SimpleUserTaskTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  ZeebeTestEngine engine;
  ZeebeClient client;

  @Test
  void testExecute() {
    var forms = client.newDeployResourceCommand()
        .addResourceFromClasspath("simpleUserTask.form")
        .send()
        .join()
        .getForm();

    var form = forms.get(0);

    tc.handleUserTask()
        .verifyAssignee(assignee -> assertThat(assignee).isEqualTo("simpleAssignee"))
        .verifyAssigneeExpression(expr -> assertThat(expr).isEqualTo("=\"simpleAssignee\""))
        .verifyCandidateGroups(groups -> assertThat(groups).containsExactly("simpleGroupA", "simpleGroupB").inOrder())
        .verifyCandidateGroupsExpression(expr -> assertThat(expr).isEqualTo("=[\"simpleGroupA\", \"simpleGroupB\"]"))
        .verifyCandidateUsers(users -> assertThat(users).containsExactly("simpleUserA", "simpleUserB").inOrder())
        .verifyCandidateUsersExpression(expr -> assertThat(expr).isEqualTo("=[\"simpleUserA\", \"simpleUserB\"]"))
        .verifyDueDate(dueDate -> assertThat(dueDate).isEqualTo("2023-02-17T00:00Z"))
        .verifyDueDateExpression(expr -> assertThat(expr).isEqualTo("=\"2023-02-17T00:00:00Z\""))
        .verifyFollowUpDate(followUpDate -> assertThat(followUpDate).isEqualTo("2023-02-18T00:00Z"))
        .verifyFollowUpDateExpression(expr -> assertThat(expr).isEqualTo("=\"2023-02-18T00:00:00Z\""))
        .verifyFormKey("simpleFormKey")
        .verifyFormKey(formKey -> assertThat(formKey).isEqualTo("simpleFormKey"));

    tc.handleUserTaskWithLinkedForm().verifyFormKey(String.valueOf(form.getFormKey()));

    tc.handleUserTaskWithEmbeddedForm().verifyFormKey("camunda-forms:bpmn:UserTaskForm_0e64hjp");

    tc.createExecutor(engine).verify(ProcessInstanceAssert::isCompleted).execute();
  }
}
