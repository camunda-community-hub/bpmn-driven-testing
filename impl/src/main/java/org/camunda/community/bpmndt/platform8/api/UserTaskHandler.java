package org.camunda.community.bpmndt.platform8.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceElement.UserTaksElement;
import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceMemo.JobMemo;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.protocol.Protocol;

public class UserTaskHandler {

  private final UserTaksElement element;

  private final Map<String, Object> variableMap = new HashMap<>();

  private Consumer<ProcessInstanceAssert> verifier;
  private io.camunda.zeebe.client.api.worker.JobHandler action;
  private Object variables;

  private Consumer<String> assigneeExpressionConsumer;
  private Consumer<String> candidateGroupsExpressionConsumer;
  private Consumer<String> candidateUsersExpressionConsumer;
  private Consumer<String> dueDateExpressionConsumer;
  private Consumer<String> followUpDateExpressionConsumer;

  private String expectedAssignee;
  private List<String> expectedCandidateGroups;
  private List<String> expectedCandidateUsers;
  private String expectedDueDate;
  private String expectedFollowUpDate;
  private String expectedFormKey;

  private Consumer<String> assigneeConsumer;
  private Consumer<List<String>> candidateGroupsConsumer;
  private Consumer<List<String>> candidateUsersConsumer;
  private Consumer<String> dueDateConsumer;
  private Consumer<String> followUpDateConsumer;
  private Consumer<String> formKeyConsumer;


  UserTaskHandler(UserTaksElement element) {
    this.element = element;

    action = this::complete;
  }

  void apply(TestCaseInstance instance, ProcessInstanceEvent processInstanceEvent) {
    JobMemo job = instance.getJob(processInstanceEvent, element.getId());
    if (!Protocol.USER_TASK_JOB_TYPE.equals(job.type)) {
      String message = "expected job %s to be of type '%s', but was '%s'";
      throw new AssertionError(String.format(message, element.getId(), Protocol.USER_TASK_JOB_TYPE, job.type));
    }

    if (verifier != null) {
      verifier.accept(BpmnAssert.assertThat(processInstanceEvent));
    }

    if (assigneeExpressionConsumer != null) {
      assigneeExpressionConsumer.accept(element.getAssignee());
    }
    if (candidateGroupsExpressionConsumer != null) {
      candidateGroupsExpressionConsumer.accept(element.getCandidateGroups());
    }
    if (candidateUsersExpressionConsumer != null) {
      candidateUsersExpressionConsumer.accept(element.getCandidateUsers());
    }
    if (dueDateExpressionConsumer != null) {
      dueDateExpressionConsumer.accept(element.getDueDate());
    }
    if (followUpDateExpressionConsumer != null) {
      followUpDateExpressionConsumer.accept(element.getFollowUpDate());
    }

    String assignee = job.getCustomHeader(Protocol.USER_TASK_ASSIGNEE_HEADER_NAME);
    if (expectedAssignee != null && !expectedAssignee.equals(assignee)) {
      String message = "expected user task %s to have assignee '%s', but was '%s'";
      throw new AssertionError(String.format(message, element.getId(), expectedAssignee, element.getAssignee()));
    }
    if (assigneeConsumer != null) {
      assigneeConsumer.accept(assignee);
    }

    if (expectedFormKey != null && !expectedFormKey.equals(element.getFormKey())) {
      String message = "expected user task %s to have follow-up date '%s', but was '%s'";
      throw new AssertionError(String.format(message, element.getId(), expectedFormKey, element.getFormKey()));
    }
    if (formKeyConsumer != null) {
      formKeyConsumer.accept(element.getFormKey());
    }

    if (action != null) {
      try (JobWorker worker = instance.client.newWorker().jobType(job.type).handler(action).open()) {
        instance.hasPassed(processInstanceEvent, element.getId());
      }
    }

    System.out.println(job.getCustomHeader(Protocol.USER_TASK_ASSIGNEE_HEADER_NAME));
    System.out.println(job.getCustomHeader(Protocol.USER_TASK_CANDIDATE_GROUPS_HEADER_NAME));
    System.out.println(job.getCustomHeader(Protocol.USER_TASK_CANDIDATE_USERS_HEADER_NAME));
    System.out.println(job.getCustomHeader(Protocol.USER_TASK_FORM_KEY_HEADER_NAME));
    System.out.println(job.getCustomHeader(Protocol.USER_TASK_DUE_DATE_HEADER_NAME));
    System.out.println(job.getCustomHeader(Protocol.USER_TASK_FOLLOW_UP_DATE_HEADER_NAME));

    List<String> candidateGroups = instance.client.getConfiguration().getJsonMapper()
        .fromJson(job.getCustomHeader(Protocol.USER_TASK_CANDIDATE_GROUPS_HEADER_NAME),
            List.class);

    System.out.println(candidateGroups);

  }

  /**
   * Completes the user task, when the process instance is waiting at the corresponding element, using specified variables.
   *
   * @see #withVariable(String, Object)
   * @see #withVariables(Object)
   * @see #withVariableMap(Map)
   */
  public void complete() {
    action = this::complete;
  }

  /**
   * Completes the user task with a custom action, when the process instance is waiting at the corresponding element.
   *
   * @param action A specific action that accepts the related {@link JobClient} and {@link ActivatedJob} - a Zeebe job handler.
   */
  public void complete(io.camunda.zeebe.client.api.worker.JobHandler action) {
    this.action = action;
  }

  void complete(JobClient client, ActivatedJob job) {
    if (variables != null) {
      client.newCompleteCommand(job).variables(variables).send();
    } else {
      client.newCompleteCommand(job).variables(variableMap).send();
    }
  }

  /**
   * Customizes the handler, using the given {@link Consumer} function. This method can be used to apply a common customization needed for different test
   * cases.
   *
   * <pre>
   * tc.handleUserTask().customize(this::prepareJob);
   * </pre>
   *
   * @param customizer A function that accepts a {@link UserTaskHandler}.
   * @return The handler.
   */
  public UserTaskHandler customize(Consumer<UserTaskHandler> customizer) {
    if (customizer != null) {
      customizer.accept(this);
    }
    return this;
  }

  /**
   * Verifies the user task's waiting state.
   *
   * @param verifier Verifier that accepts an {@link ProcessInstanceAssert} instance.
   * @return The handler.
   */
  public UserTaskHandler verify(Consumer<ProcessInstanceAssert> verifier) {
    this.verifier = verifier;
    return this;
  }

  /**
   * Verifies that the user task has a specific assignee FEEL expression (see "Assignment" section), using a consumer function.
   *
   * @param assigneeExpressionConsumer A consumer asserting the assignee expression.
   * @return The handler.
   */
  public UserTaskHandler verifyAssigneeExpression(Consumer<String> assigneeExpressionConsumer) {
    this.assigneeExpressionConsumer = assigneeExpressionConsumer;
    return this;
  }

  /**
   * Verifies that the user task has specific candidate groups FEEL expression (see "Assignment" section), using a consumer function.
   *
   * @param candidateGroupsExpressionConsumer A consumer asserting the candidate groups expression.
   * @return The handler.
   */
  public UserTaskHandler verifyCandidateGroupsExpression(Consumer<String> candidateGroupsExpressionConsumer) {
    this.candidateGroupsExpressionConsumer = candidateGroupsExpressionConsumer;
    return this;
  }

  /**
   * Verifies that the user task has specific candidate users FEEL expression (see "Assignment" section), using a consumer function.
   *
   * @param candidateUsersExpressionConsumer A consumer asserting the candidate users expression.
   * @return The handler.
   */
  public UserTaskHandler verifyCandidateUsersExpression(Consumer<String> candidateUsersExpressionConsumer) {
    this.candidateUsersExpressionConsumer = candidateUsersExpressionConsumer;
    return this;
  }

  /**
   * Verifies that the user task has a specific due date FEEL expression (see "Assignment" section), using a consumer function.
   *
   * @param dueDateExpressionConsumer A consumer asserting the due date expression.
   * @return The handler.
   */
  public UserTaskHandler verifyDueDateExpression(Consumer<String> dueDateExpressionConsumer) {
    this.dueDateExpressionConsumer = dueDateExpressionConsumer;
    return this;
  }

  /**
   * Verifies that the user task has a specific follow-up date FEEL expression (see "Assignment" section), using a consumer function.
   *
   * @param followUpDateExpressionConsumer A consumer asserting the follow-up date expression.
   * @return The handler.
   */
  public UserTaskHandler verifyFollowUpDateExpression(Consumer<String> followUpDateExpressionConsumer) {
    this.followUpDateExpressionConsumer = followUpDateExpressionConsumer;
    return this;
  }

  /**
   * Verifies that the user task has a specific form key (see "Form" section).
   *
   * @param expectedFormKey The expected form key.
   * @return The handler.
   */
  public UserTaskHandler verifyFormKey(String expectedFormKey) {
    this.expectedFormKey = expectedFormKey;
    return this;
  }

  /**
   * Verifies that the user task has a specific form key (see "Form" section), using a consumer function.
   *
   * @param formKeyConsumer A consumer asserting the form key.
   * @return The handler.
   */
  public UserTaskHandler verifyFormKey(Consumer<String> formKeyConsumer) {
    this.formKeyConsumer = formKeyConsumer;
    return this;
  }

  /**
   * Sets a variable that is used to complete the user task.
   *
   * @param name  The name of the variable.
   * @param value The variable's value.
   * @return The handler.
   * @see #complete()
   */
  public UserTaskHandler withVariable(String name, Object value) {
    if (variables != null) {
      throw new IllegalStateException("either use an object (POJO) as variables or a variable map");
    }
    variableMap.put(name, value);
    return this;
  }

  /**
   * Sets an object as variables that is used to complete the user task.
   *
   * @param variables The variables as POJO.
   * @return The handler.
   * @see #complete()
   */
  public UserTaskHandler withVariables(Object variables) {
    if (!variableMap.isEmpty()) {
      throw new IllegalStateException("either use an object (POJO) as variables or a variable map");
    }
    this.variables = variables;
    return this;
  }

  /**
   * Sets variables that are used to complete the user task.
   *
   * @param variableMap A map of variables.
   * @return The handler.
   * @see #complete()
   */
  public UserTaskHandler withVariableMap(Map<String, Object> variableMap) {
    if (variables != null) {
      throw new IllegalStateException("either use an object (POJO) as variables or a variable map");
    }
    this.variableMap.putAll(variableMap);
    return this;
  }
}
