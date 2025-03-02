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
  Should contain  ${result.stdout}  bpmn-driven-testing-8-maven-plugin:${VERSION}:generator

  # tests executed
  Should contain  ${result.stdout}  Running org.example.it.CallActivityBindingDeploymentTest
  Should contain  ${result.stdout}  Running org.example.it.CallActivityBindingVersionTagTest
  Should contain  ${result.stdout}  Running org.example.it.CallActivityErrorTest
  Should contain  ${result.stdout}  Running org.example.it.CallActivityEscalationTest
  Should contain  ${result.stdout}  Running org.example.it.CallActivityMessageTest
  Should contain  ${result.stdout}  Running org.example.it.CallActivitySignalTest
  Should contain  ${result.stdout}  Running org.example.it.CallActivityTimerTest
  Should contain  ${result.stdout}  Running org.example.it.CollaborationTest
  Should contain  ${result.stdout}  Running org.example.it.LinkEventTest
  Should contain  ${result.stdout}  Running org.example.it.OutboundConnectorErrorTest
  Should contain  ${result.stdout}  Running org.example.it.ReceiveTaskTimerTest
  Should contain  ${result.stdout}  Running org.example.it.ServiceTaskErrorTest
  Should contain  ${result.stdout}  Running org.example.it.ServiceTaskMessageTest
  Should contain  ${result.stdout}  Running org.example.it.ServiceTaskSignalTest
  Should contain  ${result.stdout}  Running org.example.it.ServiceTaskTimerTest
  Should contain  ${result.stdout}  Running org.example.it.UserTaskErrorTest
  Should contain  ${result.stdout}  Running org.example.it.UserTaskMessageTest
  Should contain  ${result.stdout}  Running org.example.it.UserTaskSignalTest
  Should contain  ${result.stdout}  Running org.example.it.UserTaskTimerTest
  # tests executed successfully
  Should contain  ${result.stdout}  Failures: 0, Errors: 0, Skipped: 1

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
  Should contain  ${result.stdout}  Failures: 0, Skipped: 1

  Should be equal as integers  ${result.rc}  0
