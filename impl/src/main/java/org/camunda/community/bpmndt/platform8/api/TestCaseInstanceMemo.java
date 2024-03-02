package org.camunda.community.bpmndt.platform8.api;

import java.util.HashMap;
import java.util.Map;

import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordType;
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.intent.JobIntent;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;
import io.camunda.zeebe.protocol.record.value.BpmnElementType;
import io.camunda.zeebe.protocol.record.value.JobRecordValue;
import io.camunda.zeebe.protocol.record.value.ProcessInstanceRecordValue;

class TestCaseInstanceMemo {

  final Map<Long, ProcessInstanceMemo> processInstances = new HashMap<>();

  void apply(Record<?> record) {
    if (record.getRecordType() != RecordType.EVENT) {
      return;
    }

    System.out.println(record);

    if (record.getValueType() == ValueType.JOB) {
      JobRecordValue recordValue = (JobRecordValue) record.getValue();

      if (record.getIntent() == JobIntent.CREATED) {
        JobMemo job = new JobMemo();
        job.id = recordValue.getElementId();
        job.state = JobIntent.CREATED;
        job.type = recordValue.getType();

        if (recordValue.getCustomHeaders() != null) {
          job.customHeaders = new HashMap<>();
          job.customHeaders.putAll(recordValue.getCustomHeaders());
        }

        ProcessInstanceMemo processInstance = processInstances.get(recordValue.getProcessInstanceKey());
        processInstance.jobs.put(job.id, job);
      }
    }

    if (record.getValueType() == ValueType.PROCESS_INSTANCE) {
      ProcessInstanceRecordValue recordValue = (ProcessInstanceRecordValue) record.getValue();

      if (record.getIntent() == ProcessInstanceIntent.ELEMENT_ACTIVATED) {
        if (recordValue.getBpmnElementType() == BpmnElementType.PROCESS) {
          ProcessInstanceMemo processInstance = new ProcessInstanceMemo();
          processInstance.key = recordValue.getProcessInstanceKey();
          processInstance.state = ProcessInstanceIntent.ELEMENT_ACTIVATED;
          processInstances.put(processInstance.key, processInstance);
        }
      }

      if (record.getIntent() == ProcessInstanceIntent.ELEMENT_COMPLETED || record.getIntent() == ProcessInstanceIntent.ELEMENT_TERMINATED) {
        ProcessInstanceIntent state = (ProcessInstanceIntent) record.getIntent();

        if (recordValue.getBpmnElementType() == BpmnElementType.PROCESS) {
          ProcessInstanceMemo processInstance = processInstances.get(recordValue.getProcessInstanceKey());
          processInstance.state = state;
        } else {
          ElementMemo element = new ElementMemo();
          element.id = recordValue.getElementId();
          element.state = state;

          ProcessInstanceMemo processInstance = processInstances.get(recordValue.getProcessInstanceKey());
          processInstance.elements.put(element.id, element);
        }
      }
    }
  }

  static class ElementMemo {

    String id;
    ProcessInstanceIntent state;
  }

  static class JobMemo {

    String id;
    JobIntent state;
    String type;

    private Map<String, String> customHeaders;

    String getCustomHeader(String key) {
      return customHeaders != null ? customHeaders.get(key) : null;
    }

    boolean hasCustomHeader(String key) {
      return customHeaders != null && customHeaders.containsKey(key);
    }
  }

  static class ProcessInstanceMemo {

    final Map<String, ElementMemo> elements = new HashMap<>();
    final Map<String, JobMemo> jobs = new HashMap<>();

    long key;
    ProcessInstanceIntent state;
  }
}
