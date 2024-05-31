package org.camunda.community.bpmndt.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordType;
import io.camunda.zeebe.protocol.record.intent.JobIntent;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;
import io.camunda.zeebe.protocol.record.intent.ProcessMessageSubscriptionIntent;
import io.camunda.zeebe.protocol.record.intent.SignalSubscriptionIntent;
import io.camunda.zeebe.protocol.record.intent.TimerIntent;
import io.camunda.zeebe.protocol.record.value.BpmnElementType;
import io.camunda.zeebe.protocol.record.value.JobRecordValue;
import io.camunda.zeebe.protocol.record.value.ProcessInstanceRecordValue;
import io.camunda.zeebe.protocol.record.value.ProcessMessageSubscriptionRecordValue;
import io.camunda.zeebe.protocol.record.value.SignalSubscriptionRecordValue;
import io.camunda.zeebe.protocol.record.value.TimerRecordValue;

/**
 * Memorizes all relevant events of the Zeebe test engine.
 */
public class TestCaseInstanceMemo {

  private final Map<Long, ProcessInstanceMemo> processInstances = new HashMap<>();
  private final List<SignalSubscriptionMemo> signalSubscriptions = new ArrayList<>(0);

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
      System.err.printf("failed to process record: %s%n", record);
    }
  }

  Collection<ProcessInstanceMemo> getProcessInstances() {
    return processInstances.values();
  }

  ProcessInstanceMemo getProcessInstance(long processInstanceKey) {
    return processInstances.get(processInstanceKey);
  }

  Collection<SignalSubscriptionMemo> getSignalSubscriptions() {
    return signalSubscriptions;
  }

  private void handleJob(Record<?> record) {
    var recordValue = (JobRecordValue) record.getValue();

    if (record.getIntent() == JobIntent.CREATED) {
      var job = new JobMemo();
      job.id = recordValue.getElementId();
      job.key = record.getKey();
      job.retries = recordValue.getRetries();
      job.state = JobIntent.CREATED;
      job.type = recordValue.getType();

      if (recordValue.getCustomHeaders() != null) {
        job.customHeaders = new HashMap<>();
        job.customHeaders.putAll(recordValue.getCustomHeaders());
      }

      processInstances.get(recordValue.getProcessInstanceKey()).put(job);
    }
  }

  private void handleMessageSubscription(Record<?> record) {
    var recordValue = (ProcessMessageSubscriptionRecordValue) record.getValue();

    if (record.getIntent() == ProcessMessageSubscriptionIntent.CREATED) {
      var messageSubscription = new MessageSubscriptionMemo();
      messageSubscription.correlationKey = recordValue.getCorrelationKey();
      messageSubscription.id = recordValue.getElementId();
      messageSubscription.messageName = recordValue.getMessageName();
      messageSubscription.state = ProcessMessageSubscriptionIntent.CREATED;

      processInstances.get(recordValue.getProcessInstanceKey()).put(messageSubscription);
    }
  }

  private void handleProcessInstance(Record<?> record) {
    var state = (ProcessInstanceIntent) record.getIntent();
    var recordValue = (ProcessInstanceRecordValue) record.getValue();

    if (recordValue.getBpmnElementType() == BpmnElementType.PROCESS) {
      if (state == ProcessInstanceIntent.ELEMENT_ACTIVATED) {
        var processInstance = new ProcessInstanceMemo();
        processInstance.bpmnProcessId = recordValue.getBpmnProcessId();
        processInstance.key = recordValue.getProcessInstanceKey();
        processInstance.parentElementInstanceKey = recordValue.getParentElementInstanceKey();
        processInstance.state = ProcessInstanceIntent.ELEMENT_ACTIVATED;

        processInstances.put(processInstance.key, processInstance);
      } else if (state == ProcessInstanceIntent.ELEMENT_COMPLETED || state == ProcessInstanceIntent.ELEMENT_TERMINATED) {
        var processInstance = processInstances.get(recordValue.getProcessInstanceKey());
        processInstance.state = state;
      }
    } else if (recordValue.getBpmnElementType() == BpmnElementType.MULTI_INSTANCE_BODY) {
      var element = new ElementMemo();
      element.id = recordValue.getElementId();
      element.key = record.getKey();
      element.state = state;

      processInstances.get(recordValue.getProcessInstanceKey()).putMultiInstance(element);
    } else {
      var element = new ElementMemo();
      element.id = recordValue.getElementId();
      element.key = record.getKey();
      element.state = state;

      processInstances.get(recordValue.getProcessInstanceKey()).put(element);
    }
  }

  private void handleSignalSubscription(Record<?> record) {
    SignalSubscriptionRecordValue recordValue = (SignalSubscriptionRecordValue) record.getValue();

    if (record.getIntent() == SignalSubscriptionIntent.CREATED) {
      var signalSubscription = new SignalSubscriptionMemo();
      signalSubscription.catchEventId = recordValue.getCatchEventId();
      signalSubscription.signalName = recordValue.getSignalName();

      signalSubscriptions.add(signalSubscription);
    }
  }

  private void handleTimer(Record<?> record) {
    var recordValue = (TimerRecordValue) record.getValue();

    if (record.getIntent() == TimerIntent.CREATED) {
      var timer = new TimerMemo();
      timer.creationDate = record.getTimestamp();
      timer.dueDate = recordValue.getDueDate();
      timer.id = recordValue.getTargetElementId();
      timer.state = TimerIntent.CREATED;

      var processInstance = processInstances.get(recordValue.getProcessInstanceKey());
      if (processInstance != null) { // can be null, in case of timer start events
        processInstance.put(timer);
      }
    }
  }

  static class ElementMemo {

    String id;
    long key;
    ProcessInstanceIntent state;
  }

  static class JobMemo {

    String id;
    long key;
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
    String id;
    String messageName;
    ProcessMessageSubscriptionIntent state;
  }

  static class ProcessInstanceMemo {

    private final Map<String, ElementMemo> elements = new HashMap<>();

    private Map<String, JobMemo> jobs;
    private Map<String, MessageSubscriptionMemo> messageSubscriptions;
    private Map<String, ElementMemo> multiInstances;
    private Map<String, TimerMemo> timers;

    String bpmnProcessId;
    long key;
    long parentElementInstanceKey;
    ProcessInstanceIntent state;

    ElementMemo getElement(String elementId) {
      return elements.get(elementId);
    }

    JobMemo getJob(String elementId) {
      return jobs != null ? jobs.get(elementId) : null;
    }

    MessageSubscriptionMemo getMessageSubscription(String elementId) {
      return messageSubscriptions != null ? messageSubscriptions.get(elementId) : null;
    }

    ElementMemo getMultiInstance(String elementId) {
      return multiInstances != null ? multiInstances.get(elementId) : null;
    }

    TimerMemo getTimer(String elementId) {
      return timers != null ? timers.get(elementId) : null;
    }

    void put(ElementMemo element) {
      elements.put(element.id, element);
    }

    void put(JobMemo job) {
      if (jobs == null) {
        jobs = new HashMap<>();
      }
      jobs.put(job.id, job);
    }

    void put(MessageSubscriptionMemo messageSubscription) {
      if (messageSubscriptions == null) {
        messageSubscriptions = new HashMap<>();
      }
      messageSubscriptions.put(messageSubscription.id, messageSubscription);
    }

    void put(TimerMemo timer) {
      if (timers == null) {
        timers = new HashMap<>();
      }
      timers.put(timer.id, timer);
    }

    void putMultiInstance(ElementMemo element) {
      if (multiInstances == null) {
        multiInstances = new HashMap<>();
      }
      multiInstances.put(element.id, element);
    }
  }

  static class SignalSubscriptionMemo {

    String catchEventId;
    String signalName;
  }

  static class TimerMemo {

    long creationDate;
    long dueDate;
    String id;
    TimerIntent state;
  }
}
