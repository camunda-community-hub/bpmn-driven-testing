package org.camunda.community.bpmndt.platform8.api;

/**
 * BPMN element, containing the information from a parsed BPMN model (design time) at runtime, used by specific handler.
 */
public abstract class TestCaseInstanceElement {

  private String id;

  /**
   * Returns the BPMN element ID.
   *
   * @return The ID of the element.
   */
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public static class JobElement extends TestCaseInstanceElement {

    private String type;

    /**
     * Returns the job type - e.g. the service task type, handled by a Zeebe client job worker.
     *
     * @return The job's type.
     */
    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }
  }

  public static class UserTaksElement extends TestCaseInstanceElement {

    private String assignee;
    private String candidateGroups;
    private String candidateUsers;
    private String dueDate;
    private String followUpDate;
    private String formKey;

    public String getAssignee() {
      return assignee;
    }

    public String getCandidateGroups() {
      return candidateGroups;
    }

    public String getCandidateUsers() {
      return candidateUsers;
    }

    public String getDueDate() {
      return dueDate;
    }

    public String getFollowUpDate() {
      return followUpDate;
    }

    public String getFormKey() {
      return formKey;
    }

    public void setAssignee(String assignee) {
      this.assignee = assignee;
    }

    public void setCandidateGroups(String candidateGroups) {
      this.candidateGroups = candidateGroups;
    }

    public void setCandidateUsers(String candidateUsers) {
      this.candidateUsers = candidateUsers;
    }

    public void setDueDate(String dueDate) {
      this.dueDate = dueDate;
    }

    public void setFollowUpDate(String followUpDate) {
      this.followUpDate = followUpDate;
    }

    public void setFormKey(String formKey) {
      this.formKey = formKey;
    }
  }
}
