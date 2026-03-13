package org.example.it;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.simpleusertask.TC_startEvent__endEvent;
import io.camunda.client.CamundaClient;
import io.camunda.process.test.api.CamundaProcessTest;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.assertions.ProcessInstanceAssert;

@CamundaProcessTest
class SimpleUserTaskTest {

  @RegisterExtension
  TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  CamundaClient client;
  CamundaProcessTestContext processTestContext;

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
        .verifyDueDate(dueDate -> assertThat(dueDate.toString()).isEqualTo("2023-02-17T00:00Z"))
        .verifyDueDateExpression(expr -> assertThat(expr).isEqualTo("=\"2023-02-17T00:00:00Z\""))
        .verifyFollowUpDate(followUpDate -> assertThat(followUpDate.toString()).isEqualTo("2023-02-18T00:00Z"))
        .verifyFollowUpDateExpression(expr -> assertThat(expr).isEqualTo("=\"2023-02-18T00:00:00Z\""))
        .verifyFormKey("simpleFormKey")
        .verifyFormKey(formKey -> assertThat(formKey).isEqualTo("simpleFormKey"));

    tc.handleUserTaskWithLinkedForm().verifyFormKey(String.valueOf(form.getFormKey()));

    tc.createExecutor(client, processTestContext).verify(ProcessInstanceAssert::isCompleted).execute();
  }
}
