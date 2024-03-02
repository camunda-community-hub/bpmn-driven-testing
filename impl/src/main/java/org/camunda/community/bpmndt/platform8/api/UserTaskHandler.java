package org.camunda.community.bpmndt.platform8.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceElement.UserTaksElement;
import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceMemo.JobMemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

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

  private String expectedAssignee;
  private Consumer<String> expectedAssigneeConsumer;
  private String expectedCandidateGroups;
  private Consumer<String> expectedCandidateGroupsConsumer;
  private String expectedCandidateUsers;
  private Consumer<String> expectedCandidateUsersConsumer;
  private String expectedDueDate;
  private Consumer<String> expectedDueDateConsumer;
  private String expectedFollowUpDate;
  private Consumer<String> expectedFollowUpDateConsumer;
  private String expectedFormKey;
  private Consumer<String> expectedFormKeyConsumer;

  private String expectedEvaluatedAssignee;
  private Consumer<String> expectedEvaluatedAssigneeConsumer;
  private List<String> expectedEvaluatedCandidateGroups;
  private Consumer<List<String>> expectedEvaluatedCandidateGroupsConsumer;
  private List<String> expectedEvaluatedCandidateUsers;
  private Consumer<List<String>> expectedEvaluatedCandidateUsersConsumer;
  private String expectedEvaluatedDueDate;
  private Consumer<String> expectedEvaluatedDueDateConsumer;
  private String expectedEvaluatedFollowUpDate;
  private Consumer<String> expectedEvaluatedFollowUpDateConsumer;

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

    if (expectedAssignee != null && !expectedAssignee.equals(element.getAssignee())) {
      String message = "expected user task %s assignee to be '%s', but was '%s'";
      throw new AssertionError(String.format(message, element.getId(), expectedAssignee, element.getAssignee()));
    }
    if (expectedAssigneeConsumer != null) {
      expectedAssigneeConsumer.accept(element.getAssignee());
    }

    if (expectedCandidateGroups != null && !expectedCandidateGroups.equals(element.getCandidateGroups())) {
      String message = "expected user task %s candidate groups to be '%s', but was '%s'";
      throw new AssertionError(String.format(message, element.getId(), expectedCandidateGroups, element.getCandidateGroups()));
    }
    if (expectedCandidateGroupsConsumer != null) {
      expectedCandidateGroupsConsumer.accept(element.getCandidateGroups());
    }

    if (expectedCandidateUsers != null && !expectedCandidateUsers.equals(element.getCandidateUsers())) {
      String message = "expected user task %s candidate users to be '%s', but was '%s'";
      throw new AssertionError(String.format(message, element.getId(), expectedCandidateUsers, element.getCandidateUsers()));
    }
    if (expectedCandidateUsersConsumer != null) {
      expectedCandidateUsersConsumer.accept(element.getCandidateUsers());
    }

    if (expectedDueDate != null && !expectedDueDate.equals(element.getDueDate())) {
      String message = "expected user task %s due date to be '%s', but was '%s'";
      throw new AssertionError(String.format(message, element.getId(), expectedDueDate, element.getDueDate()));
    }
    if (expectedDueDateConsumer != null) {
      expectedDueDateConsumer.accept(element.getDueDate());
    }

    if (expectedFollowUpDate != null && !expectedFollowUpDate.equals(element.getFollowUpDate())) {
      String message = "expected user task %s follow-up date to be '%s', but was '%s'";
      throw new AssertionError(String.format(message, element.getId(), expectedFollowUpDate, element.getFollowUpDate()));
    }
    if (expectedFollowUpDateConsumer != null) {
      expectedFollowUpDateConsumer.accept(element.getFollowUpDate());
    }

    if (expectedFormKey != null && !expectedFormKey.equals(element.getFormKey())) {
      String message = "expected user task %s follow-up date to be '%s', but was '%s'";
      throw new AssertionError(String.format(message, element.getId(), expectedFormKey, element.getFormKey()));
    }
    if (expectedFormKeyConsumer != null) {
      expectedFormKeyConsumer.accept(element.getFormKey());
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

    try {
      List<String> candidateGroups = new ObjectMapper().readValue(job.getCustomHeader(Protocol.USER_TASK_CANDIDATE_GROUPS_HEADER_NAME),
          TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));

      System.out.println(candidateGroups);
    } catch (IOException e) {

    }
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
   * Verifies that the user task has a specific assignee (a static value or FEEL expression specified in the "Assignment" section).
   *
   * @param expectedAssignee The expected assignee.
   * @return The handler.
   */
  public UserTaskHandler verifyAssignee(String expectedAssignee) {
    this.expectedAssignee = expectedAssignee;
    return this;
  }

  /**
   * Verifies that the user task has a specific assignee (a static value or FEEL expression specified in the "Assignment" section), using a consumer function.
   *
   * @param expectedAssigneeConsumer A consumer asserting the assignee.
   * @return The handler.
   */
  public UserTaskHandler verifyAssignee(Consumer<String> expectedAssigneeConsumer) {
    this.expectedAssigneeConsumer = expectedAssigneeConsumer;
    return this;
  }

  /**
   * Verifies that the user task has specific candidate groups (a static value or FEEL expression specified in the "Assignment" section).
   *
   * @param expectedCandidateGroups The expected candidate groups.
   * @return The handler.
   */
  public UserTaskHandler verifyCandidateGroups(String expectedCandidateGroups) {
    this.expectedCandidateGroups = expectedCandidateGroups;
    return this;
  }

  /**
   * Verifies that the user task has specific candidate groups (a static value or FEEL expression specified in the "Assignment" section), using a consumer
   * function.
   *
   * @param expectedCandidateGroupsConsumer A consumer asserting the candidate groups.
   * @return The handler.
   */
  public UserTaskHandler verifyCandidateGroups(Consumer<String> expectedCandidateGroupsConsumer) {
    this.expectedCandidateGroupsConsumer = expectedCandidateGroupsConsumer;
    return this;
  }

  /**
   * Verifies that the user task has specific candidate users (a static value or FEEL expression specified in the "Assignment" section).
   *
   * @param expectedCandidateUsers The expected candidate users.
   * @return The handler.
   */
  public UserTaskHandler verifyCandidateUsers(String expectedCandidateUsers) {
    this.expectedCandidateUsers = expectedCandidateUsers;
    return this;
  }

  /**
   * Verifies that the user task has specific candidate users (a static value or FEEL expression specified in the "Assignment" section), using a consumer
   * function.
   *
   * @param expectedCandidateUsersConsumer A consumer asserting the candidate users.
   * @return The handler.
   */
  public UserTaskHandler verifyCandidateUsers(Consumer<String> expectedCandidateUsersConsumer) {
    this.expectedCandidateUsersConsumer = expectedCandidateUsersConsumer;
    return this;
  }

  /**
   * Verifies that the user task has a specific due date (a static value or FEEL expression specified in the "Assignment" section).
   *
   * @param expectedDueDate The expected due date.
   * @return The handler.
   */
  public UserTaskHandler verifyDueDate(String expectedDueDate) {
    this.expectedDueDate = expectedDueDate;
    return this;
  }

  /**
   * Verifies that the user task has a specific due date (a static value or FEEL expression specified in the "Assignment" section), using a consumer function.
   *
   * @param expectedDueDateConsumer A consumer asserting the due date.
   * @return The handler.
   */
  public UserTaskHandler verifyDueDate(Consumer<String> expectedDueDateConsumer) {
    this.expectedDueDateConsumer = expectedDueDateConsumer;
    return this;
  }

  /**
   * Verifies that the user task has a specific follow-up date (a static value or FEEL expression specified in the "Assignment" section).
   *
   * @param expectedFollowUpDate The expected follow-up date.
   * @return The handler.
   */
  public UserTaskHandler verifyFollowUpDate(String expectedFollowUpDate) {
    this.expectedFollowUpDate = expectedFollowUpDate;
    return this;
  }

  /**
   * Verifies that the user task has a specific follow-up date (a static value or FEEL expression specified in the "Assignment" section), using a consumer
   * function.
   *
   * @param expectedFollowUpDateConsumer A consumer asserting the follow-up date.
   * @return The handler.
   */
  public UserTaskHandler verifyFollowUpDate(Consumer<String> expectedFollowUpDateConsumer) {
    this.expectedFollowUpDateConsumer = expectedFollowUpDateConsumer;
    return this;
  }

  /**
   * Verifies that the user task has a specific form key (a static value or FEEL expression specified in the "Assignment" section).
   *
   * @param expectedFormKey The expected form key.
   * @return The handler.
   */
  public UserTaskHandler verifyFormKey(String expectedFormKey) {
    this.expectedFormKey = expectedFormKey;
    return this;
  }

  /**
   * Verifies that the user task has a specific form key (a static value or FEEL expression specified in the "Assignment" section), using a consumer function.
   *
   * @param expectedFormKeyConsumer A consumer asserting the form key.
   * @return The handler.
   */
  public UserTaskHandler verifyFormKey(Consumer<String> expectedFormKeyConsumer) {
    this.expectedFormKeyConsumer = expectedFormKeyConsumer;
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
