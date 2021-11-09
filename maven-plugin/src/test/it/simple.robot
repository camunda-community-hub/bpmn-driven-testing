* Settings
Library  OperatingSystem
Library  Process

* Test Cases
mvn clean test
  [Tags]  xignore
  ${result}=  Run process
  ...  mvn  -B  -f  ${CURDIR}/simple/pom.xml  clean  package  -Dplugin.version\=${VERSION}
  ...  shell=True  stdout=${TEMP}/simple.out  stderr=STDOUT

  Log  ${result.stdout}

  # plugin executed
  Should contain  ${result.stdout}  bpmn-driven-testing-maven-plugin:${VERSION}:generator
  # test source directory added
  Should contain  ${result.stdout}  Adding test source directory:

  # BPMN files found
  Should contain  ${result.stdout}  Found BPMN file: noTestCases.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simple.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleAsync.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleCallActivity.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleCollaboration.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleConditionalCatchEvent.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleExternalTask.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleMessageCatchEvent.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleReceiveTask.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleSignalCatchEvent.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleSubProcess.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleTimerCatchEvent.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleUserTask.bpmn

  # test cases generated
  Should contain  ${result.stdout}  Process: noTestCases
  Should contain  ${result.stdout}  No test cases defined

  Should contain  ${result.stdout}  Process: simple
  Should contain  ${result.stdout}  Generating test case 'startEvent__endEvent'

  ${testSources}  Set variable  ${CURDIR}/simple/target/bpmndt

  # test cases written
  Should contain  ${result.stdout}  Writing test cases
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/simple/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/simpleasync/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/simplecallactivity/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/simplecollaboration/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/simpleconditionalcatchevent/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/simpleexternaltask/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/simplemessagecatchevent/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/simplereceivetask/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/simplesignalcatchevent/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/simplesubprocess/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/simpletimercatchevent/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/simpleusertask/TC_startEvent__endEvent.java

  File should exist  ${testSources}/generated/simple/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simpleasync/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simplecallactivity/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simplecollaboration/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simpleconditionalcatchevent/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simpleexternaltask/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simplemessagecatchevent/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simplereceivetask/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simplesignalcatchevent/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simplesubprocess/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simpletimercatchevent/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simpleusertask/TC_startEvent__endEvent.java

  # API classes written
  Should contain  ${result.stdout}  Writing API classes
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/AbstractJUnit4TestRule.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/CallActivityDefinition.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/CallActivityHandler.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/EventHandler.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/ExternalTaskHandler.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/JobHandler.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/MultiInstanceHandler.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/TestCaseInstance.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/TestCaseExecutor.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/UserTaskHandler.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/cfg/BpmndtCallActivityBehavior.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/cfg/BpmndtParseListener.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/cfg/BpmndtProcessEnginePlugin.java

  File should exist  ${testSources}/org/camunda/community/bpmndt/api/AbstractJUnit4TestRule.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/CallActivityDefinition.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/CallActivityHandler.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/EventHandler.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/ExternalTaskHandler.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/JobHandler.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/MultiInstanceHandler.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/TestCaseInstance.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/TestCaseExecutor.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/UserTaskHandler.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/cfg/BpmndtCallActivityBehavior.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/cfg/BpmndtParseListener.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/cfg/BpmndtProcessEnginePlugin.java

  # tests executed
  Should contain  ${result.stdout}  Running org.example.it.SimpleTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleAsyncTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleCallActivityTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleCollaborationTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleConditionalCatchEventTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleExternalTaskTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleMessageCatchEventTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleReceiveTaskTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleSignalCatchEventTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleSubProcessTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleTimerCatchEventTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleUserTaskTest
  # tests executed successfully
  Should contain  ${result.stdout}  Failures: 0, Errors: 0, Skipped: 0

  Should be equal as integers  ${result.rc}  0
