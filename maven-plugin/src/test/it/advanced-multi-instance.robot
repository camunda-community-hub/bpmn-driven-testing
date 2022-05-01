* Settings
Library  OperatingSystem
Library  Process

* Test Cases
mvn clean test
  [Tags]  xignore
  ${result}=  Run process
  ...  mvn  -B  -f  ${CURDIR}/advanced-multi-instance/pom.xml  clean  test  -Dplugin.version\=${VERSION}
  ...  shell=True  stdout=${TEMP}/advanced-multi-instance.out  stderr=STDOUT

  Log  ${result.stdout}

  # plugin executed
  Should contain  ${result.stdout}  bpmn-driven-testing-maven-plugin:${VERSION}:generator

  ${testSources}  Set variable  ${CURDIR}/advanced-multi-instance/target/bpmndt

  # Multi instance handler classes generated and written
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/async/MultiInstanceManualTaskHandler.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/callactivity/MultiInstanceCallActivityHandler.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/callactivityerror/MultiInstanceCallActivityHandler.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/callactivitytimer/MultiInstanceCallActivityHandler.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/parallel/MultiInstanceManualTaskHandler.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/sequential/MultiInstanceManualTaskHandler.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/usertask/MultiInstanceUserTaskHandler.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/usertaskerror/MultiInstanceUserTaskHandler.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/usertaskmessage/MultiInstanceUserTaskHandler.java

  File should exist  ${testSources}/generated/async/MultiInstanceManualTaskHandler.java
  File should exist  ${testSources}/generated/callactivity/MultiInstanceCallActivityHandler.java
  File should exist  ${testSources}/generated/callactivityerror/MultiInstanceCallActivityHandler.java
  File should exist  ${testSources}/generated/callactivitytimer/MultiInstanceCallActivityHandler.java
  File should exist  ${testSources}/generated/parallel/MultiInstanceManualTaskHandler.java
  File should exist  ${testSources}/generated/sequential/MultiInstanceManualTaskHandler.java
  File should exist  ${testSources}/generated/usertask/MultiInstanceUserTaskHandler.java
  File should exist  ${testSources}/generated/usertaskerror/MultiInstanceUserTaskHandler.java
  File should exist  ${testSources}/generated/usertaskmessage/MultiInstanceUserTaskHandler.java

  # tests executed
  Should contain  ${result.stdout}  Running org.example.it.AsyncTest
  Should contain  ${result.stdout}  Running org.example.it.CallActivityTest
  Should contain  ${result.stdout}  Running org.example.it.CallActivityErrorTest
  Should contain  ${result.stdout}  Running org.example.it.CallActivityTimerTest
  Should contain  ${result.stdout}  Running org.example.it.ParallelTest
  Should contain  ${result.stdout}  Running org.example.it.SequentialTest
  Should contain  ${result.stdout}  Running org.example.it.UserTaskTest
  Should contain  ${result.stdout}  Running org.example.it.UserTaskErrorTest
  Should contain  ${result.stdout}  Running org.example.it.UserTaskMessageTest

  # tests executed successfully
  Should contain  ${result.stdout}  Failures: 0, Errors: 0, Skipped: 0

  Should be equal as integers  ${result.rc}  0
