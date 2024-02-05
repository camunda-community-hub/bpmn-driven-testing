* Settings
Library  OperatingSystem
Library  Process

* Test Cases
mvn clean test
  [Tags]  xignore  maven
  ${result}=  Run process
  ...  mvn  -B  -f  ${CURDIR}/advanced-multi-instance/pom.xml  clean  test  -Dplugin.version\=${VERSION}
  ...  shell=True  stdout=${TEMP}/advanced-multi-instance.out  stderr=STDOUT

  Log  ${result.stdout}

  # plugin executed
  Should contain  ${result.stdout}  bpmn-driven-testing-maven-plugin:${VERSION}:generator

  Assert Test Code Generation  ${result}  target

  # tests executed
  Should contain  ${result.stdout}  Running org.example.it.AsyncTest
  Should contain  ${result.stdout}  Running org.example.it.CallActivityTest
  Should contain  ${result.stdout}  Running org.example.it.CallActivityErrorTest
  Should contain  ${result.stdout}  Running org.example.it.CallActivityTimerTest
  Should contain  ${result.stdout}  Running org.example.it.ParallelTest
  Should contain  ${result.stdout}  Running org.example.it.ScopeErrorEndEventTest
  Should contain  ${result.stdout}  Running org.example.it.ScopeInnerTest
  Should contain  ${result.stdout}  Running org.example.it.ScopeNestedSubProcessTest
  Should contain  ${result.stdout}  Running org.example.it.ScopeNestedTest
  Should contain  ${result.stdout}  Running org.example.it.ScopeParallelTest
  Should contain  ${result.stdout}  Running org.example.it.ScopeSequentialTest
  Should contain  ${result.stdout}  Running org.example.it.ScopeZeroTest
  Should contain  ${result.stdout}  Running org.example.it.SequentialTest
  Should contain  ${result.stdout}  Running org.example.it.UserTaskTest
  Should contain  ${result.stdout}  Running org.example.it.UserTaskErrorTest
  Should contain  ${result.stdout}  Running org.example.it.UserTaskMessageTest
  # tests executed successfully
  Should contain  ${result.stdout}  Failures: 0, Errors: 0, Skipped: 0

  Should be equal as integers  ${result.rc}  0

gradle clean build
  [Tags]  xignore  gradle
  ${result}=  Run process
  ...  gradle  -p  ${CURDIR}/advanced-multi-instance  clean  build  -Pplugin.version\=${VERSION}  --info  --stacktrace
  ...  shell=True  stdout=${TEMP}/advanced-multi-instance.out  stderr=STDOUT

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

  ${testSources}  Set variable  ${CURDIR}/advanced-multi-instance/${buildDir}/bpmndt

  # multi instance handler classes generated and written
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/async/MultiInstanceManualTaskHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/callactivity/MultiInstanceCallActivityHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/callactivityerror/MultiInstanceCallActivityHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/callactivitytimer/MultiInstanceCallActivityHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/parallel/MultiInstanceManualTaskHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/scopeerrorendevent/TC_Error__SubProcessHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/scopeerrorendevent/TC_None__SubProcessHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/scopenested/TC_startEvent__endEvent__NestedSubProcessHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/scopenested/TC_startEvent__endEvent__SubProcessHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/scopenested/TC_subProcessStartEvent__endEvent__NestedSubProcessHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/scopenestedsubprocess/TC_startEvent__endEvent__SubProcessHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/scopeparallel/TC_startEvent__endEvent__MultiInstanceScopeHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/scopesequential/TC_startEvent__endEvent__MultiInstanceScopeHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/scopezero/TC_startEvent__endEvent__SubProcessHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/sequential/MultiInstanceManualTaskHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/usertask/MultiInstanceUserTaskHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/usertaskerror/MultiInstanceUserTaskHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/usertaskmessage/MultiInstanceUserTaskHandler.java

  File should exist  ${testSources}/generated/async/MultiInstanceManualTaskHandler.java
  File should exist  ${testSources}/generated/callactivity/MultiInstanceCallActivityHandler.java
  File should exist  ${testSources}/generated/callactivityerror/MultiInstanceCallActivityHandler.java
  File should exist  ${testSources}/generated/callactivitytimer/MultiInstanceCallActivityHandler.java
  File should exist  ${testSources}/generated/parallel/MultiInstanceManualTaskHandler.java
  File should exist  ${testSources}/generated/scopeerrorendevent/TC_Error__SubProcessHandler.java
  File should exist  ${testSources}/generated/scopeerrorendevent/TC_None__SubProcessHandler.java
  File should exist  ${testSources}/generated/scopenested/TC_startEvent__endEvent__NestedSubProcessHandler.java
  File should exist  ${testSources}/generated/scopenested/TC_startEvent__endEvent__SubProcessHandler.java
  File should exist  ${testSources}/generated/scopenested/TC_subProcessStartEvent__endEvent__NestedSubProcessHandler.java
  File should exist  ${testSources}/generated/scopenestedsubprocess/TC_startEvent__endEvent__SubProcessHandler.java
  File should exist  ${testSources}/generated/scopeparallel/TC_startEvent__endEvent__MultiInstanceScopeHandler.java
  File should exist  ${testSources}/generated/scopesequential/TC_startEvent__endEvent__MultiInstanceScopeHandler.java
  File should exist  ${testSources}/generated/sequential/MultiInstanceManualTaskHandler.java
  File should exist  ${testSources}/generated/scopezero/TC_startEvent__endEvent__SubProcessHandler.java
  File should exist  ${testSources}/generated/usertask/MultiInstanceUserTaskHandler.java
  File should exist  ${testSources}/generated/usertaskerror/MultiInstanceUserTaskHandler.java
  File should exist  ${testSources}/generated/usertaskmessage/MultiInstanceUserTaskHandler.java
