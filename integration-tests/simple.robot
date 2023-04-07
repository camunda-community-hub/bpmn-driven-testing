* Settings
Library  OperatingSystem
Library  Process

* Test Cases
mvn clean test
  [Tags]  xignore  maven
  ${result}=  Run process
  ...  mvn  -B  -f  ${CURDIR}/simple/pom.xml  clean  test  -Dplugin.version\=${VERSION}
  ...  shell=True  stdout=${TEMP}/simple.out  stderr=STDOUT

  Log  ${result.stdout}

  # plugin executed
  Should contain  ${result.stdout}  bpmn-driven-testing-maven-plugin:${VERSION}:generator

  Assert Test Code Generation  ${result}  target

  # tests executed
  Should contain  ${result.stdout}  Running org.example.it.SimpleTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleAsyncTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleCallActivityTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleCollaborationTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleConditionalCatchEventTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleEventBasedGatewayTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleExternalTaskTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleMessageCatchEventTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleMessageThrowEventTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleReceiveTaskTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleSignalCatchEventTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleSubProcessTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleSubProcessNestedTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleTimerCatchEventTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleUserTaskTest
  # tests executed successfully
  Should contain  ${result.stdout}  Failures: 0, Errors: 0, Skipped: 0

  Should be equal as integers  ${result.rc}  0

gradle clean build
  [Tags]  xignore  gradle
  ${result}=  Run process
  ...  gradle  -p  ${CURDIR}/simple  clean  build  -Pplugin.version\=${VERSION}  --info  --stacktrace
  ...  shell=True  stdout=${TEMP}/simple.out  stderr=STDOUT

  Log  ${result.stdout}

  # task executed
  Should contain  ${result.stdout}  > Task :generateTestCases

  Assert Test Code Generation  ${result}  build

  # tests executed
  Should contain  ${result.stdout}  finished executing tests
  # tests executed successfully
  Should contain  ${result.stdout}  Failures: 0, Skipped: 0

  Should be equal as integers  ${result.rc}  0

* Keywords
Assert Test Code Generation
  [Arguments]  ${result}  ${buildDir}

  ${testSources}  Set variable  ${CURDIR}/simple/${buildDir}/bpmndt

  # test source directory added
  Should contain  ${result.stdout}  Adding test source directory:

  # BPMN files found
  Should contain  ${result.stdout}  Found BPMN file: simple.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleAsync.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleCallActivity.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleCollaboration.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleConditionalCatchEvent.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleExternalTask.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleMessageCatchEvent.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleMessageThrowEvent.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleReceiveTask.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleSignalCatchEvent.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleSubProcess.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleTimerCatchEvent.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleUserTask.bpmn
  Should contain  ${result.stdout}  Found BPMN file: special/duplicateTestCaseNames.bpmn
  Should contain  ${result.stdout}  Found BPMN file: special/empty.bpmn
  Should contain  ${result.stdout}  Found BPMN file: special/happyPath.bpmn
  Should contain  ${result.stdout}  Found BPMN file: special/incomplete.bpmn
  Should contain  ${result.stdout}  Found BPMN file: special/invalid.bpmn
  Should contain  ${result.stdout}  Found BPMN file: special/noTestCases.bpmn

  # test cases generated
  Should contain  ${result.stdout}  Process: duplicateTestCaseNames
  Should contain  ${result.stdout}  Generating test case 'startEvent__endEvent'
  Should contain  ${result.stdout}  Skipping test case #2: Name must be unique

  Should contain  ${result.stdout}  Process: empty
  Should contain  ${result.stdout}  Test case #1 has an empty path

  Should contain  ${result.stdout}  Process: happyPath
  Should contain  ${result.stdout}  Generating test case 'Happy_Path'

  Should contain  ${result.stdout}  Process: incomplete
  Should contain  ${result.stdout}  Test case #1 has an incomplete path

  Should contain  ${result.stdout}  Process: invalid
  Should contain  ${result.stdout}  Test case #1 has an invalid path - invalid flow node IDs: [a, b]

  Should contain  ${result.stdout}  Process: noTestCases
  Should contain  ${result.stdout}  No test cases defined

  Should contain  ${result.stdout}  Process: simple
  Should contain  ${result.stdout}  Generating test case 'startEvent__endEvent'

  # test cases written
  Should contain  ${result.stdout}  Writing test cases
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simple/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simpleasync/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simplecallactivity/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simplecollaboration/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simpleconditionalcatchevent/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simpleeventbasedgateway/TC_Message.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simpleeventbasedgateway/TC_startEvent__eventBasedGateway.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simpleeventbasedgateway/TC_Timer.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simpleexternaltask/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simplemessagecatchevent/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simplemessagethrowevent/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simplereceivetask/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simplesignalcatchevent/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simplesubprocess/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simplesubprocessnested/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simpletimercatchevent/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simpleusertask/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/duplicatetestcasenames/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/happypath/TC_Happy_Path.java

  File should exist  ${testSources}/generated/simple/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simpleasync/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simplecallactivity/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simplecollaboration/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simpleconditionalcatchevent/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simpleeventbasedgateway/TC_Message.java
  File should exist  ${testSources}/generated/simpleeventbasedgateway/TC_startEvent__eventBasedGateway.java
  File should exist  ${testSources}/generated/simpleeventbasedgateway/TC_Timer.java
  File should exist  ${testSources}/generated/simpleexternaltask/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simplemessagecatchevent/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simplemessagethrowevent/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simplereceivetask/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simplesignalcatchevent/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simplesubprocess/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simplesubprocessnested/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simpletimercatchevent/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simpleusertask/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/duplicatetestcasenames/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/happypath/TC_Happy_Path.java

  # API classes written
  Should contain  ${result.stdout}  Writing API classes
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/AbstractJUnit4TestCase.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/AbstractTestCase.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/CallActivityDefinition.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/CallActivityHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/EventHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/ExternalTaskHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/JobHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/MultiInstanceHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/MultiInstanceScopeHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/TestCaseInstance.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/TestCaseExecutor.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/UserTaskHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/cfg/BpmndtParseListener.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/cfg/BpmndtProcessEnginePlugin.java

  File should exist  ${testSources}/org/camunda/community/bpmndt/api/AbstractJUnit4TestCase.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/AbstractTestCase.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/CallActivityDefinition.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/CallActivityHandler.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/EventHandler.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/ExternalTaskHandler.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/JobHandler.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/MultiInstanceHandler.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/MultiInstanceScopeHandler.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/TestCaseInstance.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/TestCaseExecutor.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/UserTaskHandler.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/cfg/BpmndtParseListener.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/cfg/BpmndtProcessEnginePlugin.java
