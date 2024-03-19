package org.camunda.community.bpmndt.platform8.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceElement.UserTaskElement;
import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceMemo.JobMemo;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.JsonMapper;
import io.camunda.zeebe.client.api.command.ThrowErrorCommandStep1.ThrowErrorCommandStep2;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.protocol.Protocol;

public class UserTaskHandler {

  private final UserTaskElement element;

  private final Map<String, Object> variableMap = new HashMap<>();

  private Consumer<ProcessInstanceAssert> verifier;
  private BiConsumer<ZeebeClient, Long> action;
  private String errorMessage;
  private Object variables;

  private Consumer<String> assigneeExpressionConsumer;
  private Consumer<String> candidateGroupsExpressionConsumer;
  private Consumer<String> candidateUsersExpressionConsumer;
  private Consumer<String> dueDateExpressionConsumer;
  private Consumer<String> followUpDateExpressionConsumer;

  private String expectedAssignee;
  private List<String> expectedCandidateGroups;
  private List<String> expectedCandidateUsers;
  private String expectedFormKey;

  private Consumer<String> assigneeConsumer;
  private Consumer<List<String>> candidateGroupsConsumer;
  private Consumer<List<String>> candidateUsersConsumer;
  private Consumer<String> dueDateConsumer;
  private Consumer<String> followUpDateConsumer;
  private Consumer<String> formKeyConsumer;


  UserTaskHandler(UserTaskElement element) {
    this.element = element;

    complete();
  }

  @SuppressWarnings("unchecked")
  void apply(TestCaseInstance instance, ProcessInstanceEvent processInstanceEvent) {
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

    JobMemo job = instance.getJob(processInstanceEvent, element.getId());
    if (!Protocol.USER_TASK_JOB_TYPE.equals(job.type)) {
      String message = "expected job %s to be of type '%s', but was '%s'";
      throw new AssertionError(String.format(message, element.getId(), Protocol.USER_TASK_JOB_TYPE, job.type));
    }

    String assignee = job.getCustomHeader(Protocol.USER_TASK_ASSIGNEE_HEADER_NAME);
    if (expectedAssignee != null && !expectedAssignee.equals(assignee)) {
      String message = "expected user task %s to have assignee '%s', but was '%s'";
      throw new AssertionError(String.format(message, element.getId(), expectedAssignee, assignee));
    }
    if (assigneeConsumer != null) {
      assigneeConsumer.accept(assignee);
    }

    JsonMapper jsonMapper = instance.client.getConfiguration().getJsonMapper();

    String candidateGroupsHeader = job.getCustomHeader(Protocol.USER_TASK_CANDIDATE_GROUPS_HEADER_NAME);

    List<String> candidateGroups;
    if (candidateGroupsHeader == null) {
      candidateGroups = Collections.emptyList();
    } else {
      candidateGroups = jsonMapper.fromJson(candidateGroupsHeader, List.class);
    }

    if (expectedCandidateGroups != null) {
      if (expectedCandidateGroups.size() != candidateGroups.size()) {
        String message = "expected user task %s to have %d candidate group(s), but it has %d";
        throw new AssertionError(String.format(message, element.getId(), expectedCandidateGroups.size(), candidateGroups.size()));
      }

      for (int i = 0; i < candidateGroups.size(); i++) {
        if (!expectedCandidateGroups.get(i).equals(candidateGroups.get(i))) {
          String message = "expected user task %s to have candidate group #%d '%s', but it was '%s'";
          throw new AssertionError(String.format(message, element.getId(), i, expectedCandidateGroups.get(i), candidateGroups.get(i)));
        }
      }
    }
    if (candidateGroupsConsumer != null) {
      candidateGroupsConsumer.accept(candidateGroups);
    }

    String candidateUsersHeader = job.getCustomHeader(Protocol.USER_TASK_CANDIDATE_USERS_HEADER_NAME);

    List<String> candidateUsers;
    if (candidateUsersHeader == null) {
      candidateUsers = Collections.emptyList();
    } else {
      candidateUsers = jsonMapper.fromJson(candidateUsersHeader, List.class);
    }

    if (expectedCandidateUsers != null) {
      if (expectedCandidateUsers.size() != candidateUsers.size()) {
        String message = "expected user task %s to have %d candidate user(s), but it has %d";
        throw new AssertionError(String.format(message, element.getId(), expectedCandidateUsers.size(), candidateUsers.size()));
      }

      for (int i = 0; i < candidateUsers.size(); i++) {
        if (!expectedCandidateUsers.get(i).equals(candidateUsers.get(i))) {
          String message = "expected user task %s to have candidate user #%d '%s', but it was '%s'";
          throw new AssertionError(String.format(message, element.getId(), i, expectedCandidateUsers.get(i), candidateUsers.get(i)));
        }
      }
    }
    if (candidateUsersConsumer != null) {
      candidateUsersConsumer.accept(candidateUsers);
    }

    String dueDate = job.getCustomHeader(Protocol.USER_TASK_DUE_DATE_HEADER_NAME);
    if (dueDateConsumer != null) {
      dueDateConsumer.accept(dueDate);
    }

    String followUpDate = job.getCustomHeader(Protocol.USER_TASK_FOLLOW_UP_DATE_HEADER_NAME);
    if (followUpDateConsumer != null) {
      followUpDateConsumer.accept(followUpDate);
    }

    String formKey = job.getCustomHeader(Protocol.USER_TASK_FORM_KEY_HEADER_NAME);
    if (expectedFormKey != null && !expectedFormKey.equals(formKey)) {
      String message = "expected user task %s to have fork mey '%s', but was '%s'";
      throw new AssertionError(String.format(message, element.getId(), expectedFormKey, formKey));
    }
    if (formKeyConsumer != null) {
      formKeyConsumer.accept(formKey);
    }

    if (action != null) {
      action.accept(instance.client, job.key);

      if (element.getErrorCode() != null) {
        instance.hasTerminated(processInstanceEvent, element.getId());
      }
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
   * Completes the user task using a custom action, when the process instance is waiting at the corresponding element.
   *
   * @param action A specific action that accepts a {@link ZeebeClient} and the related job key.
   * @see ZeebeClient#newCompleteCommand(long)
   */
  public void complete(BiConsumer<ZeebeClient, Long> action) {
    if (action == null) {
      throw new IllegalArgumentException("action is null");
    }
    this.action = action;
  }

  void complete(ZeebeClient client, long jobKey) {
    if (variables != null) {
      client.newCompleteCommand(jobKey).variables(variables).send();
    } else {
      client.newCompleteCommand(jobKey).variables(variableMap).send();
    }
  }

  /**
   * Customizes the handler, using the given {@link Consumer} function. This method can be used to apply a common customization needed for different test
   * cases.
   *
   * <pre>
   * tc.handleUserTask().customize(this::prepare);
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
   * Throws an BPMN error using the given error message as well as the specified variables.
   */
  public void throwBpmnError(String errorMessage) {
    if (element.getErrorCode() == null) {
      throw new IllegalStateException("the subsequent test case element is not an error boundary event");
    }

    this.errorMessage = errorMessage;
    this.action = this::throwBpmnError;
  }

  void throwBpmnError(ZeebeClient client, long jobKey) {
    ThrowErrorCommandStep2 throwErrorCommandStep2 = client.newThrowErrorCommand(jobKey)
        .errorCode(element.getErrorCode())
        .errorMessage(errorMessage);

    if (variables != null) {
      throwErrorCommandStep2.variables(variables).send().join();
    } else {
      throwErrorCommandStep2.variables(variableMap).send().join();
    }
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
   * Verifies that the user task has a specific assignee.
   *
   * @param expectedAssignee The expected assignee.
   * @return The handler.
   */
  public UserTaskHandler verifyAssignee(String expectedAssignee) {
    this.expectedAssignee = expectedAssignee;
    return this;
  }

  /**
   * Verifies that the user task has a specific assignee, using a consumer.
   *
   * @param assigneeConsumer A consumer asserting the assignee.
   * @return The handler.
   */
  public UserTaskHandler verifyAssignee(Consumer<String> assigneeConsumer) {
    this.assigneeConsumer = assigneeConsumer;
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
   * Verifies that the user task has specific candidate groups.
   *
   * @param expectedCandidateGroups The expected candidate groups.
   * @return The handler.
   */
  public UserTaskHandler verifyCandidateGroups(List<String> expectedCandidateGroups) {
    this.expectedCandidateGroups = expectedCandidateGroups;
    return this;
  }

  /**
   * Verifies that the user task has specific candidate groups, using a consumer.
   *
   * @param candidateGroupsConsumer A consumer asserting the candidate groups - a list of {@code String}s.
   * @return The handler.
   */
  public UserTaskHandler verifyCandidateGroups(Consumer<List<String>> candidateGroupsConsumer) {
    this.candidateGroupsConsumer = candidateGroupsConsumer;
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
   * Verifies that the user task has specific candidate users.
   *
   * @param expectedCandidateUsers The expected candidate users.
   * @return The handler.
   */
  public UserTaskHandler verifyCandidateUsers(List<String> expectedCandidateUsers) {
    this.expectedCandidateUsers = expectedCandidateUsers;
    return this;
  }

  /**
   * Verifies that the user task has specific candidate users, using a consumer.
   *
   * @param candidateUsersConsumer A consumer asserting the candidate users - a list of {@code String}s.
   * @return The handler.
   */
  public UserTaskHandler verifyCandidateUsers(Consumer<List<String>> candidateUsersConsumer) {
    this.candidateUsersConsumer = candidateUsersConsumer;
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
   * Verifies that the user task has a specific due date, using a consumer.
   *
   * @param dueDateConsumer A consumer asserting the due date.
   * @return The handler.
   */
  public UserTaskHandler verifyDueDate(Consumer<String> dueDateConsumer) {
    this.dueDateConsumer = dueDateConsumer;
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
   * Verifies that the user task has a specific follow-up date, using a consumer.
   *
   * @param followUpDateConsumer A consumer asserting the follow-up date.
   * @return The handler.
   */
  public UserTaskHandler verifyFollowUpDate(Consumer<String> followUpDateConsumer) {
    this.followUpDateConsumer = followUpDateConsumer;
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
   * Applies no action at the user task's wait state. This is required when waiting for events (e.g. message, signal or timer events) that are attached as
   * boundary events on the element itself or on the surrounding scope (e.g. embedded subprocess).
   */
  public void waitForBoundaryEvent() {
    action = null;
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
