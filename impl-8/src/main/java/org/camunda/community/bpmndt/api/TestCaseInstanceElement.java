package org.camunda.community.bpmndt.api;

import java.util.Map;

/**
 * BPMN element, containing the information from a parsed BPMN model (design time) at runtime, used by specific handler.
 */
public abstract class TestCaseInstanceElement {

  /**
   * The ID of the BPMN element.
   */
  public String id;

  public static class CallActivityElement extends TestCaseInstanceElement {

    public String processId;
    public boolean propagateAllChildVariables;
    public boolean propagateAllParentVariables;
  }

  public static class JobElement extends TestCaseInstanceElement {

    public String retries;
    public String type;
  }

  public static class MessageEventElement extends TestCaseInstanceElement {

    public String correlationKey;
    public String messageName;
  }

  public static class MultiInstanceElement extends TestCaseInstanceElement {

    public boolean sequential;
  }

  public static class OutboundConnectorElement extends TestCaseInstanceElement {

    public Map<String, String> inputs;
    public Map<String, String> outputs;
    public String taskDefinitionType;
    public Map<String, String> taskHeaders;
  }

  public static class SignalEventElement extends TestCaseInstanceElement {

    public String signalName;
  }

  public static class TimerEventElement extends TestCaseInstanceElement {

    public String timeDate;
    public String timeDuration;
  }

  public static class UserTaskElement extends TestCaseInstanceElement {

    public String assignee;
    public String candidateGroups;
    public String candidateUsers;
    public String dueDate;
    public String followUpDate;
  }
}
