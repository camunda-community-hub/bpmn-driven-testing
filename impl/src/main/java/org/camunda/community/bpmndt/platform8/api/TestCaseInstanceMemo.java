package org.camunda.community.bpmndt.platform8.api;

import java.util.ArrayList;
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

public class TestCaseInstanceMemo {

  final Map<Long, ProcessInstanceMemo> processInstances = new HashMap<>();
  final List<SignalSubscriptionMemo> signalSubscriptions = new ArrayList<>(0);

  void apply(Record<?> record) {
    if (record.getRecordType() != RecordType.EVENT) {
      return;
    }

    // TODO remove
    System.out.println(record);

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
  }

  private void handleJob(Record<?> record) {
    JobRecordValue recordValue = (JobRecordValue) record.getValue();

    if (record.getIntent() == JobIntent.CREATED) {
      JobMemo job = new JobMemo();
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
    ProcessMessageSubscriptionRecordValue recordValue = (ProcessMessageSubscriptionRecordValue) record.getValue();

    if (record.getIntent() == ProcessMessageSubscriptionIntent.CREATED) {
      MessageSubscriptionMemo messageSubscription = new MessageSubscriptionMemo();
      messageSubscription.correlationKey = recordValue.getCorrelationKey();
      messageSubscription.id = recordValue.getElementId();
      messageSubscription.messageName = recordValue.getMessageName();
      messageSubscription.state = ProcessMessageSubscriptionIntent.CREATED;

      processInstances.get(recordValue.getProcessInstanceKey()).put(messageSubscription);
    }
  }

  private void handleProcessInstance(Record<?> record) {
    ProcessInstanceIntent state = (ProcessInstanceIntent) record.getIntent();
    ProcessInstanceRecordValue recordValue = (ProcessInstanceRecordValue) record.getValue();

    if (recordValue.getBpmnElementType() == BpmnElementType.PROCESS) {
      if (state == ProcessInstanceIntent.ELEMENT_ACTIVATED) {
        ProcessInstanceMemo processInstance = new ProcessInstanceMemo();
        processInstance.key = recordValue.getProcessInstanceKey();
        processInstance.state = ProcessInstanceIntent.ELEMENT_ACTIVATED;

        processInstances.put(processInstance.key, processInstance);
      } else if (state == ProcessInstanceIntent.ELEMENT_COMPLETED || state == ProcessInstanceIntent.ELEMENT_TERMINATED) {
        ProcessInstanceMemo processInstance = processInstances.get(recordValue.getProcessInstanceKey());
        processInstance.state = state;
      }
    } else {
      ElementMemo element = new ElementMemo();
      element.id = recordValue.getElementId();
      element.key = record.getKey();
      element.state = state;

      processInstances.get(recordValue.getProcessInstanceKey()).put(element);
    }
  }

  private void handleSignalSubscription(Record<?> record) {
    SignalSubscriptionRecordValue recordValue = (SignalSubscriptionRecordValue) record.getValue();

    if (record.getIntent() == SignalSubscriptionIntent.CREATED) {
      SignalSubscriptionMemo signalSubscription = new SignalSubscriptionMemo();
      signalSubscription.catchEventInstanceKey = recordValue.getCatchEventInstanceKey();
      signalSubscription.signalName = recordValue.getSignalName();

      signalSubscriptions.add(signalSubscription);
    }
  }

  private void handleTimer(Record<?> record) {
    TimerRecordValue recordValue = (TimerRecordValue) record.getValue();

    if (record.getIntent() == TimerIntent.CREATED) {
      TimerMemo timer = new TimerMemo();
      timer.creationDate = record.getTimestamp();
      timer.dueDate = recordValue.getDueDate();
      timer.id = recordValue.getTargetElementId();
      timer.state = TimerIntent.CREATED;

      processInstances.get(recordValue.getProcessInstanceKey()).put(timer);
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
    private Map<String, TimerMemo> timers;

    long key;
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
  }

  static class SignalSubscriptionMemo {

    long catchEventInstanceKey;
    String signalName;
  }

  static class TimerMemo {

    long creationDate;
    long dueDate;
    String id;
    TimerIntent state;
  }
}
