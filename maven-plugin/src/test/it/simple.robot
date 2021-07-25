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
  Should contain  ${result.stdout}  Found BPMN file: simpleExternalTask.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleMessageCatchEvent.bpmn
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
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/TC_simple__startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/TC_simpleAsync__startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/TC_simpleCallActivity__startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/TC_simpleExternalTask__startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/TC_simpleMessageCatchEvent__startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/TC_simpleSignalCatchEvent__startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/TC_simpleSubProcess__startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/TC_simpleTimerCatchEvent__startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/TC_simpleUserTask__startEvent__endEvent.java

  File should exist  ${testSources}/generated/TC_simple__startEvent__endEvent.java
  File should exist  ${testSources}/generated/TC_simpleAsync__startEvent__endEvent.java
  File should exist  ${testSources}/generated/TC_simpleCallActivity__startEvent__endEvent.java
  File should exist  ${testSources}/generated/TC_simpleExternalTask__startEvent__endEvent.java
  File should exist  ${testSources}/generated/TC_simpleMessageCatchEvent__startEvent__endEvent.java
  File should exist  ${testSources}/generated/TC_simpleSignalCatchEvent__startEvent__endEvent.java
  File should exist  ${testSources}/generated/TC_simpleSubProcess__startEvent__endEvent.java
  File should exist  ${testSources}/generated/TC_simpleTimerCatchEvent__startEvent__endEvent.java
  File should exist  ${testSources}/generated/TC_simpleUserTask__startEvent__endEvent.java

  # API classes written
  Should contain  ${result.stdout}  Writing API classes
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/AbstractJUnit4TestRule.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/CallActivityDefinition.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/CallActivityHandler.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/ExternalTaskHandler.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/IntermediateCatchEventHandler.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/JobHandler.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/TestCaseInstance.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/TestCaseExecutor.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/UserTaskHandler.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/cfg/BpmndtCallActivityBehavior.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/cfg/BpmndtParseListener.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/cfg/BpmndtProcessEnginePlugin.java

  File should exist  ${testSources}/org/camunda/community/bpmndt/api/AbstractJUnit4TestRule.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/CallActivityDefinition.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/CallActivityHandler.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/ExternalTaskHandler.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/IntermediateCatchEventHandler.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/JobHandler.java
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
  Should contain  ${result.stdout}  Running org.example.it.SimpleExternalTaskTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleMessageCatchEventTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleSignalCatchEventTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleSubProcessTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleTimerCatchEventTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleUserTaskTest
  # tests executed successfully
  Should contain  ${result.stdout}  Failures: 0, Errors: 0, Skipped: 0

  Should be equal as integers  ${result.rc}  0
