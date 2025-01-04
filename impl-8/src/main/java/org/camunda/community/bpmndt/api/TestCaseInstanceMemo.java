package org.camunda.community.bpmndt.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordType;
import io.camunda.zeebe.protocol.record.intent.IncidentIntent;
import io.camunda.zeebe.protocol.record.intent.JobIntent;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;
import io.camunda.zeebe.protocol.record.intent.ProcessMessageSubscriptionIntent;
import io.camunda.zeebe.protocol.record.intent.SignalSubscriptionIntent;
import io.camunda.zeebe.protocol.record.intent.TimerIntent;
import io.camunda.zeebe.protocol.record.value.BpmnElementType;
import io.camunda.zeebe.protocol.record.value.ErrorType;
import io.camunda.zeebe.protocol.record.value.IncidentRecordValue;
import io.camunda.zeebe.protocol.record.value.JobRecordValue;
import io.camunda.zeebe.protocol.record.value.ProcessInstanceRecordValue;
import io.camunda.zeebe.protocol.record.value.ProcessMessageSubscriptionRecordValue;
import io.camunda.zeebe.protocol.record.value.SignalSubscriptionRecordValue;
import io.camunda.zeebe.protocol.record.value.TimerRecordValue;

/**
 * Memorizes all relevant events of the Zeebe test engine.
 */
public class TestCaseInstanceMemo {

  final List<ElementMemo> elements = new ArrayList<>();
  final List<IncidentMemo> incidents = new ArrayList<>(0);
  final List<JobMemo> jobs = new ArrayList<>();
  final List<MessageSubscriptionMemo> messageSubscriptions = new ArrayList<>(0);
  final List<ElementMemo> multiInstanceElements = new ArrayList<>(0);
  final List<ProcessInstanceMemo> processInstances = new ArrayList<>();
  final List<SignalSubscriptionMemo> signalSubscriptions = new ArrayList<>(0);
  final List<TimerMemo> timers = new ArrayList<>(0);

  final Map<Long, Long> keys = new HashMap<>();

  private final boolean printRecordStreamEnabled;

  TestCaseInstanceMemo(boolean printRecordStreamEnabled) {
    this.printRecordStreamEnabled = printRecordStreamEnabled;
  }

  void apply(Record<?> record) {
    if (record.getRecordType() != RecordType.EVENT) {
      return;
    }

    if (printRecordStreamEnabled) {
      System.out.println(record);
    }

    try {
      switch (record.getValueType()) {
        case INCIDENT:
          handleIncident(record);
          break;
        case JOB:
          handleJob(record);
          break;
        case PROCESS_MESSAGE_SUBSCRIPTION:
          handleMessageSubscription(record);
          break;
        case PROCESS_INSTANCE:
          handleProcessInstance(record);
          break;
        case SIGNAL_SUBSCRIPTION:
          handleSignalSubscription(record);
          break;
        case TIMER:
          handleTimer(record);
          break;
      }
    } catch (RuntimeException e) {
      System.err.printf("failed to process record: %s%n: %s", record, e.getMessage());
    }
  }

  void clear() {
    elements.clear();
    incidents.clear();
    jobs.clear();
    messageSubscriptions.clear();
    multiInstanceElements.clear();
    processInstances.clear();
    signalSubscriptions.clear();
    timers.clear();

    keys.clear();
  }

  long getProcessInstanceKey(long key) {
    var current = key;
    while (true) {
      var parent = keys.get(current);
      if (parent == null) {
        break;
      }
      current = parent;
    }
    return current;
  }

  private void handleIncident(Record<?> record) {
    var recordValue = (IncidentRecordValue) record.getValue();

    if (record.getIntent() == IncidentIntent.CREATED) {
      var incident = new IncidentMemo();
      incident.elementId = recordValue.getElementId();
      incident.errorMessage = recordValue.getErrorMessage();
      incident.errorType = recordValue.getErrorType();
      incident.processInstanceKey = recordValue.getProcessInstanceKey();

      incidents.add(incident);
    }
  }

  private void handleJob(Record<?> record) {
    var recordValue = (JobRecordValue) record.getValue();

    if (record.getIntent() == JobIntent.CREATED) {
      var flowScopeKey = keys.get(recordValue.getElementInstanceKey());

      var job = new JobMemo();
      job.elementId = recordValue.getElementId();
      job.elementInstanceKey = recordValue.getElementInstanceKey();
      job.flowScopeKey = flowScopeKey != null ? flowScopeKey : -1;
      job.key = record.getKey();
      job.processInstanceKey = recordValue.getProcessInstanceKey();
      job.retries = recordValue.getRetries();
      job.state = JobIntent.CREATED;
      job.type = recordValue.getType();

      if (recordValue.getCustomHeaders() != null) {
        job.customHeaders = new HashMap<>();
        job.customHeaders.putAll(recordValue.getCustomHeaders());
      }

      jobs.add(job);
    }
  }

  private void handleMessageSubscription(Record<?> record) {
    var recordValue = (ProcessMessageSubscriptionRecordValue) record.getValue();

    if (record.getIntent() == ProcessMessageSubscriptionIntent.CREATED) {
      var flowScopeKey = keys.get(recordValue.getElementInstanceKey());

      var messageSubscription = new MessageSubscriptionMemo();
      messageSubscription.correlationKey = recordValue.getCorrelationKey();
      messageSubscription.elementId = recordValue.getElementId();
      messageSubscription.elementInstanceKey = recordValue.getElementInstanceKey();
      messageSubscription.flowScopeKey = flowScopeKey != null ? flowScopeKey : -1;
      messageSubscription.messageName = recordValue.getMessageName();
      messageSubscription.processInstanceKey = recordValue.getProcessInstanceKey();

      messageSubscriptions.add(messageSubscription);
    }
  }

  private void handleProcessInstance(Record<?> record) {
    var state = (ProcessInstanceIntent) record.getIntent();
    var recordValue = (ProcessInstanceRecordValue) record.getValue();

    if (recordValue.getBpmnElementType() != BpmnElementType.PROCESS && state == ProcessInstanceIntent.ELEMENT_ACTIVATING) {
      keys.put(record.getKey(), recordValue.getFlowScopeKey());
      return;
    }

    boolean relevantState = state == ProcessInstanceIntent.ELEMENT_ACTIVATED
        || state == ProcessInstanceIntent.ELEMENT_COMPLETED
        || state == ProcessInstanceIntent.ELEMENT_TERMINATED;

    if (!relevantState) {
      return;
    }

    if (recordValue.getBpmnElementType() == BpmnElementType.PROCESS) {
      var processInstance = new ProcessInstanceMemo();
      processInstance.bpmnProcessId = recordValue.getBpmnProcessId();
      processInstance.key = recordValue.getProcessInstanceKey();
      processInstance.parentElementInstanceKey = recordValue.getParentElementInstanceKey();
      processInstance.state = state;

      processInstances.add(processInstance);
    } else {
      var element = new ElementMemo();
      element.flowScopeKey = recordValue.getFlowScopeKey();
      element.id = recordValue.getElementId();
      element.key = record.getKey();
      element.processInstanceKey = recordValue.getProcessInstanceKey();
      element.state = state;

      if (recordValue.getBpmnElementType() == BpmnElementType.MULTI_INSTANCE_BODY) {
        multiInstanceElements.add(element);
      } else {
        elements.add(element);
      }
    }
  }

  private void handleSignalSubscription(Record<?> record) {
    var recordValue = (SignalSubscriptionRecordValue) record.getValue();

    if (record.getIntent() == SignalSubscriptionIntent.CREATED) {
      var flowScopeKey = keys.get(recordValue.getCatchEventInstanceKey());

      var signalSubscription = new SignalSubscriptionMemo();
      signalSubscription.elementId = recordValue.getCatchEventId();
      signalSubscription.elementInstanceKey = recordValue.getCatchEventInstanceKey();
      signalSubscription.flowScopeKey = flowScopeKey != null ? flowScopeKey : -1;
      signalSubscription.processInstanceKey = getProcessInstanceKey(recordValue.getCatchEventInstanceKey());
      signalSubscription.signalName = recordValue.getSignalName();

      signalSubscriptions.add(signalSubscription);
    }
  }

  private void handleTimer(Record<?> record) {
    var recordValue = (TimerRecordValue) record.getValue();

    if (record.getIntent() == TimerIntent.CREATED) {
      var flowScopeKey = keys.get(recordValue.getElementInstanceKey());

      var timer = new TimerMemo();
      timer.creationDate = record.getTimestamp();
      timer.dueDate = recordValue.getDueDate();
      timer.elementId = recordValue.getTargetElementId();
      timer.elementInstanceKey = recordValue.getElementInstanceKey();
      timer.flowScopeKey = flowScopeKey != null ? flowScopeKey : -1;
      timer.processInstanceKey = recordValue.getProcessInstanceKey();

      timers.add(timer);
    }
  }

  static class ElementMemo {

    long flowScopeKey;
    String id;
    long key;
    long processInstanceKey;
    ProcessInstanceIntent state;
  }

  static class IncidentMemo {

    String elementId;
    String errorMessage;
    ErrorType errorType;
    long processInstanceKey;
  }

  static class JobMemo {

    String elementId;
    long elementInstanceKey;
    long flowScopeKey;
    long key;
    long processInstanceKey;
    int retries;
    JobIntent state;
    String type;

    private Map<String, String> customHeaders;

    String getCustomHeader(String key) {
      return customHeaders != null ? customHeaders.get(key) : null;
    }
  }

  static class MessageSubscriptionMemo {

    String correlationKey;
    String elementId;
    long elementInstanceKey;
    long flowScopeKey;
    String messageName;
    long processInstanceKey;
  }

  static class ProcessInstanceMemo {

    String bpmnProcessId;
    long key;
    long parentElementInstanceKey;
    ProcessInstanceIntent state;
  }

  static class SignalSubscriptionMemo {

    String elementId;
    long elementInstanceKey;
    long flowScopeKey;
    long processInstanceKey;
    String signalName;
  }

  static class TimerMemo {

    long creationDate;
    long dueDate;
    String elementId;
    long elementInstanceKey;
    long flowScopeKey;
    long processInstanceKey;
  }
}