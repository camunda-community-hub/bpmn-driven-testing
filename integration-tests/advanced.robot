* Settings
Library  OperatingSystem
Library  Process

* Test Cases
mvn clean test
  [Tags]  xignore  maven
  ${result}=  Run process
  ...  mvn  -B  -f  ${CURDIR}/advanced/pom.xml  clean  test  -Dplugin.version\=${VERSION}
  ...  shell=True  stdout=${TEMP}/advanced.out  stderr=STDOUT

  Log  ${result.stdout}

  # plugin executed
  Should contain  ${result.stdout}  bpmn-driven-testing:${VERSION}:generator

  # tests executed
  Should contain  ${result.stdout}  Running org.example.it.CallActivityErrorTest
  Should contain  ${result.stdout}  Running org.example.it.CallActivityEscalationTest
  Should contain  ${result.stdout}  Running org.example.it.CallActivityMessageTest
  Should contain  ${result.stdout}  Running org.example.it.CallActivitySignalTest
  Should contain  ${result.stdout}  Running org.example.it.CallActivityTimerTest
  Should contain  ${result.stdout}  Running org.example.it.CallActivityVariablesTest
  Should contain  ${result.stdout}  Running org.example.it.CallActivityWithMappingTest
  Should contain  ${result.stdout}  Running org.example.it.CallActivityWithoutMappingTest
  Should contain  ${result.stdout}  Running org.example.it.CollaborationTest
  Should contain  ${result.stdout}  Running org.example.it.ExternalTaskErrorTest
  Should contain  ${result.stdout}  Running org.example.it.ExternalTaskMessageTest
  Should contain  ${result.stdout}  Running org.example.it.LinkEventTest
  Should contain  ${result.stdout}  Running org.example.it.ReceiveTaskTimerTest
  Should contain  ${result.stdout}  Running org.example.it.ServiceTaskTest
  Should contain  ${result.stdout}  Running org.example.it.SubProcessCallActivityMessageTest
  Should contain  ${result.stdout}  Running org.example.it.UserTaskErrorTest
  Should contain  ${result.stdout}  Running org.example.it.UserTaskEscalationTest
  Should contain  ${result.stdout}  Running org.example.it.UserTaskMessageTest
  # tests executed successfully
  Should contain  ${result.stdout}  Failures: 0, Errors: 0, Skipped: 0

  Should be equal as integers  ${result.rc}  0

gradle clean build
  [Tags]  xignore  gradle
  ${result}=  Run process
  ...  gradle  -p  ${CURDIR}/advanced  clean  build  -Pplugin.version\=${VERSION}  --info  --stacktrace
  ...  shell=True  stdout=${TEMP}/advanced.out  stderr=STDOUT

  Log  ${result.stdout}

  # task executed
  Should contain  ${result.stdout}  > Task :generateTestCases

  # tests executed
  Should contain  ${result.stdout}  finished executing tests
  # tests executed successfully
  Should contain  ${result.stdout}  Failures: 0, Skipped: 0

  Should be equal as integers  ${result.rc}  0
