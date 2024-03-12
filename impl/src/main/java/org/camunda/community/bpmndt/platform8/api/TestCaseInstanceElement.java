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

    private String errorCode;
    private String type;

    /**
     * Returns the error code of the attached error boundary event that is subsequent test case element.
     *
     * @return The error code or {@code null}, if the next element is not an error boundary event.
     */
    public String getErrorCode() {
      return errorCode;
    }

    /**
     * Returns the job type - e.g. the service task type, handled by a Zeebe client job worker.
     *
     * @return The job's type.
     */
    public String getType() {
      return type;
    }

    public void setErrorCode(String errorCode) {
      this.errorCode = errorCode;
    }

    public void setType(String type) {
      this.type = type;
    }
  }

  public static class MessageEventElement extends TestCaseInstanceElement {

    private String correlationKey;
    private String messageName;

    public String getCorrelationKey() {
      return correlationKey;
    }

    public String getMessageName() {
      return messageName;
    }

    public void setCorrelationKey(String correlationKey) {
      this.correlationKey = correlationKey;
    }

    public void setMessageName(String messageName) {
      this.messageName = messageName;
    }
  }

  public static class TimerEventElement extends TestCaseInstanceElement {

    private String timeDate;
    private String timeDuration;

    public String getTimeDate() {
      return timeDate;
    }

    public String getTimeDuration() {
      return timeDuration;
    }

    public void setTimeDate(String timeDate) {
      this.timeDate = timeDate;
    }

    public void setTimeDuration(String timeDuration) {
      this.timeDuration = timeDuration;
    }
  }

  public static class UserTaskElement extends TestCaseInstanceElement {

    private String assignee;
    private String candidateGroups;
    private String candidateUsers;
    private String dueDate;
    private String errorCode;
    private String followUpDate;

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

    public String getErrorCode() {
      return errorCode;
    }

    public String getFollowUpDate() {
      return followUpDate;
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

    public void setErrorCode(String errorCode) {
      this.errorCode = errorCode;
    }

    public void setFollowUpDate(String followUpDate) {
      this.followUpDate = followUpDate;
    }
  }
}
