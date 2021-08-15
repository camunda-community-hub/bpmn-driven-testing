* Settings
Library  OperatingSystem
Library  Process

* Test Cases
mvn clean test
  [Tags]  xignore
  ${result}=  Run process
  ...  mvn  -B  -f  ${CURDIR}/advanced/pom.xml  clean  package  -Dplugin.version\=${VERSION}
  ...  shell=True  stdout=${TEMP}/advanced.out  stderr=STDOUT

  Log  ${result.stdout}

  # plugin executed
  Should contain  ${result.stdout}  bpmn-driven-testing-maven-plugin:${VERSION}:generator

  # tests executed
  Should contain  ${result.stdout}  Running org.example.it.CallActivityErrorTest
  Should contain  ${result.stdout}  Running org.example.it.CallActivityEscalationTest
  Should contain  ${result.stdout}  Running org.example.it.CallActivityMessageTest
  Should contain  ${result.stdout}  Running org.example.it.CallActivitySignalTest
  Should contain  ${result.stdout}  Running org.example.it.CallActivityTimerTest
  Should contain  ${result.stdout}  Running org.example.it.CallActivityWithMappingTest
  Should contain  ${result.stdout}  Running org.example.it.CallActivityWithoutMappingTest
  Should contain  ${result.stdout}  Running org.example.it.ExternalTaskErrorTest
  Should contain  ${result.stdout}  Running org.example.it.ExternalTaskMessageTest
  Should contain  ${result.stdout}  Running org.example.it.UserTaskErrorTest
  # tests executed successfully
  Should contain  ${result.stdout}  Failures: 0, Errors: 0, Skipped: 0

  Should be equal as integers  ${result.rc}  0
