package org.camunda.community.bpmndt.platform8.api;

import java.io.IOException;
import java.util.List;

import org.camunda.community.bpmndt.platform8.api.TestCaseInstanceElement.UserTaksElement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.camunda.zeebe.protocol.Protocol;

public class UserTaskHandler {

  private final UserTaksElement element;

  private String expectedAssignee;
  private String expectedCandidateGroups;
  private String expectedCandidateUsers;
  private String expectedDueDate;
  private String expectedFollowUpDate;
  private String expectedFormKey;

  private String expectedEvaluatedAssignee;
  private List<String> expectedEvaluatedCandidateGroups;
  private List<String> expectedEvaluatedCandidateUsers;
  private String expectedEvaluatedDueDate;
  private String expectedEvaluatedFollowUpDate;

  UserTaskHandler(UserTaksElement element) {
    this.element = element;
  }

  void apply(TestCaseInstance instance, long processInstanceKey) {
    var job = instance.getJob(processInstanceKey, element.getId());
    if (!Protocol.USER_TASK_JOB_TYPE.equals(job.type)) {
      throw new AssertionError("expected job %s to be of type %s, but was %s".formatted(element.getId(), Protocol.USER_TASK_JOB_TYPE, job.type));
    }

    System.out.println(job.getCustomHeader(Protocol.USER_TASK_ASSIGNEE_HEADER_NAME));
    System.out.println(job.getCustomHeader(Protocol.USER_TASK_CANDIDATE_GROUPS_HEADER_NAME));
    System.out.println(job.getCustomHeader(Protocol.USER_TASK_CANDIDATE_USERS_HEADER_NAME));
    System.out.println(job.getCustomHeader(Protocol.USER_TASK_FORM_KEY_HEADER_NAME));
    System.out.println(job.getCustomHeader(Protocol.USER_TASK_DUE_DATE_HEADER_NAME));
    System.out.println(job.getCustomHeader(Protocol.USER_TASK_FOLLOW_UP_DATE_HEADER_NAME));

    try {
      var candidateGroups = new ObjectMapper().readValue(job.getCustomHeader(Protocol.USER_TASK_CANDIDATE_GROUPS_HEADER_NAME),
          TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));

      System.out.println(candidateGroups);
    } catch (IOException e) {

    }
  }
}
