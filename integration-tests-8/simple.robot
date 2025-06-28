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
  Should contain  ${result.stdout}  bpmn-driven-testing-8:${VERSION}:generator

  Assert Test Code Generation  ${result}  target

  # tests executed
  Should contain  ${result.stdout}  Running org.example.it.SimpleBusinessRuleTaskTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleCallActivityTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleCollaborationTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleEventBasedGatewayTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleMessageCatchEventTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleMessageEndEventTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleMessageStartEventTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleMessageThrowEventTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleReceiveTaskTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleScriptTaskTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleSendTaskTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleServiceTaskTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleSignalCatchEventTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleSignalStartEventTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleSubProcessNestedTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleSubProcessTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleTimerCatchEventTest
  Should contain  ${result.stdout}  Running org.example.it.SimpleUserTaskTest
  # tests executed successfully
  Should contain  ${result.stdout}  Failures: 0, Errors: 0, Skipped: 1

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
  Should contain  ${result.stdout}  Failures: 0, Skipped: 1

  Should be equal as integers  ${result.rc}  0

* Keywords
Assert Test Code Generation
  [Arguments]  ${result}  ${buildDir}

  ${testSources}  Set variable  ${CURDIR}/simple/${buildDir}/bpmndt

  # test source directory added
  Should contain  ${result.stdout}  Adding test source directory:

  # BPMN files found
  Should contain  ${result.stdout}  Found BPMN file: simple.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleBusinessRuleTask.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleCallActivity.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleCollaboration.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleEventBasedGateway.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleMessageCatchEvent.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleMessageEndEvent.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleMessageStartEvent.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleMessageThrowEvent.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleOutboundConnector.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleReceiveTask.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleScriptTask.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleSendTask.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleServiceTask.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleSignalCatchEvent.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleSignalStartEvent.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleSubProcess.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleSubProcessNested.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleTimerCatchEvent.bpmn
  Should contain  ${result.stdout}  Found BPMN file: simpleUserTask.bpmn
  Should contain  ${result.stdout}  Found BPMN file: special/duplicateTestCaseNames.bpmn
  Should contain  ${result.stdout}  Found BPMN file: special/empty.bpmn
  Should contain  ${result.stdout}  Found BPMN file: special/happyPath.bpmn
  Should contain  ${result.stdout}  Found BPMN file: special/happyPathPlatform7.bpmn
  Should contain  ${result.stdout}  Found BPMN file: special/incomplete.bpmn
  Should contain  ${result.stdout}  Found BPMN file: special/invalid.bpmn
  Should contain  ${result.stdout}  Found BPMN file: special/noTestCases.bpmn

  # test cases generated
  Should contain  ${result.stdout}  Process: duplicateTestCaseNames
  Should contain  ${result.stdout}  Generating test case 'startEvent__endEvent'
  Should contain  ${result.stdout}  Skipping test case #2: name 'startEvent__endEvent' must be unique

  Should contain  ${result.stdout}  Process: empty
  Should contain  ${result.stdout}  Test case #1 has an empty path

  Should contain  ${result.stdout}  Process: happyPath
  Should contain  ${result.stdout}  Generating test case 'Happy_Path'

  Should contain  ${result.stdout}  Skipping BPMN model special/happyPathPlatform7.bpmn, since it is not designed for Camunda Platform 8

  Should contain  ${result.stdout}  Process: incomplete
  Should contain  ${result.stdout}  Test case #1 has an incomplete path

  Should contain  ${result.stdout}  Process: invalid
  Should contain  ${result.stdout}  Test case #1 has an invalid path - invalid element IDs: [a, b]

  Should contain  ${result.stdout}  Process: noTestCases
  Should contain  ${result.stdout}  No test cases defined

  Should contain  ${result.stdout}  Process: simple
  Should contain  ${result.stdout}  Generating test case 'startEvent__endEvent'

  # test cases written
  Should contain  ${result.stdout}  Writing test cases
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simple/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simplebusinessruletask/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simplecallactivity/TC_startEvent__callActivity.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simplecallactivity/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simplecollaboration/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simpleeventbasedgateway/TC_Message.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simpleeventbasedgateway/TC_startEvent__eventBasedGateway.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simpleeventbasedgateway/TC_Timer.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simplemessagecatchevent/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simplemessageendevent/TC_startEvent__messageEndEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simplemessagestartevent/TC_messageStartEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simplemessagethrowevent/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simpleoutboundconnector/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simplereceivetask/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simplescripttask/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simplesendtask/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simpleservicetask/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simplesignalcatchevent/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simplesignalstartevent/TC_signalStartEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simplesubprocess/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simplesubprocess/TC_startEvent__subProcessEndEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simplesubprocess/TC_subProcessStartEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simplesubprocessnested/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simplesubprocessnested/TC_userTask__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simpletimercatchevent/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/simpleusertask/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/duplicatetestcasenames/TC_startEvent__endEvent.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/happypath/TC_Happy_Path.java

  File should exist  ${testSources}/generated/simple/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simplebusinessruletask/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simplecallactivity/TC_startEvent__callActivity.java
  File should exist  ${testSources}/generated/simplecallactivity/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simplecollaboration/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simpleeventbasedgateway/TC_Message.java
  File should exist  ${testSources}/generated/simpleeventbasedgateway/TC_startEvent__eventBasedGateway.java
  File should exist  ${testSources}/generated/simpleeventbasedgateway/TC_Timer.java
  File should exist  ${testSources}/generated/simplemessagecatchevent/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simplemessageendevent/TC_startEvent__messageEndEvent.java
  File should exist  ${testSources}/generated/simplemessagestartevent/TC_messageStartEvent__endEvent.java
  File should exist  ${testSources}/generated/simplemessagethrowevent/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simpleoutboundconnector/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simplereceivetask/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simplescripttask/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simplesendtask/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simpleservicetask/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simplesignalcatchevent/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simplesignalstartevent/TC_signalStartEvent__endEvent.java
  File should exist  ${testSources}/generated/simplesubprocess/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simplesubprocess/TC_startEvent__subProcessEndEvent.java
  File should exist  ${testSources}/generated/simplesubprocess/TC_subProcessStartEvent__endEvent.java
  File should exist  ${testSources}/generated/simplesubprocessnested/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simplesubprocessnested/TC_userTask__endEvent.java
  File should exist  ${testSources}/generated/simpletimercatchevent/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/simpleusertask/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/duplicatetestcasenames/TC_startEvent__endEvent.java
  File should exist  ${testSources}/generated/happypath/TC_Happy_Path.java

  # API classes written
  Should contain  ${result.stdout}  Writing API classes
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/AbstractJUnit5TestCase.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/AbstractTestCase.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/CallActivityBindingType.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/CallActivityHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/CustomMultiInstanceHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/JobHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/MessageEventHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/OutboundConnectorHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/ReceiveTaskHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/SignalEventHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/SimulateSubProcessResource.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/TestCaseInstance.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/TestCaseExecutor.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/TestCaseInstanceElement.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/TestCaseInstanceMemo.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/TimerEventHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/UserTaskHandler.java

  File should exist  ${testSources}/org/camunda/community/bpmndt/api/AbstractJUnit5TestCase.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/AbstractTestCase.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/CallActivityBindingType.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/CallActivityHandler.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/CustomMultiInstanceHandler.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/JobHandler.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/MessageEventHandler.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/OutboundConnectorHandler.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/ReceiveTaskHandler.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/SignalEventHandler.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/SimulateSubProcessResource.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/TestCaseInstance.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/TestCaseExecutor.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/TestCaseInstanceElement.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/TestCaseInstanceMemo.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/TimerEventHandler.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/UserTaskHandler.java
