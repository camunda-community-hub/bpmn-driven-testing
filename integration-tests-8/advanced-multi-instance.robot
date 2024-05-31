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
  Should contain  ${result.stdout}  bpmn-driven-testing-8-maven-plugin:${VERSION}:generator

  # tests executed
  Should contain  ${result.stdout}  Running org.example.it.ParallelTest
  Should contain  ${result.stdout}  Running org.example.it.ScopeErrorEndEventTest
  Should contain  ${result.stdout}  Running org.example.it.ScopeNestedTest
  Should contain  ${result.stdout}  Running org.example.it.ScopeParallelTest
  Should contain  ${result.stdout}  Running org.example.it.ScopeSequentialTest
  Should contain  ${result.stdout}  Running org.example.it.ScopeZeroTest
  Should contain  ${result.stdout}  Running org.example.it.SequentialTest
  Should contain  ${result.stdout}  Running org.example.it.UserTaskErrorTest
  Should contain  ${result.stdout}  Running org.example.it.UserTaskMessageTest
  # tests executed successfully
  Should contain  ${result.stdout}  Failures: 0, Errors: 0, Skipped: 1

  Should be equal as integers  ${result.rc}  0

gradle clean build
  [Tags]  xignore  gradle
  ${result}=  Run process
  ...  gradle  -p  ${CURDIR}/advanced-multi-instance  clean  build  -Pplugin.version\=${VERSION}  --info  --stacktrace
  ...  shell=True  stdout=${TEMP}/advanced-multi-instance.out  stderr=STDOUT

  Log  ${result.stdout}

  # task executed
  Should contain  ${result.stdout}  > Task :generateTestCases

  # tests executed
  Should contain  ${result.stdout}  finished executing tests
  # tests executed successfully
  Should contain  ${result.stdout}  Failures: 0, Skipped: 1

  Should be equal as integers  ${result.rc}  0
